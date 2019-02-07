package com.san.kir.manger.room.dao

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.MangaStatistic
import com.san.kir.manger.room.models.MangaStatisticColumn

@Dao
interface StatisticDao : BaseDao<MangaStatistic> {
    @Query("SELECT * FROM ${MangaStatisticColumn.tableName} ORDER BY ${MangaStatisticColumn.allTime} DESC")
    fun pagedItems(): DataSource.Factory<Int, MangaStatistic>

    @Query("SELECT * FROM ${MangaStatisticColumn.tableName} ORDER BY ${MangaStatisticColumn.allTime} DESC")
    fun loadItems(): LiveData<List<MangaStatistic>>

    @Query("SELECT * FROM ${MangaStatisticColumn.tableName}")
    fun getItems(): List<MangaStatistic>

    @Query("SELECT * FROM ${MangaStatisticColumn.tableName} WHERE ${MangaStatisticColumn.manga} IS :unic")
    fun getItem(unic: String): MangaStatistic
}
