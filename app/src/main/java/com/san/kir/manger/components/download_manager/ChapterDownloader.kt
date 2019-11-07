package com.san.kir.manger.components.download_manager

import com.github.kittinunf.fuel.Fuel
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.room.RoomDB
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.utils.JobContext
import com.san.kir.manger.utils.extensions.createDirs
import com.san.kir.manger.utils.extensions.getFullPath
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.concurrent.Executors.newFixedThreadPool
import java.util.regex.Pattern


class ChapterDownloader(private val task: DownloadItem, concurrent: Int, private val db: RoomDB) {
    var delegate: Delegate? = null

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

        val pages = ManageSites.pages(getDownloadItem())

        if (pages.isEmpty()) {
            throw Exception("Problem with site")
        }

        lock.withLock {
            totalPages = pages.size
        }

        delegate?.onProgress(getDownloadItem())

        if (interrupted) return

        if (pages.all { it.isNotEmpty() })
            pages.forEach { url ->
                executor.post {
                    pageDownload(prepareUrl(url), downloadPath)
                }.join()
            }
        else {
            db.chapterDao.getItem(getDownloadItem().link)?.let {
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

        if (!interrupted
            && pageSize != null
            && pageSize != -1L // check valid size
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
                    page.parentFile.createDirs()
                    page.createNewFile()
                    page
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
        fun onComplete(item: DownloadItem)
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
