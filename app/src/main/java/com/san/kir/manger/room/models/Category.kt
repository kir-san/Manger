package com.san.kir.manger.room.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "categories")
class Category {
    @PrimaryKey(autoGenerate = true) var id = 0L
    var name = ""
    var order = 0
    var isVisible = true
    var typeSort = ""
    var isReverseSort = false
    var spanPortrait = 2
    var spanLandscape = 3

    @ColumnInfo(name = "isListPortrait")
    var isLargePortrait = true
    @ColumnInfo(name = "isListLandscape")
    var isLargeLandscape = true

    constructor()
    constructor(name: String, order: Int) {
        this.name = name
        this.order = order
    }
}


