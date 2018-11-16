package com.san.kir.manger.components.downloadManager

import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus
import com.san.kir.manger.utils.JobContext
import com.san.kir.manger.utils.log

class DownloadManagerDelegateImpl(
    private val uiJob: JobContext,
    private val job: JobContext,
    private val listener: DownloadListener,
    private val iteratorProcessor: IteratorProcessor
) : DownloadManager.Delegate {
    private val dbManager = Main.db.downloadDao
    override fun onDownloadRemovedFromManager(item: DownloadItem) {
        job.post {
            if (iteratorProcessor.isStopped) {
                iteratorProcessor.start()
            }
        }
    }

    override fun onStarted(item: DownloadItem) {
        item.status = DownloadStatus.loading
        dbManager.update(item)
        uiJob.post {
            listener.onProgress(item)
        }
    }

    override fun onProgress(item: DownloadItem) {
        dbManager.update(item)
        uiJob.post {
            listener.onProgress(item)
        }
    }

    override fun onError(item: DownloadItem, cause: Throwable?) {
        item.status = DownloadStatus.error
        dbManager.update(item)
        uiJob.post {
            listener.onError(item, cause)
        }
    }

    override fun onComplete(item: DownloadItem) {
        item.status = DownloadStatus.completed
        dbManager.update(item)

        log("onComplete")

        val stat = Main.db.statisticDao.getItem(item.manga)
        stat.downloadSize += item.totalSize
        stat.downloadTime += item.totalTime
        Main.db.statisticDao.update(stat)

        log("stat = ${stat}")

        uiJob.post {
            listener.onCompleted(item)
        }
    }
}
