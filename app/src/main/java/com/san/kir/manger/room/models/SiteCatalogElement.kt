package com.san.kir.manger.room.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "items")
data class SiteCatalogElement(
        @PrimaryKey(autoGenerate = true) var id: Long = 0L,
        var siteId: Int = 0,
        var host: String = "",
        var catalogName: String = "",
        var name: String = "",
        var shotLink: String = "",
        var link: String = "",
        var type: String = "",
        var authors: List<String> = listOf(),
        var statusEdition: String = "",
        var statusTranslate: String = "",
        var volume: Int = 0,
        var genres: MutableList<String> = mutableListOf(),
        var about: String = "",
        var isAdded: Boolean = false,
        var populate: Int = 0,
        var logo: String = "",
        var dateId: Int = 0,
        var isFull: Boolean = false)

fun SiteCatalogElement.toManga(category: String, path: String): Manga {
    return Manga(unic = name,
                 name = name,
                 host = host,
                 authorsList = authors,
                 logo = logo,
                 about = about,
                 categories = category,
                 genresList = genres,
                 path = path,
                 site = link,
                 status = statusEdition)
}
