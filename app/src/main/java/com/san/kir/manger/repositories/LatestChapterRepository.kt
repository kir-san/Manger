package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.manger.room.entities.LatestChapter
import com.san.kir.manger.room.getDatabase

class LatestChapterRepository(context: Context) {
    private val mLatestDao = getDatabase(context).latestChapterDao

    suspend fun getItems() = mLatestDao.getItems()
    suspend fun delete(vararg chapter: LatestChapter) = mLatestDao.delete(*chapter)
    suspend fun deleteAll() = mLatestDao.deleteAll()
}
