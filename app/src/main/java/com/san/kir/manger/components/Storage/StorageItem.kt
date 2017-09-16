package com.san.kir.manger.components.Storage

import android.os.Parcel
import android.os.Parcelable

class StorageItem(val name: String = "",
                  val path: String = "",
                  var sizeFull: Long = 0L,
                  var sizeRead: Long = 0L,
                  var isNew: Boolean = true) : Parcelable {

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readLong(),
                                       source.readLong(),
                                       1.toByte() == source.readByte())

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(name)
        dest?.writeString(path)
        dest?.writeLong(sizeFull)
        dest?.writeLong(sizeRead)
        dest?.writeByte((if (isNew) 1 else 0).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<StorageItem> = object : Parcelable.Creator<StorageItem> {
            override fun createFromParcel(source: Parcel): StorageItem {
                return StorageItem(source)
            }

            override fun newArray(size: Int): Array<StorageItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}
