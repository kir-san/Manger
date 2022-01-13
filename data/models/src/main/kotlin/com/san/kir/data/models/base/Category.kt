package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.support.SortLibraryUtil
import com.san.kir.data.models.columns.CategoryColumn
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = CategoryColumn.tableName)
data class Category(
    @PrimaryKey(autoGenerate = true) var id: Long = 0L,

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "order")
    var order: Int = 0,

    @ColumnInfo(name = "isVisible")
    var isVisible: Boolean = true,

    @ColumnInfo(name = "typeSort")
    var typeSort: String = SortLibraryUtil.abc,

    @ColumnInfo(name = "isReverseSort")
    var isReverseSort: Boolean = false,

    @ColumnInfo(name = "spanPortrait")
    var spanPortrait: Int = 2,

    @ColumnInfo(name = "spanLandscape")
    var spanLandscape: Int = 3,

    @ColumnInfo(name = "isListPortrait")
    var isLargePortrait: Boolean = true,

    @ColumnInfo(name = "isListLandscape")
    var isLargeLandscape: Boolean = true,
) : Parcelable


