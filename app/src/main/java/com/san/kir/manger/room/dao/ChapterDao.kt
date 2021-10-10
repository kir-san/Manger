package com.san.kir.manger.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.ChaptersColumn
import com.san.kir.manger.utils.enums.DownloadState
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao : BaseDao<Chapter> {
    @Query("SELECT * FROM ${ChaptersColumn.tableName}")
    suspend fun getItems(): List<Chapter>

    @Query(
        "SELECT * FROM ${ChaptersColumn.tableName} " +
                "WHERE ${ChaptersColumn.manga} IS :manga"
    )
    suspend fun getItemsWhereManga(manga: String): List<Chapter>

    @Query(
        "SELECT * FROM ${ChaptersColumn.tableName} " +
                "WHERE ${ChaptersColumn.manga} IS :manga"
    )
    fun loadItems(manga: String): Flow<List<Chapter>>

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

    @Query("SELECT * FROM chapters WHERE manga IS :manga ORDER BY id ASC")
    suspend fun getItemsAsc(manga: String): List<Chapter>

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
}

