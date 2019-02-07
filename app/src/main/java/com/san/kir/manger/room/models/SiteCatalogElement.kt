package com.san.kir.manger.room.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "items")
data class SiteCatalogElement(
    @PrimaryKey(autoGenerate = true) var id: Long,
    var siteId: Int,
    var host: String,
    var catalogName: String,
    var name: String,
    var shotLink: String,
    var link: String,
    var type: String,
    var authors: List<String>,
    var statusEdition: String,
    var statusTranslate: String,
    var volume: Int,
    var genres: MutableList<String>,
    var about: String,
    var isAdded: Boolean,
    var populate: Int,
    var logo: String,
    var dateId: Int,
    var isFull: Boolean
) {
    @Ignore
    constructor(
        siteId: Int=0,
        host: String="",
        catalogName: String="",
        name: String="",
        shotLink: String="",
        link: String="",
        type: String="",
        authors: List<String> = listOf(),
        statusEdition: String="",
        statusTranslate: String="",
        volume: Int=0,
        genres: MutableList<String> = mutableListOf(),
        about: String="",
        isAdded: Boolean = false,
        populate: Int=0,
        logo: String="",
        dateId: Int=0,
        isFull: Boolean = false
    ) : this(
        0,
        siteId,
        host,
        catalogName,
        name,
        shotLink,
        link,
        type,
        authors,
        statusEdition,
        statusTranslate,
        volume,
        genres,
        about,
        isAdded,
        populate,
        logo,
        dateId,
        isFull
    )
}

fun SiteCatalogElement.toManga(category: String, path: String): Manga {
    return Manga(
        unic = name,
        name = name,
        host = host,
        authorsList = authors,
        logo = logo,
        about = about,
        categories = category,
        genresList = genres,
        path = path,
        site = link,
        status = statusEdition
    )
}
