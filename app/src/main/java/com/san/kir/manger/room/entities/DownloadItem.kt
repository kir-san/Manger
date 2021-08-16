package com.san.kir.manger.room.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.manger.room.columns.DownloadColumn
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = DownloadColumn.tableName)
data class DownloadItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DownloadColumn.id)
    var id: Long = 0L,

    @ColumnInfo(name = DownloadColumn.manga)
    var manga: String = "",

    @ColumnInfo(name = DownloadColumn.name)
    var name: String = "",

    @ColumnInfo(name = DownloadColumn.link)
    var link: String = "",

    @ColumnInfo(name = DownloadColumn.path)
    var path: String = "",

    @ColumnInfo(name = DownloadColumn.totalPages)
    var totalPages: Int = 0,

    @ColumnInfo(name = DownloadColumn.downloadPages)
    var downloadPages: Int = 0,

    @ColumnInfo(name = DownloadColumn.totalSize)
    var totalSize: Long = 0L,

    @ColumnInfo(name = DownloadColumn.downloadSize)
    var downloadSize: Long = 0L,

    @ColumnInfo(name = DownloadColumn.totalTime)
    var totalTime: Long = 0L,

    @ColumnInfo(name = DownloadColumn.status)
    var status: Int = -1,

    @ColumnInfo(name = DownloadColumn.order)
    var order: Long = 0,

    @ColumnInfo(name = DownloadColumn.error)
    var isError: Boolean = false

) : Parcelable

fun Chapter.toDownloadItem() =
    DownloadItem(
        id = 0,
        manga = manga,
        name = name,
        link = site,
        path = path,
        totalPages = 0,
        downloadPages = 0,
        totalSize = 0,
        downloadSize = 0,
        totalTime = 0,
        status = -1,
        order = 0,
        isError = false
    )
