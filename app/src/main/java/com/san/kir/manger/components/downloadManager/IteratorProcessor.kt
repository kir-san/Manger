package com.san.kir.manger.components.downloadManager

import android.os.Handler
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.models.DownloadItem

class IteratorProcessor(
    private val handler: Handler,
    private val manager: DownloadManager,
    private val networkManager: NetworkManager
) {
    private val priorityQueueIntervalInMilliseconds = 500L
    private val dbManager = Main.db.downloadDao

    private val lock = Object()

    @Volatile
    var isStopped = true
        private set

    @Volatile
    private var isRetry = false

    private val iteratorRunnable = Runnable {
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


    fun start() {
        synchronized(lock) {
            isStopped = false
            registerIterator()
        }
    }

    fun stop() {
        synchronized(lock) {
            unregisterIterator()
            isStopped = true
        }
    }

    fun setRetry(isRetry: Boolean) {
        synchronized(lock) {
            this.isRetry = isRetry
        }
    }

    private fun getIterator(): Iterator<DownloadItem> {
        synchronized(lock) {
            var queuedList = dbManager.getQueuedDownloads()
            if (isRetry) {
                queuedList += dbManager.getErrorDownloads()
            }
            return queuedList.iterator()
        }
    }

    private fun registerIterator() {
        handler.postDelayed(iteratorRunnable, priorityQueueIntervalInMilliseconds)
    }

    private fun unregisterIterator() {
        handler.removeCallbacks(iteratorRunnable)
    }
}
