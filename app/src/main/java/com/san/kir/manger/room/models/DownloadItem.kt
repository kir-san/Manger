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
        var order: Long = 0) : Parcelable {

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
                parcel.readLong())

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
                parcel.writeLong(order)
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
        const val unknown = 0
        const val loading = 1
        const val pause = 2
        const val error = 3
        const val completed = 4
        const val queued = 5
}

fun Chapter.toDownloadItem() = DownloadItem(name = manga + " " + name,
                                            link = site,
                                            path = path)
