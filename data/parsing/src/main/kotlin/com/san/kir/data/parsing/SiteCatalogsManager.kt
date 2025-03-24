package com.san.kir.data.parsing

import android.content.Context
import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.utils.DIR
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.core.utils.getFullPath
import com.san.kir.data.models.catalog.SiteCatalogElement
import com.san.kir.data.models.main.Chapter
import com.san.kir.data.models.main.Manga
import com.san.kir.data.parsing.sites.Acomics
import com.san.kir.data.parsing.sites.Allhentai
import com.san.kir.data.parsing.sites.Mangachan
import com.san.kir.data.parsing.sites.Mintmanga
import com.san.kir.data.parsing.sites.Readmanga
import com.san.kir.data.parsing.sites.Selfmanga
import com.san.kir.data.parsing.sites.Unicomics
import com.san.kir.data.parsing.sites.Yaoichan
import io.ktor.util.StringValues
import timber.log.Timber

public class SiteCatalogsManager(
    context: Context,
    connectManager: ConnectManager,
) {

    init {
        Status.init(context)
        Translate.init(context)
    }

    public val catalog: List<SiteCatalog> by lazy {
        listOf(
            Mangachan(connectManager),
            Readmanga(connectManager),
            Mintmanga(connectManager),
            Selfmanga(connectManager),
            Allhentai(connectManager),
            Yaoichan(connectManager),
            Unicomics(connectManager),
            Acomics(connectManager),
            // ComX(connectManager),
        )
    }

    public fun catalog(link: String): SiteCatalog {
        return catalog.first { siteCatalog ->
            siteCatalog.allCatalogName.any { link.contains(it) }
                    || siteCatalog.servers.any { link.contains(it) }
        }
    }

    public suspend fun chapters(manga: Manga): List<Chapter> = catalog(manga.host).chapters(manga)

    // Загрузка полной информации для элемента в каталоге
    public suspend fun fullElement(simpleElement: SiteCatalogElement): SiteCatalogElement =
        withDefaultContext {
            catalog.first { it.allCatalogName.any { s -> s == simpleElement.catalogName } }
                .fullElement(simpleElement)
        }

    // Получение страниц
    public suspend fun pages(item: Chapter): List<String> = withIoContext { catalog(item.link).pages(item) }

    public suspend fun elementByUrl(url: String): SiteCatalogElement? =
        withDefaultContext {
            var lUrl = url

            if (!lUrl.contains("http")) {
                lUrl = "http://$lUrl"
            }

            catalog(lUrl).elementByUrl(lUrl)
        }

    public fun catalogName(siteName: String): String {
        val first = catalog(siteName)
        var catName = first.catalogName
        first.allCatalogName
            .firstOrNull { getFullPath(DIR.catalogName(it)).exists() }
            ?.also { catName = it }

        return catName
    }

    public fun headersByLink(link: String): StringValues? {
       return runCatching { catalog(link) }.onFailure(Timber.Forest::e).getOrNull()?.headers
    }
}
