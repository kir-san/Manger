package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.support.ChapterFilter
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "manga")
data class Manga(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "host")
    var host: String = "",

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "logo")
    var logo: String = "",

    @ColumnInfo(name = "about")
    var about: String = "",

    @Deprecated(
        message = "Больше не использовать, не удообное обновление",
        replaceWith = ReplaceWith("categoryId")
    )
    @ColumnInfo(name = "category")
    var category: String = "",

    @ColumnInfo(name = "category_id", defaultValue = "0")
    var categoryId: Long = 0,

    @ColumnInfo(name = "path")
    var path: String = "",

    @ColumnInfo(name = "status")
    var status: String = "",

    @ColumnInfo(name = "color")
    var color: Int = 0,

    @ColumnInfo(name = "populate")
    var populate: Int = 0,

    @ColumnInfo(name = "ordering")
    var order: Int = 0,

    @ColumnInfo(name = "isAlternativeSort")
    var isAlternativeSort: Boolean = true,

    @ColumnInfo(name = "isUpdate")
    var isUpdate: Boolean = true,

    @ColumnInfo(name = "chapterFilter")
    var chapterFilter: ChapterFilter = ChapterFilter.ALL_READ_ASC,

    @ColumnInfo(name = "isAlternativeSite")
    var isAlternativeSite: Boolean = false,

    @ColumnInfo(name = "shortLink")
    var shortLink: String = "",

    @ColumnInfo(name = "authors")
    var authorsList: List<String> = listOf(),

    @ColumnInfo(name = "genres")
    var genresList: List<String> = listOf(),

    ) : Parcelable
