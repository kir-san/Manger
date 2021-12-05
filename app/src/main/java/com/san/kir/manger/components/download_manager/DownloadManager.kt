package com.san.kir.manger.components.download_manager

import com.san.kir.manger.components.parsing.ConnectManager
import com.san.kir.data.parsing.SiteCatalogsManager
import com.san.kir.manger.data.room.entities.Chapter
import com.san.kir.manger.utils.JobContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManager @Inject constructor(
    private val connectManager: ConnectManager,
    private val manager: com.san.kir.data.parsing.SiteCatalogsManager,
) {
    private val lock = Mutex()
    private val concurrentLimit: Int = 1
    private val executor = JobContext(Executors.newFixedThreadPool(concurrentLimit))
    private val currentDownloadsMap = hashMapOf<Long, ChapterDownloader>()

    private var concurrentPages: Int = 4
    private var downloadCounter = 0
    private var isClosed: Boolean = false

    var delegate: Delegate? = null

    suspend fun start(task: Chapter): Boolean {
        lock.withLock {
            if (currentDownloadsMap.containsKey(task.id)
                || downloadCounter >= concurrentLimit) {
                return false
            }

            val chapterDownloader = getNewChapterDownloader(task)
            downloadCounter += 1
            currentDownloadsMap[task.id] = chapterDownloader
            return try {
                executor.post {
                    chapterDownloader.run()

                    lock.withLock {
                        if (currentDownloadsMap.containsKey(task.id)) {
                            currentDownloadsMap.remove(task.id)
                            downloadCounter -= 1
                            delegate?.onDownloadRemovedFromManager(chapterDownloader.getDownloadItem())
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

    @Suppress("ControlFlowWithEmptyBody")
    suspend fun cancel(id: Long): Boolean {
        lock.withLock {
            return if (currentDownloadsMap.containsKey(id)) {
                val chapterDownloader = currentDownloadsMap[id] as ChapterDownloader
                chapterDownloader.cancel()
                while (!chapterDownloader.terminated) {

                }
                currentDownloadsMap.remove(id)
                downloadCounter -= 1
                delegate?.onDownloadRemovedFromManager(chapterDownloader.getDownloadItem())

                true
            } else {
                false
            }
        }
    }

    suspend fun cancelAll() {
        lock.withLock {
            cancelAllDownloads()
        }
    }

    suspend fun changeConcurrentPages(concurrent: Int) {
        lock.withLock {
            concurrentPages = concurrent
        }
    }

    suspend fun contains(id: Long): Boolean {
        lock.withLock {
            return currentDownloadsMap.containsKey(id)
        }
    }

    suspend fun canAccommodateNewDownload(): Boolean {
        lock.withLock {
            return downloadCounter < concurrentLimit
        }
    }

    suspend fun close() {
        lock.withLock {
            if (isClosed) {
                return
            }
            isClosed = true
            cancelAllDownloads()
            executor.close()
        }
    }

    @Suppress("ControlFlowWithEmptyBody")
    private suspend fun cancelAllDownloads() {
        currentDownloadsMap.iterator().forEach {
            it.value.cancel()
            while (!it.value.terminated) {

            }
        }
        currentDownloadsMap.clear()
        downloadCounter = 0
    }

    private fun getNewChapterDownloader(task: Chapter): ChapterDownloader {
        return ChapterDownloader(connectManager, manager, task, concurrentPages, delegate)
    }

    interface Delegate : ChapterDownloader.Delegate {
        fun onDownloadRemovedFromManager(item: Chapter)
    }
}
