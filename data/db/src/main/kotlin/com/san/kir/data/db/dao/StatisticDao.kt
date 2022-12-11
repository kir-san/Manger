package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.base.Statistic
import com.san.kir.data.models.extend.SimplifiedStatistic
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticDao : BaseDao<Statistic> {
    @Query("SELECT * FROM simple_statistic ORDER BY all_time DESC")
    fun loadSimpleItems(): Flow<List<SimplifiedStatistic>>

    @Query("SELECT SUM(all_time) FROM statistic")
    fun loadAllTime(): Flow<Long>

    @Query("SELECT * FROM statistic ORDER BY all_time DESC")
    fun loadItems(): Flow<List<Statistic>>

    @Query("SELECT * FROM statistic WHERE manga_id IS :mangaId")
    fun loadItemById(mangaId: Long): Flow<Statistic>

    @Query("SELECT * FROM statistic")
    suspend fun items(): List<Statistic>

    @Query("SELECT * FROM statistic WHERE id IS :itemId")
    suspend fun itemById(itemId: Long): Statistic

    @Query("SELECT * FROM statistic WHERE manga_id IS :mangaId")
    suspend fun itemByMangaId(mangaId: Long): Statistic

    @Query("SELECT id FROM statistic WHERE manga_id IS :mangaId")
    suspend fun idByMangaId(mangaId: Long): Long?

    @Query("DELETE FROM statistic WHERE id=:itemId")
    suspend fun delete(itemId: Long)
}
