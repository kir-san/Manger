package com.san.kir.manger.components.download_manager

import com.san.kir.manger.room.RoomDB
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus
import com.san.kir.manger.utils.JobContext

class DownloadManagerDelegateImpl(
    private val uiJob: JobContext,
    private val job: JobContext,
    private val listener: DownloadListener,
    private val iteratorProcessor: IteratorProcessor,
    dbManager: RoomDB
) : DownloadManager.Delegate {
    private val mDownloadDao = dbManager.downloadDao
    private val mStatisticDao = dbManager.statisticDao

    override fun onDownloadRemovedFromManager(item: DownloadItem) {
        job.post {
            if (iteratorProcessor.isStopped) {
                iteratorProcessor.start()
            }
        }
    }

    override fun onStarted(item: DownloadItem) {
        item.status = DownloadStatus.loading
        mDownloadDao.update(item)
        uiJob.post {
            listener.onProgress(item)
        }
    }

    override fun onProgress(item: DownloadItem) {
        mDownloadDao.update(item)
        uiJob.post {
            listener.onProgress(item)
        }
    }

    override fun onError(item: DownloadItem, cause: Throwable?) {
        item.status = DownloadStatus.error
        mDownloadDao.update(item)
        uiJob.post {
            listener.onError(item, cause)
        }
    }

    override fun onComplete(item: DownloadItem) {
        item.status = DownloadStatus.completed
        mDownloadDao.update(item)

        val stat = mStatisticDao.getItem(item.manga)
        stat.downloadSize += item.downloadSize
        stat.downloadTime += item.totalTime
        mStatisticDao.update(stat)

        uiJob.post {
            listener.onCompleted(item)
        }
    }
}
