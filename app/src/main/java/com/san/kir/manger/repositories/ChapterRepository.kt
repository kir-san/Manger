package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.manger.data.room.entities.Chapter
import com.san.kir.data.db.getDatabase

class ChapterRepository(context: Context) {
    private val db = com.san.kir.data.db.getDatabase(context)
    private val mChapterDao = db.chapterDao

    suspend fun getItems() = mChapterDao.getItems()
    suspend fun getItems(mangaUnic: String) = mChapterDao.getItemsWhereManga(mangaUnic)
    suspend fun getItem(site: String) = mChapterDao.getItemWhereLink(site)

    suspend fun insert(vararg chapter: Chapter) = mChapterDao.insert(*chapter)
    suspend fun update(vararg chapter: Chapter) = mChapterDao.update(*chapter)
    suspend fun delete(vararg chapter: Chapter) = mChapterDao.delete(*chapter)
    suspend fun deleteItems(manga: String) = delete(*getItems(manga).toTypedArray())
}
