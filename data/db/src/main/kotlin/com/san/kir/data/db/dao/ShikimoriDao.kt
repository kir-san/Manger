package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.base.ShikiManga
import com.san.kir.data.models.extend.SimplefiedMangaWithChapterCounts
import kotlinx.coroutines.flow.Flow

@Dao
interface ShikimoriDao : BaseDao<ShikiManga> {
    @Query("SELECT * FROM ${ShikiManga.tableName}")
    fun items(): Flow<List<ShikiManga>>

    @Query("SELECT * FROM ${ShikiManga.tableName} " +
            "WHERE ${ShikiManga.Col.id} IS :targetID")
    suspend fun item(targetID: Long): ShikiManga?

    @Query("SELECT * FROM ${ShikiManga.tableName} " +
            "WHERE ${ShikiManga.Col.id} IS :targetID")
    fun loadItem(targetID: Long): Flow<ShikiManga?>

    @Query("SELECT * FROM ${ShikiManga.tableName} " +
            "WHERE ${ShikiManga.Col.libMangaId} IS :libId")
    suspend fun itemWhereLibId(libId: Long): ShikiManga?

    @Query("DELETE FROM ${ShikiManga.tableName}")
    suspend fun clearAll()

    @Query("SELECT * FROM ${SimplefiedMangaWithChapterCounts.viewName}")
    fun loadLibraryItems(): Flow<List<SimplefiedMangaWithChapterCounts>>
}

