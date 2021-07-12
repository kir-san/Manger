package com.san.kir.manger.components.download_manager

import com.san.kir.manger.room.RoomDB
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.utils.JobContext
import com.san.kir.manger.utils.enums.DownloadStatus

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
        job.post {
            mDownloadDao.update(item)
        }
        uiJob.post {
            listener.onProgress(item)
        }
    }

    override fun onProgress(item: DownloadItem) {
        job.post {
            mDownloadDao.update(item)
        }
        uiJob.post {
            listener.onProgress(item)
        }
    }

    override fun onError(item: DownloadItem, cause: Throwable?) {
        item.status = DownloadStatus.pause
        item.isError = true
        job.post {
            mDownloadDao.update(item)
        }
        uiJob.post {
            listener.onError(item, cause)
        }
    }

    override suspend fun onComplete(item: DownloadItem) {
        item.status = DownloadStatus.completed
        job.post {
            mDownloadDao.update(item)
        }

        val stat = mStatisticDao.getItem(item.manga)
        stat.downloadSize = stat.downloadSize.plus(item.downloadSize)
        stat.downloadTime = stat.downloadTime.plus(item.totalTime)
        job.post {
            mStatisticDao.update(stat)
        }
        uiJob.post {
            listener.onCompleted(item)
        }
    }
}
