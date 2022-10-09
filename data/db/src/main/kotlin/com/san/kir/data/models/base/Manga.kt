package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.support.ChapterFilter
import kotlinx.parcelize.Parcelize

@Stable
@Parcelize
@Entity(tableName = "manga")
data class Manga(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "host")
    val host: String = "",

    @ColumnInfo(name = "name")
    val name: String = "",

    @ColumnInfo(name = "logo")
    val logo: String = "",

    @ColumnInfo(name = "about")
    val about: String = "",

    @Deprecated(
        message = "Больше не использовать, не удообное обновление",
        replaceWith = ReplaceWith("categoryId")
    )
    @ColumnInfo(name = "category")
    val category: String = "",

    @ColumnInfo(name = "category_id", defaultValue = "0")
    val categoryId: Long = 0,

    @ColumnInfo(name = "path")
    val path: String = "",

    @ColumnInfo(name = "status")
    val status: String = "",

    @ColumnInfo(name = "color")
    val color: Int = 0,

    @ColumnInfo(name = "populate")
    val populate: Int = 0,

    @ColumnInfo(name = "ordering")
    val order: Int = 0,

    @ColumnInfo(name = "isAlternativeSort")
    val isAlternativeSort: Boolean = true,

    @ColumnInfo(name = "isUpdate")
    val isUpdate: Boolean = true,

    @ColumnInfo(name = "chapterFilter")
    val chapterFilter: ChapterFilter = ChapterFilter.ALL_READ_ASC,

    @ColumnInfo(name = "isAlternativeSite")
    val isAlternativeSite: Boolean = false,

    @ColumnInfo(name = "shortLink")
    val shortLink: String = "",

    @ColumnInfo(name = "authors")
    val authorsList: List<String> = listOf(),

    @ColumnInfo(name = "genres")
    val genresList: List<String> = listOf(),

    ) : Parcelable
