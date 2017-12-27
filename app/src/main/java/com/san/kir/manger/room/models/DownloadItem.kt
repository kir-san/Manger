package com.san.kir.manger.room.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity(tableName = "downloads")
data class DownloadItem(
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0,
        var name: String = "",
        var link: String = "",
        var path: String = "",
        var totalPages: Int = 0,
        var downloadPages: Int = 0,
        var totalSize: Long = 0,
        var downloadSize: Long = 0,
        var totalTime: Long = 0,
        var status: Int = DownloadStatus.unknown,
        var order: Int = 0) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readLong(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readInt(),
                parcel.readInt(),
                parcel.readLong(),
                parcel.readLong(),
                parcel.readLong(),
                parcel.readInt(),
                parcel.readInt())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeLong(id)
                parcel.writeString(name)
                parcel.writeString(link)
                parcel.writeString(path)
                parcel.writeInt(totalPages)
                parcel.writeInt(downloadPages)
                parcel.writeLong(totalSize)
                parcel.writeLong(downloadSize)
                parcel.writeLong(totalTime)
                parcel.writeInt(status)
                parcel.writeInt(order)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<DownloadItem> {
                override fun createFromParcel(parcel: Parcel): DownloadItem {
                        return DownloadItem(parcel)
                }

                override fun newArray(size: Int): Array<DownloadItem?> {
                        return arrayOfNulls(size)
                }
        }

}

object DownloadStatus {
        val unknown = 0
        val loading = 1
        val pause = 2
        val error = 3
        val completed = 4
}

fun Chapter.toDownloadItem() = DownloadItem(name = manga + " " + name,
                                            link = site,
                                            path = path)
