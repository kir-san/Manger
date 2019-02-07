package com.san.kir.manger.room.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.DownloadColumn
import com.san.kir.manger.room.models.DownloadItem

@Dao
interface DownloadDao : BaseDao<DownloadItem> {
    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "ORDER BY ${DownloadColumn.status}, `${DownloadColumn.order}`"
    )
    fun loadItems(): LiveData<List<DownloadItem>>

    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "WHERE ${DownloadColumn.status} IS :status " +
                "ORDER BY `${DownloadColumn.order}`"
    )
    fun getItems(status: Int): List<DownloadItem>

    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "WHERE ${DownloadColumn.status} IS :status1 " +
                "AND ${DownloadColumn.status} IS :status2 " +
                "ORDER BY `${DownloadColumn.order}`"
    )
    fun loadItems(status1: Int, status2: Int): LiveData<List<DownloadItem>>

    @Query("SELECT * FROM ${DownloadColumn.tableName}")
    fun getItems(): List<DownloadItem>

    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "WHERE ${DownloadColumn.link} IS :link"
    )
    fun getItem(link: String): DownloadItem?

    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "WHERE ${DownloadColumn.link} IS :link"
    )
    fun loadItem(link: String): LiveData<DownloadItem?>
}

