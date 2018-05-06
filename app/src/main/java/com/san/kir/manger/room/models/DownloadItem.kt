package com.san.kir.manger.room.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

object DownloadColumn {
    const val tableName = "downloads"
    const val id = "id"
    const val manga = "manga"
    const val name = "name"
    const val link = "link"
    const val path = "path"
    const val totalPages = "totalPages"
    const val downloadPages = "downloadPages"
    const val totalSize = "totalSize"
    const val downloadSize = "downloadSize"
    const val totalTime = "totalTime"
    const val status = "status"
    const val order = "order"
}

@Entity(tableName = DownloadColumn.tableName)
data class DownloadItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DownloadColumn.id)
    var id: Long = 0,

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
    var totalSize: Long = 0,

    @ColumnInfo(name = DownloadColumn.downloadSize)
    var downloadSize: Long = 0,

    @ColumnInfo(name = DownloadColumn.totalTime)
    var totalTime: Long = 0,

    @ColumnInfo(name = DownloadColumn.status)
    var status: Int = DownloadStatus.unknown,

    @ColumnInfo(name = DownloadColumn.order)
    var order: Long = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(manga)
        parcel.writeString(name)
        parcel.writeString(link)
        parcel.writeString(path)
        parcel.writeInt(totalPages)
        parcel.writeInt(downloadPages)
        parcel.writeLong(totalSize)
        parcel.writeLong(downloadSize)
        parcel.writeLong(totalTime)
        parcel.writeInt(status)
        parcel.writeLong(order)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<DownloadItem> {
        override fun createFromParcel(parcel: Parcel) = DownloadItem(parcel)

        override fun newArray(size: Int): Array<DownloadItem?> = arrayOfNulls(size)
    }
}

object DownloadStatus {
    const val loading = 0
    const val queued = 1
    const val pause = 2
    const val error = 3
    const val completed = 4
    const val unknown = 5
}

fun Chapter.toDownloadItem() = DownloadItem(
    manga = manga,
    name = name,
    link = site,
    path = path
)

fun LatestChapter.toDownloadItem() = DownloadItem(
    manga = manga,
    name = name,
    link = site,
    path = path
)
