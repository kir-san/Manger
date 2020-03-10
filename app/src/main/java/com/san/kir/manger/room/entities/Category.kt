package com.san.kir.manger.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.san.kir.manger.room.columns.CategoryColumn

@Entity(tableName = CategoryColumn.tableName)
class Category {
    @PrimaryKey(autoGenerate = true) var id = 0L

    @ColumnInfo(name = "name")
    var name = ""

    @ColumnInfo(name = "order")
    var order = 0

    @ColumnInfo(name = "isVisible")
    var isVisible = true

    @ColumnInfo(name = "typeSort")
    var typeSort = ""

    @ColumnInfo(name = "isReverseSort")
    var isReverseSort = false

    @ColumnInfo(name = "spanPortrait")
    var spanPortrait = 2

    @ColumnInfo(name = "spanLandscape")
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


