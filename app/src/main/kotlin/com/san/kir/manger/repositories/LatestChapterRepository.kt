package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.data.db.RoomDB
import com.san.kir.data.models.base.LatestChapter

class LatestChapterRepository(context: Context) {
    private val mLatestDao = RoomDB.getDatabase(context).latestChapterDao

    suspend fun getItems() = mLatestDao.getItems()
    suspend fun delete(vararg chapter: LatestChapter) = mLatestDao.delete(*chapter)
    suspend fun deleteAll() = mLatestDao.deleteAll()
}
