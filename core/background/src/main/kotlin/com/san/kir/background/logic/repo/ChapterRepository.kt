package com.san.kir.background.logic.repo

import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.StatisticDao
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.parsing.SiteCatalogsManager
import javax.inject.Inject

class ChapterRepository @Inject constructor(
    private val manager: SiteCatalogsManager,
    private val chapterDao: ChapterDao,
    private val statisticDao: StatisticDao,
) {
    suspend fun chapter(chapterId: Long) = withIoContext { chapterDao.itemById(chapterId) }
    suspend fun update(chapter: Chapter) = withIoContext { chapterDao.update(chapter) }
    suspend fun addToQueue(chapterId: Long) = withIoContext { chapterDao.setQueueStatus(chapterId) }
    suspend fun pages(chapter: Chapter) = withIoContext { manager.pages(chapter) }
    suspend fun pausedChapters() = withIoContext { chapterDao.itemsByStatus(DownloadState.PAUSED) }
    suspend fun pauseChapters(chapterIds: List<Long>) =
        withIoContext { chapterDao.updateStatus(chapterIds, DownloadState.PAUSED) }

    suspend fun updateStatistic(chapter: Chapter) = withIoContext {
        val stat = statisticDao.itemByMangaId(chapter.mangaId)
        statisticDao.update(
            stat.copy(
                downloadSize = stat.downloadSize + chapter.downloadSize,
                downloadTime = stat.downloadTime + chapter.downloadTime,
            )
        )
    }
}
