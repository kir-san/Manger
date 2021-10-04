package com.san.kir.manger.components.download_manager

import com.san.kir.manger.room.dao.DownloadDao
import com.san.kir.manger.room.dao.StatisticDao
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.utils.JobContext
import com.san.kir.manger.utils.enums.DownloadStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManagerDelegateImpl @Inject constructor(
    private val job: JobContext,
    private val listener: DownloadListener,
    private val iteratorProcessor: IteratorProcessor,
    private val downloadDao: DownloadDao,
    private val statisticDao: StatisticDao,
) : DownloadManager.Delegate {

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
            downloadDao.update(item)
        }
        listener.onProgress(item)
    }

    override fun onProgress(item: DownloadItem) {
        job.post {
            downloadDao.update(item)
        }
        listener.onProgress(item)
    }

    override fun onError(item: DownloadItem, cause: Throwable?) {
        item.status = DownloadStatus.pause
        item.isError = true
        job.post {
            downloadDao.update(item)
        }
        listener.onError(item, cause)
    }

    override suspend fun onComplete(item: DownloadItem) {
        item.status = DownloadStatus.completed
        job.post {
            downloadDao.update(item)
        }

        val stat = statisticDao.getItem(item.manga)
        stat.downloadSize = stat.downloadSize.plus(item.downloadSize)
        stat.downloadTime = stat.downloadTime.plus(item.totalTime)
        job.post {
            statisticDao.update(stat)
        }
        listener.onCompleted(item)
    }
}
