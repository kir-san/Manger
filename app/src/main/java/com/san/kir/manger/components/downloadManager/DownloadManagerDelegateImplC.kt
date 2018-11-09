package com.san.kir.manger.components.downloadManager

import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus

class DownloadManagerDelegateImplC(
    private val uiJob: JobContext,
    private val job: JobContext,
    private val listener: DownloadListener,
    private val iteratorProcessor: IteratorProcessorC
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

        val stat = Main.db.statisticDao.loadItem(item.manga)
        stat.downloadSize += item.totalSize
        stat.downloadTime += item.totalTime
        Main.db.statisticDao.update(stat)

        uiJob.post {
            listener.onCompleted(item)
        }
    }
}
