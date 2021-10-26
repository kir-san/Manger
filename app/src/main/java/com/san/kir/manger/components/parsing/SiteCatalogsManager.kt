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
import com.san.kir.manger.di.DefaultDispatcher
import com.san.kir.manger.room.dao.SiteDao
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.room.entities.toDownloadItem
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.extensions.log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class Parsing @Inject constructor() {
    @OptIn(ExperimentalTime::class)
    suspend fun getDocument(url: String): Document {
        var tryCount = 10
        while (tryCount != 0) {
            tryCount--

            val (_, responce, result) = Fuel.get(url)
                .appendHeader("User-agent" to "manger app ${ID.generate()}")
                .responseString()

            log("statusCode ${responce.statusCode} - url ${responce.url}")
            val retryKey = "Retry-After"

            when (responce.statusCode) {
                429 ->
                    if (responce.headers.containsKey(retryKey)) {
                        val timeOut = responce[retryKey].first().toLong()
                        log("delay $timeOut seconds")
                        delay(Duration.seconds(timeOut))
                    } else {
                        delay(Duration.seconds(10))
                    }

                else -> {
                    result.fold(
                        {
                            return Jsoup.parse(it)
                        }, {
                            it.printStackTrace()
                        })
                }
            }
        }
        return Document("")
    }
}

class SiteCatalogsManager @Inject constructor(
    private val siteDao: SiteDao,
    parsing: Parsing,
    @DefaultDispatcher private val default: CoroutineDispatcher,
) {

    val catalog by lazy {
        listOf(
            Mangachan(parsing, siteDao),
            Readmanga(parsing, siteDao),
            Mintmanga(parsing, siteDao),
            Selfmanga(parsing, siteDao),
            Allhentai(parsing, siteDao),
            Yaoichan(parsing, siteDao),
            Unicomics(parsing, siteDao),
            Acomics(parsing, siteDao)
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
