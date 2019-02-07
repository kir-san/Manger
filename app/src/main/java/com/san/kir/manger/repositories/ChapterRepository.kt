package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.room.models.Chapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChapterRepository(context: Context) {
    private val db = getDatabase(context)
    private val mChapterDao = db.chapterDao

    fun getItems(mangaUnic: String): List<Chapter> {
        return mChapterDao.getItems(mangaUnic)
    }

    fun getItem(site: String): Chapter? {
        return mChapterDao.getItem(site)
    }

    fun getItemsNotReadAsc(mangaUnic: String): List<Chapter> {
        return mChapterDao.getItemsNotReadAsc(mangaUnic)
    }

    fun getItemsAsc(mangaUnic: String): List<Chapter> {
        return mChapterDao.getItemsAsc(mangaUnic)
    }

    fun insert(vararg chapter: Chapter) = GlobalScope.launch { mChapterDao.insert(*chapter) }
    fun update(vararg chapter: Chapter) = GlobalScope.launch { mChapterDao.update(*chapter) }
    fun delete(vararg chapter: Chapter) = GlobalScope.launch { mChapterDao.delete(*chapter) }

    fun countNotRead(mangaUnic: String): Int {
        return mChapterDao.getItems(mangaUnic).filter { !it.isRead }.size
    }

    fun count(manga: String): Int {
        return getItems(manga).size
    }

    fun deleteItems(manga: String): Job {
        return delete(*getItems(manga).toTypedArray())
    }
}
