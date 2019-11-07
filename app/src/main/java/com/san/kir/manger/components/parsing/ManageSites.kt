package com.san.kir.manger.components.parsing

import com.github.kittinunf.fuel.Fuel
import com.san.kir.manger.components.parsing.sites.Acomics
import com.san.kir.manger.components.parsing.sites.Allhentai
import com.san.kir.manger.components.parsing.sites.Mangachan
import com.san.kir.manger.components.parsing.sites.Mintmanga
import com.san.kir.manger.components.parsing.sites.Readmanga
import com.san.kir.manger.components.parsing.sites.Selfmanga
import com.san.kir.manger.components.parsing.sites.Unicomics
import com.san.kir.manger.components.parsing.sites.Yaoichan
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.room.entities.toDownloadItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object ManageSites {

    lateinit var mSiteRepository: SiteRepository

    val CATALOG_SITES by lazy {
        listOf(
            Mangachan(mSiteRepository),
            Readmanga(mSiteRepository),
            Mintmanga(mSiteRepository),
            Selfmanga(mSiteRepository),
            Allhentai(mSiteRepository),
            Yaoichan(mSiteRepository),
            Unicomics(mSiteRepository),
            Acomics(mSiteRepository)
        )
    }

    fun getDocument(url: String): Document {
        val (_, _, result) = Fuel.get(url).responseString()
        result.fold({}, { throw it })
        return Jsoup.parse(result.component1())
    }

    fun getSite(link: String): SiteCatalog {
        return CATALOG_SITES.first {
            it.allCatalogName.any { s ->
                link.contains(s)
            }
        }
    }

    suspend fun chapters(manga: Manga): List<Chapter> {
        val site = getSite(manga.host)
        return site.chapters(manga)
    }

    // Загрузка полной информации для элемента в каталоге
    suspend fun getFullElement(simpleElement: SiteCatalogElement) =
        withContext(Dispatchers.Default) {
            CATALOG_SITES.first { it.allCatalogName.any { s -> s == simpleElement.catalogName } }
                .getFullElement(simpleElement)
        }


    // Получение страниц
    suspend fun pages(item: DownloadItem): List<String> {
        val site = getSite(item.link)
        return site.pages(item)
    }

    suspend fun pages(chapter: Chapter) = pages(chapter.toDownloadItem())

    suspend fun getElementOnline(url: String): SiteCatalogElement? {
        var lUrl = url
        getSite(lUrl).also {
            if (!lUrl.contains("http://")) {
                lUrl = "http://$lUrl"
            }
            return it.getElementOnline(lUrl)
        }
    }
}
