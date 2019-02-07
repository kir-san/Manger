package com.san.kir.manger.repositories

import android.arch.lifecycle.LiveData
import android.content.Context
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.room.models.LatestChapter
import com.san.kir.manger.room.models.action
import com.san.kir.manger.utils.enums.ChapterStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LatestChapterRepository(context: Context) {
    private val db = getDatabase(context)
    private val mLatestDao = db.latestChapterDao
    private val mChapterDao = db.chapterDao

    fun loadItems(): LiveData<List<LatestChapter>> {
        return mLatestDao.loadItems()
    }

    fun getItems(): List<LatestChapter> {
        return mLatestDao.getItems()
    }

    fun getItemsWhereLink(site: String): List<LatestChapter> {
        return mLatestDao.getItemsWhereLink(site)
    }

    fun getItemsWhereManga(manga: String): List<LatestChapter> {
        return mLatestDao.getItemsWhereManga(manga)
    }

    fun insert(vararg chapter: LatestChapter) = GlobalScope.launch { mLatestDao.insert(*chapter) }
    fun update(vararg chapter: LatestChapter) = GlobalScope.launch { mLatestDao.update(*chapter) }
    fun delete(vararg chapter: LatestChapter) = GlobalScope.launch { mLatestDao.delete(*chapter) }

    fun deleteItems(manga: String): Job {
        return delete(*getItemsWhereManga(manga).toTypedArray())
    }

    fun clearDownloaded(): Job {
        return delete(*getItems().filter { it.action == ChapterStatus.DELETE }.toTypedArray())
    }

    fun clearAll(): Job {
        return GlobalScope.launch { mLatestDao.deleteAll() }
    }

    fun getNewChapters(): List<LatestChapter> {
        return mLatestDao.getItems()
            .filter { !isRead(it) }
            .filter { it.action == ChapterStatus.DOWNLOADABLE }
    }

    fun hasNewChapters(): Boolean {
        return getItems()
            .filter { !isRead(it) }
            .any { it.action == ChapterStatus.DOWNLOADABLE }
    }

    fun isRead(chapter: LatestChapter): Boolean {
        return try {
            mChapterDao.getItems(chapter.manga)
                .first { it.name == chapter.name }
                .isRead
        } catch (ex: NoSuchElementException) {
            false
        }
    }
}
