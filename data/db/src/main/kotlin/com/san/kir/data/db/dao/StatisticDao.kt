package com.san.kir.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.MangaStatistic
import com.san.kir.data.models.columns.MangaStatisticColumn
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

    @Query("SELECT * FROM ${MangaStatisticColumn.tableName} WHERE ${MangaStatisticColumn.manga} IS :name")
    fun loadItem(name: String): Flow<MangaStatistic>

    @Query("SELECT * FROM ${MangaStatisticColumn.tableName}")
    suspend fun getItems(): List<MangaStatistic>

    @Query("SELECT * FROM ${MangaStatisticColumn.tableName} WHERE ${MangaStatisticColumn.manga} IS :name")
    suspend fun getItem(name: String): MangaStatistic
}
