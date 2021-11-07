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
import com.san.kir.manger.di.DefaultDispatcher
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.room.entities.toDownloadItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiteCatalogsManager @Inject constructor(
    connectManager: ConnectManager,
    @DefaultDispatcher private val default: CoroutineDispatcher,
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
        withContext(default) {
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
        withContext(default) {
            var lUrl = url

            if (!lUrl.contains("http")) {
                lUrl = "http://$lUrl"
            }

            return@withContext getSite(lUrl).getElementOnline(lUrl)
        }
}
