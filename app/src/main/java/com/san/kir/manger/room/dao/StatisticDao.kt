package com.san.kir.manger.room.dao

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.MangaStatistic
import com.san.kir.manger.room.models.MangaStatisticColumn
import com.san.kir.manger.utils.sumByLong

@Dao
interface StatisticDao : BaseDao<MangaStatistic> {
    @Query("SELECT * FROM ${MangaStatisticColumn.tableName} ORDER BY ${MangaStatisticColumn.allTime} DESC")
    fun loadPagedItems(): DataSource.Factory<Int, MangaStatistic>

    @Query("SELECT * FROM ${MangaStatisticColumn.tableName} ORDER BY ${MangaStatisticColumn.allTime} DESC")
    fun loadLivedItems(): LiveData<List<MangaStatistic>>

    @Query("SELECT * FROM ${MangaStatisticColumn.tableName}")
    fun loadItems(): List<MangaStatistic>

    @Query("SELECT * FROM ${MangaStatisticColumn.tableName} WHERE ${MangaStatisticColumn.manga} IS :unic")
    fun loadItem(unic: String): MangaStatistic
}

fun StatisticDao.loadPagedStatisticItems() =
    LivePagedListBuilder(loadPagedItems(), 20).build()

fun StatisticDao.loadAllTime(): LiveData<Long> =
    Transformations.map(loadLivedItems()) { list -> list.sumByLong { it.allTime } }

