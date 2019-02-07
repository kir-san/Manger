package com.san.kir.manger.room.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "sites")
data class Site(
    @PrimaryKey(autoGenerate = true) var id: Long,
    var name: String,
    var host: String,
    var catalogName: String,
    var volume: Int,
    var oldVolume: Int,
    var siteID: Int
)
