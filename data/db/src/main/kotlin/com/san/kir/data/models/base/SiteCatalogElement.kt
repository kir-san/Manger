package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Stable
@Parcelize
@Entity(tableName = "items")
data class SiteCatalogElement(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "host") val host: String = "",
    @ColumnInfo(name = "catalogName") val catalogName: String = "",
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "shotLink") val shortLink: String = "",
    @ColumnInfo(name = "link") val link: String = "",
    @ColumnInfo(name = "type") val type: String = "",
    @ColumnInfo(name = "authors") val authors: List<String> = emptyList(),
    @ColumnInfo(name = "statusEdition") val statusEdition: String = "",
    @ColumnInfo(name = "statusTranslate") val statusTranslate: String = "",
    @ColumnInfo(name = "volume") val volume: Int = 0,
    @ColumnInfo(name = "genres") val genres: List<String> = emptyList(),
    @ColumnInfo(name = "about") val about: String = "",
    @ColumnInfo(name = "populate") val populate: Int = 0,
    @ColumnInfo(name = "logo") val logo: String = "",
    @ColumnInfo(name = "dateId") val dateId: Int = 0,
    @ColumnInfo(name = "isFull") val isFull: Boolean = false,
) : Parcelable

fun SiteCatalogElement.toManga(categoryId: Long, path: String): Manga {
    return Manga(
        name = name,
        host = host,
        authorsList = authors,
        logo = logo,
        about = about,
        categoryId = categoryId,
        genresList = genres,
        path = path,
        shortLink = shortLink,
        status = statusEdition
    )
}
