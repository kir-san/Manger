package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "statistic")
@Stable
data class Statistic(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "manga_id")
    val mangaId: Long = 0L,

    @ColumnInfo(name = "all_chapters")
    val allChapters: Int = 0,

    @ColumnInfo(name = "last_chapters")
    val lastChapters: Int = 0,

    @ColumnInfo(name = "all_pages")
    val allPages: Int = 0,

    @ColumnInfo(name = "last_pages")
    val lastPages: Int = 0,

    @ColumnInfo(name = "all_time")
    val allTime: Long = 0L,

    @ColumnInfo(name = "last_time")
    val lastTime: Long = 0L,

    @ColumnInfo(name = "max_speed")
    val maxSpeed: Int = 0,

    @ColumnInfo(name = "download_size")
    val downloadSize: Long = 0L,

    @ColumnInfo(name = "last_download_size")
    val lastDownloadSize: Long = 0L,

    @ColumnInfo(name = "download_time")
    val downloadTime: Long = 0L,

    @ColumnInfo(name = "last_download_time")
    val lastDownloadTime: Long = 0L,

    @ColumnInfo(name = "opened_times")
    val openedTimes: Int = 0,
) : Parcelable
