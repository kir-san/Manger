package com.san.kir.manger.components.downloadManager

import com.san.kir.manger.room.models.DownloadItem
import java.io.Closeable
import java.util.concurrent.Executors.newFixedThreadPool

class DownloadManager(private val concurrentLimit: Int) : Closeable {
    private val lock = Object()
    private val executor = newFixedThreadPool(concurrentLimit)
    private val currentDownloadsMap = hashMapOf<Long, ChapterDownloader>()

    @Volatile
    private var concurrentPages: Int = 4

    @Volatile
    private var downloadCounter = 0

    var delegate: DownloadManager.Delegate? = null

    @Volatile
    private var isClosed: Boolean = false

    fun start(task: DownloadItem): Boolean {
        synchronized(lock) {
            if (currentDownloadsMap.containsKey(task.id)
                || downloadCounter >= concurrentLimit) {
                return false
            }

            val chapterDownloader = getNewChapterDownloader(task)
            chapterDownloader.delegate = delegate
            downloadCounter += 1
            currentDownloadsMap[task.id] = chapterDownloader
            return try {
                executor.execute {
                    chapterDownloader.run()

                    synchronized(lock) {
                        if (currentDownloadsMap.containsKey(task.id)) {
                            currentDownloadsMap.remove(task.id)
                            downloadCounter -= 1
                            delegate?.onDownloadRemovedFromManager(chapterDownloader.downloadItem)
                        }
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun cancel(id: Long): Boolean {
        synchronized(lock) {
            return if (currentDownloadsMap.containsKey(id)) {
                val chapterDownloader = currentDownloadsMap[id] as ChapterDownloader
                chapterDownloader.cancel()
                while (!chapterDownloader.terminated) {

                }
                currentDownloadsMap.remove(id)
                downloadCounter -= 1
                delegate?.onDownloadRemovedFromManager(chapterDownloader.downloadItem)

                true
            } else {
                false
            }
        }
    }

    fun cancelAll() {
        synchronized(lock) {
            cancelAllDownloads()
        }
    }

    fun contains(id: Long): Boolean {
        synchronized(lock) {
            return currentDownloadsMap.containsKey(id)
        }
    }

    fun canAccommodateNewDownload(): Boolean {
        synchronized(lock) {
            return downloadCounter < concurrentLimit
        }
    }

    fun isWork(): Boolean {
        synchronized(lock) {
            return downloadCounter != 0
        }
    }

    fun changeConcurrentPages(concurrent: Int) {
        synchronized(lock) {
            concurrentPages = concurrent
        }
    }

    private fun getNewChapterDownloader(task: DownloadItem): ChapterDownloader {
        return ChapterDownloader(task = task, concurrent = concurrentPages)
    }

    private fun cancelAllDownloads() {
        currentDownloadsMap.iterator().forEach {
            it.value.cancel()
            while (!it.value.terminated) {

            }
        }
        currentDownloadsMap.clear()
        downloadCounter = 0
    }

    override fun close() {
        synchronized(lock) {
            if (isClosed) {
                return
            }
            isClosed = true
            cancelAllDownloads()
            executor.shutdown()
        }
    }

    interface Delegate : ChapterDownloader.Delegate {
        fun onDownloadRemovedFromManager(item: DownloadItem)
    }
}

