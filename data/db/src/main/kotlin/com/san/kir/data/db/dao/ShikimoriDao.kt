package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.ShikiManga
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
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

    @Query("SELECT * FROM ${ShikiManga.tableName} " +
            "WHERE ${ShikiManga.Col.libMangaId} IS :libId")
    fun loadItemWhereLibId(libId: Long): Flow<ShikiManga?>

    @Query("DELETE FROM ${ShikiManga.tableName}")
    suspend fun clearAll()

    @Query("SELECT * FROM ${SimplifiedMangaWithChapterCounts.viewName}")
    fun loadLibraryItems(): Flow<List<SimplifiedMangaWithChapterCounts>>

    @Query("SELECT * FROM ${SimplifiedMangaWithChapterCounts.viewName} " +
            "WHERE ${Manga.Col.id} IS :id")
    fun loadLibraryItem(id: Long): Flow<SimplifiedMangaWithChapterCounts>
}

