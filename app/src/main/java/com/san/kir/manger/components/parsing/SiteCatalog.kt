package com.san.kir.manger.components.parsing

import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.utils.extensions.log
import kotlinx.coroutines.flow.Flow

abstract class SiteCatalog {
    open var isInit: Boolean = false
    open var id: Int = 0

    abstract val name: String
    abstract val catalogName: String

    open val host: String
        get() = "http://$catalogName"
    open val allCatalogName: List<String>
        get() = listOf(catalogName)

    abstract val siteCatalog: String

    abstract var volume: Int
    abstract var oldVolume: Int

    abstract suspend fun init(): SiteCatalog

    open suspend fun getFullElement(element: SiteCatalogElement): SiteCatalogElement = element
    abstract fun getCatalog(): Flow<SiteCatalogElement>
    abstract suspend fun getElementOnline(url: String): SiteCatalogElement?
    abstract suspend fun chapters(manga: Manga): List<Chapter>
    abstract suspend fun pages(item: DownloadItem): List<String>
}

abstract class SiteCatalogClassic : SiteCatalog()

abstract class SiteCatalogAlternative : SiteCatalog()

fun SiteCatalog.getShortLink(fullLink: String): String {
    val foundedCatalogs = allCatalogName.filter { catalog -> fullLink.contains(catalog, true) }

    val shortLink: String

    if (foundedCatalogs.size == 1) {
        shortLink = fullLink.split(foundedCatalogs.first()).last()
    } else {
        log("fullLink = $fullLink")
        throw Throwable("Каталогов найдено больше одного или не найдено совсем")
    }

    return shortLink
}
