package com.san.kir.manger.room.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.DownloadColumn
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus

@Dao
interface DownloadDao : BaseDao<DownloadItem> {
    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "WHERE ${DownloadColumn.status} IS ${DownloadStatus.loading} " +
                "OR ${DownloadColumn.status} IS ${DownloadStatus.queued} " +
                "ORDER BY ${DownloadColumn.status}, `${DownloadColumn.order}`"
    )
    fun loadLoadingDownloads(): LiveData<List<DownloadItem>>

    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "WHERE ${DownloadColumn.status} IS ${DownloadStatus.pause} " +
                "OR ${DownloadColumn.status} IS ${DownloadStatus.error} " +
                "OR ${DownloadColumn.status} IS ${DownloadStatus.completed} " +
                "ORDER BY ${DownloadColumn.status}, `${DownloadColumn.order}`"
    )
    fun loadOtherDownloads(): LiveData<List<DownloadItem>>

    @Query("SELECT * FROM ${DownloadColumn.tableName}")
    fun loadAllDownloads(): LiveData<List<DownloadItem>>

    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "WHERE ${DownloadColumn.status} IS ${DownloadStatus.queued} " +
                "ORDER BY `${DownloadColumn.order}`"
    )
    fun getQueuedDownloads(): List<DownloadItem>

    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "WHERE ${DownloadColumn.status} IS ${DownloadStatus.error} " +
                "ORDER BY `${DownloadColumn.order}`"
    )
    fun getErrorDownloads(): List<DownloadItem>

    @Query("SELECT * FROM ${DownloadColumn.tableName}")
    fun loadItems(): List<DownloadItem>

    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "WHERE ${DownloadColumn.link} IS :link"
    )
    fun loadItem(link: String): DownloadItem?

    @Query(
        "SELECT * FROM ${DownloadColumn.tableName} " +
                "WHERE ${DownloadColumn.link} IS :link"
    )
    fun loadLivedItem(link: String): LiveData<DownloadItem?>
}

