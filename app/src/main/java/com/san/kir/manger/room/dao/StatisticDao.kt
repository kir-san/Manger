package com.san.kir.manger.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.room.entities.MangaStatisticColumn
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticDao : BaseDao<MangaStatistic> {
    @Query(
        "SELECT * FROM ${MangaStatisticColumn.tableName} " +
                "ORDER BY ${MangaStatisticColumn.allTime} DESC"
    )
    fun allItemsByAllTime(): PagingSource<Int, MangaStatistic>

    @Query("SELECT * FROM ${MangaStatisticColumn.tableName} ORDER BY ${MangaStatisticColumn.allTime} DESC")
    fun loadItems(): Flow<List<MangaStatistic>>

    @Query("SELECT * FROM ${MangaStatisticColumn.tableName}")
    suspend fun getItems(): List<MangaStatistic>

    @Query("SELECT * FROM ${MangaStatisticColumn.tableName} WHERE ${MangaStatisticColumn.manga} IS :unic")
    suspend fun getItem(unic: String): MangaStatistic
}
