package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.core.support.DownloadState
import com.san.kir.data.models.Chapter
import com.san.kir.data.models.columns.ChaptersColumn
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao : BaseDao<Chapter> {
    @Query("SELECT * FROM ${ChaptersColumn.tableName}")
    suspend fun items(): List<Chapter>

    @Query("SELECT ${ChaptersColumn.manga} FROM ${ChaptersColumn.tableName} " +
            "WHERE ${ChaptersColumn.id} IS :chapterID")
    suspend fun getMangaName(chapterID: Long): String

    @Query("SELECT * FROM ${ChaptersColumn.tableName} " +
            "WHERE ${ChaptersColumn.id} IS :chapterID")
    suspend fun itemWhereId(chapterID: Long): Chapter

    @Query(
        "SELECT * FROM ${ChaptersColumn.tableName} " +
                "WHERE ${ChaptersColumn.manga} IS :manga"
    )
    suspend fun getItemsWhereManga(manga: String): List<Chapter>

    @Query(
        "SELECT * FROM ${ChaptersColumn.tableName} " +
                "WHERE ${ChaptersColumn.manga} IS :manga"
    )
    fun loadItemsWhereManga(manga: String): Flow<List<Chapter>>

    @Query(
        "SELECT COUNT(*) FROM ${ChaptersColumn.tableName} " +
                "WHERE ${ChaptersColumn.manga} IS :manga " +
                "AND ${ChaptersColumn.isRead} IS 0"
    )
    fun loadCountNotReadItemsWhereManga(manga: String): Flow<Int>

    @Query(
        "SELECT * FROM ${ChaptersColumn.tableName} " +
                "WHERE ${ChaptersColumn.site} IS :link"
    )
    suspend fun getItemWhereLink(link: String): Chapter?

    @Query(
        "SELECT * FROM ${ChaptersColumn.tableName} " +
                "WHERE ${ChaptersColumn.status} IS :status " +
                "ORDER BY `${ChaptersColumn.order}`"
    )
    suspend fun getItemsWhereStatus(status: DownloadState): List<Chapter>

    @Query(
        "SELECT * FROM ${ChaptersColumn.tableName} " +
                "WHERE ${ChaptersColumn.error} IS 0 " +
                "ORDER BY `${ChaptersColumn.order}`"
    )
    suspend fun getErrorItems(): List<Chapter>

    @Query(
        "SELECT * FROM ${ChaptersColumn.tableName} " +
                "WHERE ${ChaptersColumn.manga} IS :manga " +
                "AND ${ChaptersColumn.isRead} IS 0 " +
                "ORDER BY ${ChaptersColumn.id} ASC"
    )
    suspend fun getItemsNotReadAsc(manga: String): List<Chapter>

    @Query(
        "SELECT * FROM ${ChaptersColumn.tableName} " +
                "WHERE ${ChaptersColumn.isInUpdate} IS 1 " +
                "ORDER BY ${ChaptersColumn.id} DESC"
    )
    fun loadAllItems(): Flow<List<Chapter>>

    @Query(
        "SELECT * FROM ${ChaptersColumn.tableName} " +
                "WHERE `${ChaptersColumn.status}` IS NOT :status " +
                "ORDER BY ${ChaptersColumn.status},`${ChaptersColumn.order}`"
    )
    fun loadDownloadItemsWhereStatusNot(status: DownloadState = DownloadState.UNKNOWN): Flow<List<Chapter>>
}

