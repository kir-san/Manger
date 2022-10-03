package com.san.kir.core.download

import com.san.kir.core.support.DownloadState
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.StatisticDao
import com.san.kir.data.models.base.Chapter
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

        val stat = statisticDao.itemByMangaId(item.id)

        job.post {
            statisticDao.update(
                stat.copy(
                    downloadSize = stat.downloadSize + item.downloadSize,
                    downloadTime = stat.downloadTime + item.totalTime
                )
            )
        }
        listener.onCompleted(item)
    }
}
