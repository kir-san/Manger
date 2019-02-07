package com.san.kir.manger.components.parsing

import com.github.kittinunf.fuel.Fuel
import com.san.kir.manger.components.parsing.sites.Allhentai
import com.san.kir.manger.components.parsing.sites.Mangachan
import com.san.kir.manger.components.parsing.sites.Mintmanga
import com.san.kir.manger.components.parsing.sites.Readmanga
import com.san.kir.manger.components.parsing.sites.Selfmanga
import com.san.kir.manger.components.parsing.sites.Unicomics
import com.san.kir.manger.components.parsing.sites.Yaoichan
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.room.models.toDownloadItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
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
            Unicomics(mSiteRepository)
        )
    }

    fun getDocument(url: String): Document {
        val (_, _, result) = Fuel.get(url).responseString()
        return Jsoup.parse(result.component1())
    }

    suspend fun chapters(manga: Manga): List<Chapter>? {
        val site = CATALOG_SITES.firstOrNull {
            it.allCatalogName.any { manga.host.contains(it) }
        }
        return site?.chapters(manga)
    }

    // Загрузка полной информации для элемента в каталоге
    fun getFullElement(simpleElement: SiteCatalogElement) = GlobalScope.async {
        CATALOG_SITES.first { it.allCatalogName.any { it == simpleElement.catalogName }}
            .getFullElement(simpleElement)
    }

    // Получение страниц для главы
    suspend fun pages(item: DownloadItem) = CATALOG_SITES
        .first { it.allCatalogName.any { item.link.contains(it) } }
        .pages(item)

    suspend fun pages(chapter: Chapter) = pages(chapter.toDownloadItem())

    suspend fun getElementOnline(url: String): SiteCatalogElement? {
        var lUrl = url
        CATALOG_SITES
            .firstOrNull { catalog ->
                catalog.allCatalogName.any {
                    lUrl.contains(it)
                }
            }
            ?.also {
                if (!lUrl.contains("http://")) {
                    lUrl = "http://$lUrl"
                }
                return it.getElementOnline(lUrl)
            }
        return null
    }
}
