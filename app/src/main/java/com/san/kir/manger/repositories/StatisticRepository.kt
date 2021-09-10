package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.room.getDatabase

class StatisticRepository(context: Context) {
    private val db = getDatabase(context)
    private val mStatisticDao = db.statisticDao

    suspend fun getItem(unic: String) = mStatisticDao.getItem(unic)

    suspend fun update(vararg site: MangaStatistic) = mStatisticDao.update(*site)
    suspend fun insert(vararg site: MangaStatistic) = mStatisticDao.insert(*site)
    suspend fun delete(vararg site: MangaStatistic) = mStatisticDao.delete(*site)

}
