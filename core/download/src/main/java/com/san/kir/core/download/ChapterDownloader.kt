package com.san.kir.core.download

import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.utils.convertImagesToPng
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.isOkPng
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.parsing.SiteCatalogsManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import java.util.concurrent.Executors.newFixedThreadPool

class ChapterDownloader(
    private val connectManager: ConnectManager,
    private val manager: SiteCatalogsManager,
    private var task: Chapter,
    concurrent: Int,
    private val delegate: Delegate?,
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
            task = task.copy(
                totalPages = totalPages,
                downloadPages = downloadPages,
                downloadSize = downloadSize,
                totalTime = totalTime,
            )
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

                connectManager.downloadFile(file, link)
                    .onSuccess { contentLength = it.contentLength }
                    .onFailure(Timber::e)

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
