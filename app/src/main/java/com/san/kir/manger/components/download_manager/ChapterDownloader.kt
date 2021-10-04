package com.san.kir.manger.components.download_manager

import com.github.kittinunf.fuel.Fuel
import com.san.kir.manger.components.parsing.SiteCatalogsManager
import com.san.kir.manger.room.dao.ChapterDao
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.utils.JobContext
import com.san.kir.manger.utils.extensions.createDirs
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.concurrent.Executors.newFixedThreadPool
import java.util.regex.Pattern


class ChapterDownloader(
    private val manager: SiteCatalogsManager,
    private val task: DownloadItem,
    concurrent: Int,
    private val chapterDao: ChapterDao,
    private val delegate: Delegate?
) {
    private var totalPages = 0
    private var downloadPages = 0
    private var downloadSize = 0L
    private var totalTime = 0L

    private val lock = Mutex()
    private var executor = JobContext(newFixedThreadPool(concurrent))

    @Volatile
    private var interrupted: Boolean = false

    @Volatile
    var terminated: Boolean = false

    suspend fun getDownloadItem(): DownloadItem {
        lock.withLock {
            task.totalPages = totalPages
            task.downloadPages = downloadPages
            task.downloadSize = downloadSize
            task.totalTime = totalTime
        }
        return task
    }

    suspend fun cancel() {
        lock.withLock {
            interrupted = true
        }
    }

    suspend fun run() {
        val previousTime = System.currentTimeMillis()
        delegate?.onStarted(getDownloadItem())

        runCatching {
            download()
        }.fold(
            onSuccess = {
                lock.withLock {
                    totalTime = System.currentTimeMillis() - previousTime
                }

                if (!interrupted) delegate?.onComplete(getDownloadItem())
            },
            onFailure = { e ->
                e.printStackTrace()
                delegate?.onError(getDownloadItem(), e.cause)
            }
        )

        executor.close()
        executor.join()
        terminated = true
    }

    private suspend fun download() {
        if (interrupted) return

        val downloadPath = getFullPath(getDownloadItem().path)

        if (interrupted) return

        val pages = manager.pages(getDownloadItem())

        if (interrupted) return

        if (pages.isNotEmpty() && pages.all { it.isNotEmpty() }) {

            lock.withLock {
                totalPages = pages.size
            }

            delegate?.onProgress(getDownloadItem())

            if (interrupted) return

            pages.forEach { url ->
                executor.post {
                    pageDownload(prepareUrl(url), downloadPath)
                }.join()
            }
        } else {
            chapterDao.getItem(getDownloadItem().link)?.let {

                lock.withLock {
                    totalPages = it.pages.size
                }

                delegate?.onProgress(getDownloadItem())

                if (interrupted) return

                it.pages.forEach { url ->
                    executor.post {
                        pageDownload(prepareUrl(url), downloadPath)
                    }.join()
                }
            } ?: run {

            }
        }

        try {
            executor.close()
            executor.join()
        } catch (e: Exception) {
            e.printStackTrace()
            executor.close()
        }
    }

    private suspend fun pageDownload(link: String, downloadPath: File) {
        if (interrupted) return

        val page = File(downloadPath, nameFromUrl(link))

        if (interrupted) return

        val pageSize = sizeOfPageFromUrl(link)

        val isCorrectPageSize = pageSize != null && pageSize != -1L // check valid size

        if (!interrupted
            && isCorrectPageSize
            && page.exists()
            && page.length() == pageSize) {
            lock.withLock {
                downloadPages++
                downloadSize += pageSize
            }
            delegate?.onProgress(getDownloadItem())
        } else if (!interrupted) {
            var contentLength = 0L

            Fuel.download(link)
                .fileDestination { response, _ ->
                    contentLength = response.contentLength
                    page.parentFile?.createDirs()
                    page.createNewFile()
                    page
                }
                .progress { readBytes, totalBytes ->
                    log("link = $link\nreadBytes = $readBytes, totalBytes = $totalBytes")
                }
                .response()

            if (interrupted) return

            lock.withLock {
                downloadSize += contentLength
                downloadPages++
            }
            delegate?.onProgress(getDownloadItem())
        }
    }

    private fun sizeOfPageFromUrl(link: String): Long? {
        val res = Fuel.get(link).response()
        res.third.fold(
            success = {
                return res.second.contentLength
            },
            failure = {
                return null
            }
        )
    }

    interface Delegate {
        fun onStarted(item: DownloadItem)
        fun onProgress(item: DownloadItem)
        fun onError(item: DownloadItem, cause: Throwable?)
        suspend fun onComplete(item: DownloadItem)
    }

    companion object {
        fun nameFromUrl(url: String): String {
            val pat = Pattern.compile("[\\w.-]+\\.[a-z]{3,4}")
                .matcher(prepareUrl(url))
            var name = ""
            while (pat.find())
                name = pat.group()
            return name
        }

        fun prepareUrl(url: String) = url.removeSurrounding("\"", "\"")
    }
}
