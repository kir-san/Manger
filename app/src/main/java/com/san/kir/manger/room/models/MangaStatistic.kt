package com.san.kir.manger.room.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity(tableName = MangaStatisticColumn.tableName)
data class MangaStatistic(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = MangaStatisticColumn.id)
    var id: Long = 0,

    @ColumnInfo(name = MangaStatisticColumn.manga)
    var manga:String = "",

    @ColumnInfo(name = MangaStatisticColumn.allChapters)
    var allChapters: Int = 0,

    @ColumnInfo(name = MangaStatisticColumn.lastChapters)
    var lastChapters: Int = 0,

    @ColumnInfo(name = MangaStatisticColumn.allPages)
    var allPages: Int = 0,

    @ColumnInfo(name = MangaStatisticColumn.lastPages)
    var lastPages: Int = 0,

    @ColumnInfo(name = MangaStatisticColumn.allTime)
    var allTime: Long = 0,

    @ColumnInfo(name = MangaStatisticColumn.lastTime)
    var lastTime: Long = 0,

    @ColumnInfo(name = MangaStatisticColumn.maxSpeed)
    var maxSpeed: Int = 0,

    @ColumnInfo(name = MangaStatisticColumn.downloadSize)
    var downloadSize: Long = 0,

    @ColumnInfo(name = MangaStatisticColumn.downloadTime)
    var downloadTime: Long = 0,

    @ColumnInfo(name = MangaStatisticColumn.openedTimes)
    var openedTimes: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
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
