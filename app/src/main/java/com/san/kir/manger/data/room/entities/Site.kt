package com.san.kir.manger.data.room.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "sites")
data class Site(
    @PrimaryKey(autoGenerate = true) var id: Long = 0L,
    var name: String = "",
    var host: String = "",
    var catalogName: String = "",
    var volume: Int = -1,
    var oldVolume: Int = -1,
    var siteID: Int = -1
) : Parcelable
