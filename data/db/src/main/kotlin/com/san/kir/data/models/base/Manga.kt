package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.san.kir.core.support.ChapterFilter
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = Manga.tableName)
data class Manga(
    @ColumnInfo(name = Col.id)
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = Col.host)
    var host: String = "",

    @ColumnInfo(name = Col.name)
    var name: String = "",

    @ColumnInfo(name = Col.logo)
    var logo: String = "",

    @ColumnInfo(name = Col.about)
    var about: String = "",

    @Deprecated(
        message = "Больше не использовать, не удообное обновление",
        replaceWith = ReplaceWith("categoryId")
    )
    @ColumnInfo(name = Col.category)
    var category: String = "",

    @ColumnInfo(name = Col.categoryId, defaultValue = "0")
    var categoryId: Long = 0,

    @ColumnInfo(name = Col.path)
    var path: String = "",

    @ColumnInfo(name = Col.status)
    var status: String = "",

    @ColumnInfo(name = Col.color)
    var color: Int = 0,

    @ColumnInfo(name = Col.populate)
    var populate: Int = 0,

    @ColumnInfo(name = Col.order)
    var order: Int = 0,

    @ColumnInfo(name = Col.alternativeSort)
    var isAlternativeSort: Boolean = true,

    @ColumnInfo(name = Col.update)
    var isUpdate: Boolean = true,

    @ColumnInfo(name = Col.filter)
    var chapterFilter: ChapterFilter = ChapterFilter.ALL_READ_ASC,

    @ColumnInfo(name = Col.alternativeSite)
    var isAlternativeSite: Boolean = false,

    @ColumnInfo(name = Col.link)
    var shortLink: String = "",

    @ColumnInfo(name = Col.authors)
    var authorsList: List<String> = listOf(),

    @ColumnInfo(name = Col.genres)
    var genresList: List<String> = listOf(),

    ) : Parcelable {
    companion object {
        const val tableName = "manga"
    }

    object Col {
        const val id = "id"
        const val host = "host"
        const val name = "name"
        const val category = "category"
        const val populate = "populate"
        const val order = "ordering"
        const val logo = "logo"
        const val about = "about"
        const val color = "color"
        const val path = "path"
        const val status = "status"
        const val alternativeSort = "isAlternativeSort"
        const val alternativeSite = "isAlternativeSite"
        const val update = "isUpdate"
        const val filter = "chapterFilter"
        const val link = "shortLink"
        const val authors = "authors"
        const val genres = "genres"
        const val categoryId = "category_id"
    }
}

