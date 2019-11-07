package com.san.kir.manger.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.action
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.utils.enums.ChapterStatus

class ChapterRepository(context: Context) {
    private val db = getDatabase(context)
    private val mChapterDao = db.chapterDao

    suspend fun getItems() = mChapterDao.getItems()

    suspend fun getItems(mangaUnic: String): List<Chapter> {
        return mChapterDao.getItems(mangaUnic)
    }

    suspend fun getItem(site: String): Chapter? {
        return mChapterDao.getItem(site)
    }

    suspend fun getItemsNotReadAsc(mangaUnic: String): List<Chapter> {
        return mChapterDao.getItemsNotReadAsc(mangaUnic)
    }

    suspend fun getItemsAsc(mangaUnic: String): List<Chapter> {
        return mChapterDao.getItemsAsc(mangaUnic)
    }

    suspend fun insert(vararg chapter: Chapter) = mChapterDao.insert(*chapter)
    suspend fun update(vararg chapter: Chapter) = mChapterDao.update(*chapter)
    suspend fun delete(vararg chapter: Chapter) = mChapterDao.delete(*chapter)

    suspend fun countNotRead(mangaUnic: String) =
        mChapterDao.getItems(mangaUnic).filter { !it.isRead }.size

    @Suppress("unused")
    suspend fun count(manga: String): Int {
        return getItems(manga).size
    }

    suspend fun deleteItems(manga: String): Int {
        return delete(*getItems(manga).toTypedArray())
    }

    fun loadInUpdateItems(): LiveData<List<Chapter>> {
        return mChapterDao.loadInUpdateItems()
    }

    suspend fun newChapters(): List<Chapter> {
        return mChapterDao.getItems()
            .filter { it.isInUpdate }
            .filter { !it.isRead }
            .filter { it.action == ChapterStatus.DOWNLOADABLE }
    }
}
