package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.support.SortLibraryUtil
import kotlinx.parcelize.Parcelize
import javax.annotation.concurrent.Immutable

@Immutable
@Parcelize
@Entity(tableName = "categories")
data class Category(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "name")
    val name: String = "",

    @ColumnInfo(name = "ordering")
    val order: Int = 0,

    @ColumnInfo(name = "isVisible")
    val isVisible: Boolean = true,

    @ColumnInfo(name = "typeSort")
    val typeSort: String = SortLibraryUtil.abc,

    @ColumnInfo(name = "isReverseSort")
    val isReverseSort: Boolean = false,

    @ColumnInfo(name = "spanPortrait")
    val spanPortrait: Int = 2,

    @ColumnInfo(name = "spanLandscape")
    val spanLandscape: Int = 3,

    @ColumnInfo(name = "isListPortrait")
    val isLargePortrait: Boolean = true,

    @ColumnInfo(name = "isListLandscape")
    val isLargeLandscape: Boolean = true,
) : Parcelable
