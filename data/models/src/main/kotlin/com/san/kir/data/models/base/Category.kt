package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.support.SortLibraryUtil
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = Category.tableName)
data class Category(
    @ColumnInfo(name = Col.id)
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = Col.name)
    var name: String = "",

    @ColumnInfo(name = Col.order)
    var order: Int = 0,

    @ColumnInfo(name = Col.isVisible)
    var isVisible: Boolean = true,

    @ColumnInfo(name = Col.typeSort)
    var typeSort: String = SortLibraryUtil.abc,

    @ColumnInfo(name = Col.isReverseSort)
    var isReverseSort: Boolean = false,

    @ColumnInfo(name = Col.spanPortrait)
    var spanPortrait: Int = 2,

    @ColumnInfo(name = Col.spanLandscape)
    var spanLandscape: Int = 3,

    @ColumnInfo(name = Col.isLargePortrait)
    var isLargePortrait: Boolean = true,

    @ColumnInfo(name = Col.isLargeLandscape)
    var isLargeLandscape: Boolean = true,
) : Parcelable {
    companion object {
        const val tableName = "categories"
    }

    object Col {
        const val id = "id"
        const val name = "name"
        const val order = "ordering"
        const val isVisible = "isVisible"
        const val typeSort = "typeSort"
        const val isReverseSort = "isReverseSort"
        const val spanPortrait = "spanPortrait"
        const val spanLandscape = "spanLandscape"
        const val isLargePortrait = "isListPortrait"
        const val isLargeLandscape = "isListLandscape"
    }
}


