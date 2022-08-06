package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.data.models.columns.MangaStatisticColumn
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = MangaStatisticColumn.tableName)
data class Statistic(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = MangaStatisticColumn.id)
    var id: Long = 0L,

    @ColumnInfo(name = MangaStatisticColumn.manga)
    var manga: String = "",

    @ColumnInfo(name = MangaStatisticColumn.allChapters)
    var allChapters: Int = 0,

    @ColumnInfo(name = MangaStatisticColumn.lastChapters)
    var lastChapters: Int = 0,

    @ColumnInfo(name = MangaStatisticColumn.allPages)
    var allPages: Int = 0,

    @ColumnInfo(name = MangaStatisticColumn.lastPages)
    var lastPages: Int = 0,

    @ColumnInfo(name = MangaStatisticColumn.allTime)
    var allTime: Long = 0L,

    @ColumnInfo(name = MangaStatisticColumn.lastTime)
    var lastTime: Long = 0L,

    @ColumnInfo(name = MangaStatisticColumn.maxSpeed)
    var maxSpeed: Int = 0,

    @ColumnInfo(name = MangaStatisticColumn.downloadSize)
    var downloadSize: Long = 0L,

    @ColumnInfo(name = MangaStatisticColumn.lastDownloadSize)
    var lastDownloadSize: Long = 0L,

    @ColumnInfo(name = MangaStatisticColumn.downloadTime)
    var downloadTime: Long = 0L,

    @ColumnInfo(name = MangaStatisticColumn.lastDownloadTime)
    var lastDownloadTime: Long = 0L,

    @ColumnInfo(name = MangaStatisticColumn.openedTimes)
    var openedTimes: Int = 0,
) : Parcelable
