package com.san.kir.manger.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

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
