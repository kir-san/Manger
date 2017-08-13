package com.san.kir.manger.dbflow.models

import java.io.Serializable

class SiteCatalogElement : Serializable {
    var id = -1
    var host = String()
    var catalogName = String()
    var name = String()
    var shotLink = String()
    var link = String()
    var type = String()
    var authors = mutableListOf<String>()
    var statusEdition = String()
    var statusTranslate = String()
    var volume = 0
    var genres = mutableListOf<String>()
    var about = String()
    var isAdded = false
    var populate = 0
    var logo = String()
    var dateId = 0
    var isFull = false
}

fun SiteCatalogElement.toManga(category: String, path: String): Manga {
    return Manga(name = name,
                 host = host,
                 authors = authors,
                 logo = logo,
                 about = about,
                 category = category,
                 genres = genres,
                 path = path,
                 site = link,
                 status = statusEdition)
}
