package com.san.kir.data.parsing

import android.app.Application
import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.support.DIR
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.core.utils.getFullPath
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.SiteCatalogElement
import com.san.kir.data.parsing.sites.Acomics
import com.san.kir.data.parsing.sites.Allhentai
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
    context: Application,
    connectManager: ConnectManager,
) {

    init {
        Status.init(context)
        Translate.init(context)
    }

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
//            ComX(connectManager),
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
                .fullElement(simpleElement)
        }


    // Получение страниц
    suspend fun pages(item: Chapter): List<String> {
        val site = getSite(item.link)
        return site.pages(item)
    }

    suspend fun elementByUrl(url: String): SiteCatalogElement? =
        withDefaultContext {
            var lUrl = url

            if (!lUrl.contains("http")) {
                lUrl = "http://$lUrl"
            }

            return@withDefaultContext getSite(lUrl).elementByUrl(lUrl)
        }

    fun catalogName(siteName: String): String {
        val first = catalog.firstOrNull { it.name == siteName }
            ?: catalog.first { it.catalogName == siteName }

        var catName = first.catalogName

        first.allCatalogName
            .firstOrNull { getFullPath(DIR.catalogName(catName)).exists() }
            ?.also { catName = it }

        return catName
    }
}
