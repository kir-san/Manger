package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.ShikiDbManga
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import kotlinx.coroutines.flow.Flow

@Dao
interface ShikimoriDao : BaseDao<ShikiDbManga> {
    @Query("SELECT * FROM ${ShikiDbManga.tableName}")
    fun loadItems(): Flow<List<ShikiDbManga>>

    @Query("SELECT * FROM ${ShikiDbManga.tableName} " +
            "WHERE ${ShikiDbManga.Col.targetId} IS :targetID")
    suspend fun itemByTargetId(targetID: Long): ShikiDbManga?

    @Query("SELECT * FROM ${ShikiDbManga.tableName} " +
            "WHERE ${ShikiDbManga.Col.targetId} IS :targetID")
    fun loadItemByTargetId(targetID: Long): Flow<ShikiDbManga?>

    @Query("SELECT * FROM ${ShikiDbManga.tableName} " +
            "WHERE ${ShikiDbManga.Col.libMangaId} IS :libId")
    suspend fun itemByLibId(libId: Long): ShikiDbManga?

    @Query("SELECT * FROM ${ShikiDbManga.tableName} " +
            "WHERE ${ShikiDbManga.Col.libMangaId} IS :libId")
    fun loadItemByLibId(libId: Long): Flow<ShikiDbManga?>

    @Query("DELETE FROM ${ShikiDbManga.tableName}")
    suspend fun clearAll()

    @Query(
        "SELECT * FROM ${SimplifiedMangaWithChapterCounts.viewName} " +
                "ORDER BY ${Manga.Col.name}"
    )
    fun loadLibraryItems(): Flow<List<SimplifiedMangaWithChapterCounts>>

    @Query("SELECT * FROM ${SimplifiedMangaWithChapterCounts.viewName} " +
            "WHERE ${Manga.Col.id} IS :id")
    fun loadLibraryItemById(id: Long): Flow<SimplifiedMangaWithChapterCounts>

    @Query("DELETE FROM ${ShikiDbManga.tableName} " +
                   "WHERE ${ShikiDbManga.Col.targetId} IS :targetID")
    suspend fun removeByTargetId(targetID: Long)

    @Query("UPDATE ${ShikiDbManga.tableName} " +
                   "SET ${ShikiDbManga.Col.libMangaId} = :libId " +
                   "WHERE ${ShikiDbManga.Col.targetId} IS :targetID")
    suspend fun updateLibIdByTargetId(targetID: Long, libId: Long)
}

