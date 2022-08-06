package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.data.db.RoomDB
import com.san.kir.data.models.base.Chapter

class ChapterRepository(context: Context) {
    private val db = RoomDB.getDatabase(context)
    private val mChapterDao = db.chapterDao()

    private suspend fun getItems(mangaUnic: String) = mChapterDao.getItemsWhereManga(mangaUnic)

    suspend fun insert(vararg chapter: Chapter) = mChapterDao.insert(*chapter)
    suspend fun update(vararg chapter: Chapter) = mChapterDao.update(*chapter)
    suspend fun delete(vararg chapter: Chapter) = mChapterDao.delete(*chapter)
    suspend fun deleteItems(manga: String) = delete(*getItems(manga).toTypedArray())
}
