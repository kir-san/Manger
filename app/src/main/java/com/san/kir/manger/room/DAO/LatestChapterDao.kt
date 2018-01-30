package com.san.kir.manger.room.DAO

import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.LatestChapter
import com.san.kir.manger.room.models.action
import com.san.kir.manger.room.models.isRead
import com.san.kir.manger.utils.CHAPTER_STATUS
import kotlinx.coroutines.experimental.async

@Dao
interface LatestChapterDao : BaseDao<LatestChapter> {
    @Query("SELECT * FROM latestChapters ORDER BY id DESC")
    fun loadLatestChapters(): DataSource.Factory<Int, LatestChapter>

    @Query("SELECT * FROM latestChapters")
    fun load(): List<LatestChapter>
}

fun LatestChapterDao.clearHistoryDownload() = async {
    load().filter { it.action == CHAPTER_STATUS.DELETE }.forEach { delete(it) }
}

fun LatestChapterDao.clearHistoryRead() = async {
    load().filter { it.isRead.await() }.forEach { delete(it) }
}

fun LatestChapterDao.clearHistory() = async {
    load().forEach { delete(it) }
}

fun LatestChapterDao.downloadNewChapters() = async {
    load().filter { !it.isRead.await() }
            .filter { it.action == CHAPTER_STATUS.DOWNLOADABLE }
}

fun LatestChapterDao.hasNewChapters() = async {
    load().filter { !it.isRead.await() }
            .any { it.action == CHAPTER_STATUS.DOWNLOADABLE }
}

fun LatestChapterDao.loadPagedLatestChapters() =
        LivePagedListBuilder(loadLatestChapters(), 20).build()
