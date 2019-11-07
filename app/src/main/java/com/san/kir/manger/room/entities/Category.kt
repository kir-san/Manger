package com.san.kir.manger.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

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

    @Ignore constructor()
    constructor(name: String, order: Int) {
        this.name = name
        this.order = order
    }
}


