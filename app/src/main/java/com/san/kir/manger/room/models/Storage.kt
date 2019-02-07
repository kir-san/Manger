package com.san.kir.manger.room.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity(tableName = "StorageItem")
data class Storage(
    @PrimaryKey(autoGenerate = true) var id: Long,
    var name: String,
    var path: String,
    var sizeFull: Double,
    var sizeRead: Double,
    var isNew: Boolean,
    var catalogName: String
) : Parcelable {
    @Ignore
    constructor(
        name: String = "",
        path: String = "",
        sizeFull: Double = 0.0,
        sizeRead: Double = 0.0,
        isNew: Boolean = false,
        catalogName: String = ""
    ) : this(0, name, path, sizeFull, sizeRead, isNew, catalogName)

    @Ignore
    constructor(parcel: Parcel) :
            this(
                id = parcel.readLong(),
                name = parcel.readString(),
                path = parcel.readString(),
                sizeFull = parcel.readDouble(),
                sizeRead = parcel.readDouble(),
                isNew = parcel.readByte() != 0.toByte(),
                catalogName = parcel.readString()
            )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeString(path)
        parcel.writeDouble(sizeFull)
        parcel.writeDouble(sizeRead)
        parcel.writeByte(if (isNew) 1 else 0)
        parcel.writeString(catalogName)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Storage> {
        override fun createFromParcel(parcel: Parcel) = Storage(parcel)

        override fun newArray(size: Int): Array<Storage?> = arrayOfNulls(size)
    }
}

