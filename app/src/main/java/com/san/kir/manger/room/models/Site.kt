package com.san.kir.manger.room.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "sites")
data class Site(@PrimaryKey(autoGenerate = true) var id: Long = 0L,
                var name: String = "",
                var host: String = "",
                var catalogName: String = "",
                var volume: Int = 0,
                var oldVolume: Int = 0,
                var siteID: Int = 0)
