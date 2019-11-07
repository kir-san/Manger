package com.san.kir.manger.room.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.room.entities.MangaStatisticColumn

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
