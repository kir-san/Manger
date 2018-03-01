package com.san.kir.manger.components.DownloadManager

import android.os.Handler
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.utils.log

class IteratorProcessor(
    private val handler: Handler,
    private val manager: DownloadManager
) {
    private val PRIORITY_QUEUE_INTERVAL_IN_MILLISECONDS = 500L
    private val dbManager = Main.db.downloadDao

    private val lock = Object()

    @Volatile
    var isStopped = true
        private set

    private val iteratorRunnable = Runnable {
        val iterator = getIterator()

        if (iterator.hasNext()) {
            while (iterator.hasNext() && manager.canAccommodateNewDownload()) {
                val download = iterator.next()

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
            log("iterator stop")
        }
    }

    private fun getIterator(): Iterator<DownloadItem> {
        synchronized(lock) {
            val queuedList = dbManager.getQueuedDownloads()
            return queuedList.iterator()
        }
    }

    private fun registerIterator() {
        handler.postDelayed(iteratorRunnable, PRIORITY_QUEUE_INTERVAL_IN_MILLISECONDS)
    }

    private fun unregisterIterator() {
        handler.removeCallbacks(iteratorRunnable)
    }
}
