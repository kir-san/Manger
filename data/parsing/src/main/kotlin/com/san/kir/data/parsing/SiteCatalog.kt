package com.san.kir.data.parsing

import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.SiteCatalogElement
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

abstract class SiteCatalog {
    abstract val name: String
    abstract val catalogName: String

    open val host: String
        get() = "http://$catalogName"
    open val allCatalogName: List<String>
        get() = listOf(catalogName)

    abstract val catalog: String

    abstract var volume: Int

    abstract suspend fun init(): SiteCatalog

    open suspend fun fullElement(element: SiteCatalogElement): SiteCatalogElement = element
    abstract fun catalog(): Flow<SiteCatalogElement>
    abstract suspend fun elementByUrl(url: String): SiteCatalogElement?
    abstract suspend fun chapters(manga: Manga): List<Chapter>
    abstract suspend fun pages(item: Chapter): List<String>
}

abstract class SiteCatalogClassic : SiteCatalog()

abstract class SiteCatalogAlternative : SiteCatalog()

fun SiteCatalog.getShortLink(fullLink: String): String {
    val foundedCatalogs = allCatalogName.filter { catalog -> fullLink.contains(catalog, true) }
    val shortLink: String

    if (foundedCatalogs.size == 1 || fullLink.contains(catalogName, true)) {
        shortLink = fullLink.split(foundedCatalogs.first()).last()
    } else {
        Timber.v("fullLink = $fullLink")
        Timber.v("foundedCatalogs = $foundedCatalogs")
        throw Throwable("Каталогов найдено больше одного или не найдено совсем")
    }

    return shortLink
}
