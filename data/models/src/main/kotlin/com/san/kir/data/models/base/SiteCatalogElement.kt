package com.san.kir.data.models.base

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "items")
data class SiteCatalogElement(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @Deprecated("Unused")
    var siteId: Int = 0,
    var host: String = "",
    var catalogName: String = "",
    var name: String = "",
    var shotLink: String = "",
    var link: String = "",
    var type: String = "",
    var authors: List<String> = emptyList(),
    var statusEdition: String = "",
    var statusTranslate: String = "",
    var volume: Int = 0,
    var genres: List<String> = emptyList(),
    var about: String = "",
    var isAdded: Boolean = false,
    var populate: Int = 0,
    var logo: String = "",
    var dateId: Int = 0,
    var isFull: Boolean = false,
) : Parcelable

fun SiteCatalogElement.toManga(category: String, path: String): Manga {
    return Manga(
        name = name,
        host = host,
        authorsList = authors,
        logo = logo,
        about = about,
        category = category,
        genresList = genres,
        path = path,
        shortLink = shotLink,
        status = statusEdition
    )
}
