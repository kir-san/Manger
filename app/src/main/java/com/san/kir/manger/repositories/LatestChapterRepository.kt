package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.manger.data.room.entities.LatestChapter
import com.san.kir.data.db.getDatabase

class LatestChapterRepository(context: Context) {
    private val mLatestDao = com.san.kir.data.db.getDatabase(context).latestChapterDao

    suspend fun getItems() = mLatestDao.getItems()
    suspend fun delete(vararg chapter: LatestChapter) = mLatestDao.delete(*chapter)
    suspend fun deleteAll() = mLatestDao.deleteAll()
}
