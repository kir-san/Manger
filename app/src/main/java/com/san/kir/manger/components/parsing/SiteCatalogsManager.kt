package com.san.kir.manger.components.parsing

import com.san.kir.manger.components.parsing.sites.Acomics
import com.san.kir.manger.components.parsing.sites.Allhentai
import com.san.kir.manger.components.parsing.sites.ComX
import com.san.kir.manger.components.parsing.sites.Mangachan
import com.san.kir.manger.components.parsing.sites.Mintmanga
import com.san.kir.manger.components.parsing.sites.Readmanga
import com.san.kir.manger.components.parsing.sites.Selfmanga
import com.san.kir.manger.components.parsing.sites.Unicomics
import com.san.kir.manger.components.parsing.sites.Yaoichan
import com.san.kir.manger.data.room.entities.Chapter
import com.san.kir.manger.data.room.entities.DownloadItem
import com.san.kir.manger.data.room.entities.Manga
import com.san.kir.manger.data.room.entities.SiteCatalogElement
import com.san.kir.manger.data.room.entities.toDownloadItem
import com.san.kir.manger.utils.coroutines.withDefaultContext
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
