package com.san.kir.manger.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.san.kir.manger.data.room.columns.DownloadColumn
import com.san.kir.manger.data.room.entities.DownloadItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao : BaseDao<DownloadItem> {
    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "ORDER BY ${DownloadColumn.status}, `${DownloadColumn.order}`"
    )
    fun loadItems(): LiveData<List<DownloadItem>>

    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "ORDER BY ${DownloadColumn.status}, `${DownloadColumn.order}`"
    )
    fun flowItems(): Flow<List<DownloadItem>>

    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "ORDER BY ${DownloadColumn.status}, `${DownloadColumn.order}`"
    )
    suspend fun items(): List<DownloadItem>

    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "WHERE ${DownloadColumn.status} IS :status " +
                "ORDER BY `${DownloadColumn.order}`"
    )
    suspend fun getItems(status: Int): List<DownloadItem>

    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "WHERE ${DownloadColumn.status} IS :status1 " +
                "AND ${DownloadColumn.status} IS :status2 " +
                "ORDER BY `${DownloadColumn.order}`"
    )
    fun loadItems(status1: Int, status2: Int): LiveData<List<DownloadItem>>

    @Query("SELECT * FROM ${DownloadColumn.tableName}")
    suspend fun getItems(): List<DownloadItem>

    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "WHERE ${DownloadColumn.link} IS :link"
    )
    suspend fun getItem(link: String): DownloadItem?

    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "WHERE ${DownloadColumn.link} IS :link"
    )
    fun loadItem(link: String): Flow<DownloadItem?>
}
