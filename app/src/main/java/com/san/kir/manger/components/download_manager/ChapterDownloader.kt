package com.san.kir.manger.components.download_manager

import com.san.kir.manger.components.parsing.ConnectManager
import com.san.kir.data.parsing.SiteCatalogsManager
import com.san.kir.manger.data.room.entities.Chapter
import com.san.kir.manger.utils.JobContext
import com.san.kir.manger.utils.extensions.convertImagesToPng
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.isOkPng
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.concurrent.Executors.newFixedThreadPool

class ChapterDownloader(
    private val connectManager: ConnectManager,
    private val manager: com.san.kir.data.parsing.SiteCatalogsManager,
    private val task: Chapter,
    concurrent: Int,
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

    suspend fun getDownloadItem(): Chapter {
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
            lock.withLock {
                totalPages = task.pages.size
            }

            delegate?.onProgress(getDownloadItem())

            if (interrupted) return

            task.pages.forEach { url ->
                executor.post {
                    pageDownload(prepareUrl(url), downloadPath)
                }.join()
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

        val name = connectManager.nameFromUrl(link)

        var file = File(downloadPath, name)

        file = File(file.parentFile, "${file.nameWithoutExtension}.png")

        if (!interrupted && file.exists() && file.isOkPng()) {

            lock.withLock {
                downloadPages++
                downloadSize += file.length()
            }

            delegate?.onProgress(getDownloadItem())

        } else if (!interrupted && file.exists()) {

            val png = convertImagesToPng(file)

            if (png.isOkPng().not()) {
                png.delete()
                return
            }

            lock.withLock {
                downloadPages++
                downloadSize += png.length()
            }

            delegate?.onProgress(getDownloadItem())

        } else if (!interrupted) {
            var contentLength = 0L
            var tryCount = 3

            // 3 попытки загрузить изображение
            while (tryCount != 0) {
                tryCount--

                if (interrupted) return

                if (link.isEmpty()) return

                contentLength = connectManager.downloadFile(file, link)

                // Если размер исходного и загруженного одинаков, то страница загружена
                if (file.exists() && file.length() == contentLength) {
                    val png = convertImagesToPng(file)

                    if (png.isOkPng()) break

                    png.delete()
                }
            }

            if (interrupted) return

            lock.withLock {
                downloadSize += contentLength
                downloadPages++
            }

            delegate?.onProgress(getDownloadItem())
        }
    }

    interface Delegate {
        fun onStarted(item: Chapter)
        fun onProgress(item: Chapter)
        fun onError(item: Chapter, cause: Throwable?)
        suspend fun onComplete(item: Chapter)
    }

    companion object {

        fun prepareUrl(url: String) = url.removeSurrounding("\"", "\"")
    }
}
