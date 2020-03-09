package com.san.kir.manger.repositories

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.paging.LivePagedListBuilder
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.action
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.utils.enums.ChapterStatus

class ChapterRepository(context: Context) {
    private val db = getDatabase(context)
    private val mChapterDao = db.chapterDao

    suspend fun getItems() = mChapterDao.getItems()
    suspend fun getItems(mangaUnic: String) = mChapterDao.getItems(mangaUnic)
    suspend fun getItem(site: String) = mChapterDao.getItem(site)
    suspend fun getItemsNotReadAsc(mangaUnic: String) = mChapterDao.getItemsNotReadAsc(mangaUnic)
    suspend fun getItemsAsc(mangaUnic: String) = mChapterDao.getItemsAsc(mangaUnic)
    suspend fun countNotRead(mangaUnic: String) =
        mChapterDao.getItems(mangaUnic).filter { !it.isRead }.size

    suspend fun newChapters() = mChapterDao.getItems()
        .filter { it.isInUpdate }
        .filter { !it.isRead }
        .filter { it.action == ChapterStatus.DOWNLOADABLE }

    suspend fun insert(vararg chapter: Chapter) = mChapterDao.insert(*chapter)
    suspend fun update(vararg chapter: Chapter) = mChapterDao.update(*chapter)
    suspend fun delete(vararg chapter: Chapter) = mChapterDao.delete(*chapter)
    suspend fun deleteItems(manga: String) = delete(*getItems(manga).toTypedArray())

    @Suppress("unused")
    suspend fun count(manga: String): Int {
        return getItems(manga).size
    }

    fun loadInUpdateItems() = mChapterDao.loadInUpdateItems()
    fun pagedItems() = LivePagedListBuilder(mChapterDao.pagedItems(), 50).build().asFlow()
}
