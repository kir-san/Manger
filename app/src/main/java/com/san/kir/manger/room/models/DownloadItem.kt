package com.san.kir.manger.room.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import com.san.kir.manger.utils.enums.DownloadColumn
import com.san.kir.manger.utils.enums.DownloadStatus

@Entity(tableName = DownloadColumn.tableName)
data class DownloadItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DownloadColumn.id)
    var id: Long,

    @ColumnInfo(name = DownloadColumn.manga)
    var manga: String,

    @ColumnInfo(name = DownloadColumn.name)
    var name: String,

    @ColumnInfo(name = DownloadColumn.link)
    var link: String,

    @ColumnInfo(name = DownloadColumn.path)
    var path: String,

    @ColumnInfo(name = DownloadColumn.totalPages)
    var totalPages: Int,

    @ColumnInfo(name = DownloadColumn.downloadPages)
    var downloadPages: Int,

    @ColumnInfo(name = DownloadColumn.totalSize)
    var totalSize: Long,

    @ColumnInfo(name = DownloadColumn.downloadSize)
    var downloadSize: Long,

    @ColumnInfo(name = DownloadColumn.totalTime)
    var totalTime: Long,

    @ColumnInfo(name = DownloadColumn.status)
    var status: Int,

    @ColumnInfo(name = DownloadColumn.order)
    var order: Long,

    @ColumnInfo(name = DownloadColumn.error)
    var isError: Boolean

) : Parcelable {
    @Ignore
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
        parcel.readLong(),
        parcel.readByte() != 0.toByte()
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
        parcel.writeByte(if (isError) 1 else 0)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<DownloadItem> {
        override fun createFromParcel(parcel: Parcel) = DownloadItem(parcel)

        override fun newArray(size: Int): Array<DownloadItem?> = arrayOfNulls(size)
    }

}

fun Chapter.toDownloadItem() = DownloadItem(
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
    status = DownloadStatus.unknown,
    order = 0,
    isError = false
)

fun LatestChapter.toDownloadItem() = DownloadItem(
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
    status = DownloadStatus.unknown,
    order = 0,
    isError = false
)
