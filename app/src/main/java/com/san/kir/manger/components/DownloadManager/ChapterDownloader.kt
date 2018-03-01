package com.san.kir.manger.components.DownloadManager

import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.utils.getFullPath
import okio.Okio
import java.io.File
import java.net.URL
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors.newFixedThreadPool
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.regex.Pattern


class ChapterDownloader(private val task: DownloadItem) : Runnable {
    val downloadItem: DownloadItem
        get() {
            task.totalPages = totalPages.get()
            task.downloadPages = downloadPages.get()
            task.downloadSize = downloadSize.get()
            task.totalTime = totalTime.get()
            return task
        }

    var delegate: Delegate? = null

    private val totalPages = AtomicInteger(0)
    private val downloadPages = AtomicInteger(0)
    private val downloadSize = AtomicLong(0L)
    private val totalTime = AtomicLong(0L)


    private var executor = newFixedThreadPool(3)

    @Volatile
    var interrupted: Boolean = false
        private set
    @Volatile
    var terminated: Boolean = false

    fun cancel() {
        interrupted = true
    }

    override fun run() {
        val previousTime = System.currentTimeMillis()
        delegate?.onStarted(downloadItem)

        try {
            download()
            totalTime.set(System.currentTimeMillis() - previousTime)

            if (!interrupted) delegate?.onComplete(downloadItem)
        } catch (e: ExecutionException) {
            delegate?.onError(downloadItem, e.cause)
        } catch (e: Exception) {
            delegate?.onError(downloadItem, e.cause)
        } finally {
            executor.shutdownNow()
            while (!executor.awaitTermination(10, TimeUnit.MILLISECONDS)) {
            }
            terminated = true
        }

    }

    private fun download() {
        if (interrupted) return

        val downloadPath = getFullPath(downloadItem.path)

        if (interrupted) return

        val pages = ManageSites.pages(downloadItem)
        totalPages.set(pages.size)
        delegate?.onProgress(downloadItem)

        if (interrupted) return

        pages.forEach { url ->
            val task = executor.submit {

                pageDownload(prepareUrl(url), downloadPath)

            }
            task.get()
        }

        try {
            executor.shutdown()
            while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
            }
        } catch (e: Exception) {
            e.printStackTrace()
            executor.shutdownNow()
        }
    }

    private fun prepareUrl(url: String) = url.removeSurrounding("\"", "\"")

    private fun pageDownload(link: String, downloadPath: File) {
        if (interrupted) return

        val page = File(downloadPath, nameFromUrl(link))

        if (interrupted) return

        val pageSize = sizeOfPageFromUrl(link)

        if (!interrupted
            && pageSize != -1L // check valid size
            && page.exists()
            && page.length() == pageSize) {
            downloadPages.incrementAndGet()
            downloadSize.addAndGet(pageSize)
            delegate?.onProgress(downloadItem)
        } else if (!interrupted) {
            val body = ManageSites.openLink(link).body()
            val contentLength = body!!.contentLength()

            val sink = Okio.buffer(Okio.sink(page))
            sink.writeAll(body.source())
            sink.close()

            if (interrupted) return

            downloadSize.addAndGet(contentLength)
            downloadPages.incrementAndGet()
            delegate?.onProgress(downloadItem)
        }
    }

    private fun nameFromUrl(url: String): String {
        val pat = Pattern.compile("[a-z0-9._-]+\\.[a-z]{3,4}")
            .matcher(url.removeSurrounding("\"", "\""))
        var name = ""
        while (pat.find())
            name = pat.group()
        return name
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
}

