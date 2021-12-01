package com.san.kir.manger.components.download_manager

import com.san.kir.manger.data.room.dao.ChapterDao
import com.san.kir.manger.data.room.dao.StatisticDao
import com.san.kir.manger.data.room.entities.Chapter
import com.san.kir.manger.utils.JobContext
import com.san.kir.manger.utils.enums.DownloadState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManagerDelegateImpl @Inject constructor(
    private val job: JobContext,
    private val listener: DownloadListener,
    private val iteratorProcessor: IteratorProcessor,
    private val chapterDao: ChapterDao,
    private val statisticDao: StatisticDao,
) : DownloadManager.Delegate {

    override fun onDownloadRemovedFromManager(item: Chapter) {
        job.post {
            if (iteratorProcessor.isStopped) {
                iteratorProcessor.start()
            }
        }
    }

    override fun onStarted(item: Chapter) {
        item.status = DownloadState.LOADING
        job.post {
            chapterDao.update(item)
        }
        listener.onProgress(item)
    }

    override fun onProgress(item: Chapter) {
        job.post {
            chapterDao.update(item)
        }
        listener.onProgress(item)
    }

    override fun onError(item: Chapter, cause: Throwable?) {
        item.status = DownloadState.PAUSED
        item.isError = true
        job.post {
            chapterDao.update(item)
        }
        listener.onError(item, cause)
    }

    override suspend fun onComplete(item: Chapter) {
        item.status = DownloadState.COMPLETED
        job.post {
            chapterDao.update(item)
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
