package com.san.kir.manger.room.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.LatestChapter
import com.san.kir.manger.room.models.action
import com.san.kir.manger.room.models.isRead
import com.san.kir.manger.utils.ChapterStatus

@Dao
interface LatestChapterDao : BaseDao<LatestChapter> {
    @Query("SELECT * FROM latestChapters ORDER BY id DESC")
    fun loadItems(): LiveData<List<LatestChapter>>

    @Query("SELECT * FROM latestChapters")
    fun getItems(): List<LatestChapter>

    @Query("SELECT * FROM latestChapters WHERE site IS :link")
    fun getItemsWhereLink(link: String): List<LatestChapter>

    @Query("SELECT * FROM latestChapters WHERE manga IS :manga")
    fun getItemsWhereManga(manga: String): List<LatestChapter>
}

fun LatestChapterDao.deleteItems(manga: String) =
    delete(*getItemsWhereManga(manga).toTypedArray())

fun LatestChapterDao.clearDownloaded() =
    GlobalScope.launch(Dispatchers.Default) {
        getItems()
            .filter { it.action == ChapterStatus.DELETE }
            .forEach { delete(it) }
    }

fun LatestChapterDao.clearRead() =
    GlobalScope.launch(Dispatchers.Default) {
        getItems()
            .filter { it.isRead() }.forEach { delete(it) }
    }

fun LatestChapterDao.clearAll() =
    GlobalScope.launch(Dispatchers.Default) {
        getItems()
            .forEach { delete(it) }
    }

fun LatestChapterDao.getNewChapters() =
    getItems()
        .filter { !it.isRead() }
        .filter { it.action == ChapterStatus.DOWNLOADABLE }


fun LatestChapterDao.hasNewChapters() =
    getItems()
        .filter { !it.isRead() }
        .any { it.action == ChapterStatus.DOWNLOADABLE }
