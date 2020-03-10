package com.san.kir.manger.room.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = MangaStatisticColumn.tableName)
data class MangaStatistic(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = MangaStatisticColumn.id)
    var id: Long,

    @ColumnInfo(name = MangaStatisticColumn.manga)
    var manga: String,

    @ColumnInfo(name = MangaStatisticColumn.allChapters)
    var allChapters: Int,

    @ColumnInfo(name = MangaStatisticColumn.lastChapters)
    var lastChapters: Int,

    @ColumnInfo(name = MangaStatisticColumn.allPages)
    var allPages: Int,

    @ColumnInfo(name = MangaStatisticColumn.lastPages)
    var lastPages: Int,

    @ColumnInfo(name = MangaStatisticColumn.allTime)
    var allTime: Long,

    @ColumnInfo(name = MangaStatisticColumn.lastTime)
    var lastTime: Long,

    @ColumnInfo(name = MangaStatisticColumn.maxSpeed)
    var maxSpeed: Int,

    @ColumnInfo(name = MangaStatisticColumn.downloadSize)
    var downloadSize: Long,

    @ColumnInfo(name = MangaStatisticColumn.downloadTime)
    var downloadTime: Long,

    @ColumnInfo(name = MangaStatisticColumn.openedTimes)
    var openedTimes: Int
) : Parcelable {
    @Ignore
    constructor(
        manga: String = "",
        allChapters: Int = 0,
        lastChapters: Int = 0,
        allPages: Int = 0,
        lastPages: Int = 0,
        allTime: Long = 0,
        lastTime: Long = 0,
        maxSpeed: Int = 0,
        downloadSize: Long = 0,
        downloadTime: Long = 0,
        openedTimes: Int = 0
    ) : this(
        0,
        manga,
        allChapters,
        lastChapters,
        allPages,
        lastPages,
        allTime,
        lastTime,
        maxSpeed,
        downloadSize,
        downloadTime,
        openedTimes
    )

    @Ignore
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(manga)
        parcel.writeInt(allChapters)
        parcel.writeInt(lastChapters)
        parcel.writeInt(allPages)
        parcel.writeInt(lastPages)
        parcel.writeLong(allTime)
        parcel.writeLong(lastTime)
        parcel.writeInt(maxSpeed)
        parcel.writeLong(downloadSize)
        parcel.writeLong(downloadTime)
        parcel.writeInt(openedTimes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MangaStatistic> {
        override fun createFromParcel(parcel: Parcel): MangaStatistic {
            return MangaStatistic(parcel)
        }

        override fun newArray(size: Int): Array<MangaStatistic?> {
            return arrayOfNulls(size)
        }
    }
}

object MangaStatisticColumn {
    const val tableName = "statistic"

    const val id = "id"
    const val manga = "manga"
    const val allChapters = "all_chapters"
    const val lastChapters = "last_chapters"
    const val allPages = "all_pages"
    const val lastPages = "last_pages"
    const val allTime = "all_time"
    const val lastTime = "last_time"
    const val maxSpeed = "max_speed"
    const val downloadSize = "download_size"
    const val downloadTime = "download_time"
    const val openedTimes = "opened_times"
}
