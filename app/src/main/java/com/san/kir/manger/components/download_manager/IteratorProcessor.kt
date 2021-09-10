package com.san.kir.manger.components.download_manager

import com.san.kir.manger.room.dao.DownloadDao
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.utils.JobContext
import com.san.kir.manger.utils.NetworkManager
import com.san.kir.manger.utils.enums.DownloadStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class IteratorProcessor @Inject constructor(
    private val job: JobContext,
    private val manager: DownloadManager,
    private val networkManager: NetworkManager,
    private val downloadDao: DownloadDao
) {
    private val priorityQueueIntervalInMilliseconds = 500L

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

    private suspend fun getIterator(): Iterator<DownloadItem> {
        var queuedList = downloadDao.getItems(DownloadStatus.queued)
        if (isRetry) {
            queuedList = queuedList + downloadDao.getErrorItems()
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
