package com.san.kir.data.parsing

import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.data.models.Chapter
import com.san.kir.data.models.DownloadItem
import com.san.kir.data.models.Manga
import com.san.kir.data.models.SiteCatalogElement
import com.san.kir.data.models.toDownloadItem
import com.san.kir.data.parsing.sites.Acomics
import com.san.kir.data.parsing.sites.Allhentai
import com.san.kir.data.parsing.sites.ComX
import com.san.kir.data.parsing.sites.Mangachan
import com.san.kir.data.parsing.sites.Mintmanga
import com.san.kir.data.parsing.sites.Readmanga
import com.san.kir.data.parsing.sites.Selfmanga
import com.san.kir.data.parsing.sites.Unicomics
import com.san.kir.data.parsing.sites.Yaoichan
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiteCatalogsManager @Inject constructor(
    connectManager: ConnectManager,
) {

    val catalog by lazy {
        listOf(
            Mangachan(connectManager),
            Readmanga(connectManager),
            Mintmanga(connectManager),
            Selfmanga(connectManager),
            Allhentai(connectManager),
            Yaoichan(connectManager),
            Unicomics(connectManager),
            Acomics(connectManager),
            ComX(connectManager),
        )
    }

    fun getSite(link: String): SiteCatalog {
        return catalog.first {
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
        withDefaultContext {
            catalog.first { it.allCatalogName.any { s -> s == simpleElement.catalogName } }
                .getFullElement(simpleElement)
        }


    // Получение страниц
    suspend fun pages(item: DownloadItem): List<String> {
        val site = getSite(item.link)
        return site.pages(item)
    }

    suspend fun pages(chapter: Chapter) = pages(chapter.toDownloadItem())

    suspend fun getElementOnline(url: String): SiteCatalogElement? =
        withDefaultContext {
            var lUrl = url

            if (!lUrl.contains("http")) {
                lUrl = "http://$lUrl"
            }

            return@withDefaultContext getSite(lUrl).getElementOnline(lUrl)
        }
}