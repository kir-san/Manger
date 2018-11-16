package com.san.kir.manger.components.downloadManager

import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.utils.JobContext
import com.san.kir.manger.utils.getFullPath
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.Okio
import java.io.File
import java.net.URL
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors.newFixedThreadPool
import java.util.regex.Pattern


class ChapterDownloader(private val task: DownloadItem, concurrent: Int) {
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

        try {
            download()

            lock.withLock {
                totalTime = System.currentTimeMillis() - previousTime
            }

            if (!interrupted) delegate?.onComplete(getDownloadItem())
        } catch (e: ExecutionException) {
            e.printStackTrace()
            delegate?.onError(getDownloadItem(), e.cause)
        } catch (e: Exception) {
            e.printStackTrace()
            delegate?.onError(getDownloadItem(), e.cause)
        } finally {
            executor.close()
            executor.join()
            terminated = true
        }

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

        pages.forEach { url ->
            val task = executor.post {
                pageDownload(prepareUrl(url), downloadPath)
            }
            task.join()
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
            && pageSize != -1L // check valid size
            && page.exists()
            && page.length() == pageSize) {
            lock.withLock {
                downloadPages++
                downloadSize += pageSize
            }
            delegate?.onProgress(getDownloadItem())
        } else if (!interrupted) {
            val body = ManageSites.openLink(link).body()
            val contentLength = body!!.contentLength()

            val sink = Okio.buffer(Okio.sink(page))
            sink.writeAll(body.source())
            sink.close()

            if (interrupted) return

            lock.withLock {
                downloadSize += contentLength
                downloadPages++
            }
            delegate?.onProgress(getDownloadItem())
        }
    }

    private fun sizeOfPageFromUrl(link: String): Long {
        val url = URL(link)
        val urlConnection = url.openConnection()
        urlConnection.connect()
        return urlConnection.contentLength.toLong()
    }

    interface Delegate {
        fun onStarted(item: DownloadItem)
        fun onProgress(item: DownloadItem)
        fun onError(item: DownloadItem, cause: Throwable?)
        fun onComplete(item: DownloadItem)
    }

    companion object {
        fun nameFromUrl(url: String): String {
            val pat = Pattern.compile("[a-z0-9._-]+\\.[a-z]{3,4}")
                .matcher(url.removeSurrounding("\"", "\""))
            var name = ""
            while (pat.find())
                name = pat.group()
            return name
        }

        fun prepareUrl(url: String) = url.removeSurrounding("\"", "\"")
    }
}
