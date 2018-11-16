package com.san.kir.manger.components.downloadManager

import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus
import com.san.kir.manger.utils.JobContext
import com.san.kir.manger.utils.NetworkManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class IteratorProcessor(
    private val job: JobContext,
    private val manager: DownloadManager,
    private val networkManager: NetworkManager
) {
    private val priorityQueueIntervalInMilliseconds = 500L
    private val dbManager = Main.db.downloadDao

    private val lock = Mutex()
    private var j: Job? = null

    var isStopped = true
        private set

    private var isRetry = false

    suspend fun start() {
        lock.withLock {
            isStopped = false
        }
        registerIterator()
    }

    suspend fun stop() {
        unregisterIterator()
        lock.withLock {
            isStopped = true
        }
    }

    suspend fun setRetry(isRetry: Boolean) {
        lock.withLock {
            this.isRetry = isRetry
        }
    }

    private fun getIterator(): Iterator<DownloadItem> {
        var queuedList = dbManager.getItems(DownloadStatus.queued)
        if (isRetry) {
            queuedList += dbManager.getItems(DownloadStatus.error)
        }
        return queuedList.iterator()
    }

    private fun registerIterator() {
        j = job.post {
            delay(priorityQueueIntervalInMilliseconds)
            val iterator = getIterator()

            if (iterator.hasNext()) {
                while (iterator.hasNext() && manager.canAccommodateNewDownload()) {
                    val download = iterator.next()
                    if (networkManager.isAvailable())
                        manager.start(download)
                }
            }
            stop()
        }
    }

    private fun unregisterIterator() {
        j?.cancel()
    }
}
