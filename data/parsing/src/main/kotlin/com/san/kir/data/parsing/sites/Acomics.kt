package com.san.kir.data.parsing.sites

import com.san.kir.core.internet.ConnectManager
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.SiteCatalogElement
import com.san.kir.data.parsing.SiteCatalogAlternative
import com.san.kir.data.parsing.Status
import com.san.kir.data.parsing.Translate
import com.san.kir.data.parsing.getShortLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import timber.log.Timber
import java.util.concurrent.Executors

class Acomics(private val connectManager: ConnectManager) : SiteCatalogAlternative() {
    override val name: String = "Авторский комикс"
    override val catalogName: String = "acomics.ru"
    override val host: String
        get() = "https://$catalogName"
    override val catalog: String =
        "$host/comics?categories=&ratings[]=1&ratings[]=2&ratings[]=3&ratings[]=4&ratings[]=5&ratings[]=6&type=0&updatable=0&issue_count=1&sort=last_update"
    override var volume = 0

    override suspend fun init(): Acomics {
        var docLocal = Elements()
        var i = 580
        volume = 5800

        suspend fun isGetNext(): Boolean {
            docLocal = connectManager
                .getDocument(catalog + "&skip=${10 * i}")
                .select(contentTemplate)

            return docLocal.none { it.text().isBlank() }
        }

        while (isGetNext()) {
            volume += docLocal.size
            i++
        }

        Timber.v("Acomics volume = ${volume}")
        return this
    }

    override suspend fun elementByUrl(url: String): SiteCatalogElement? = runCatching {
        val doc = connectManager.getDocument("$url/about")

        fullElement(
            SiteCatalogElement(
                host = host,
                catalogName = catalogName,
                shortLink = url.split(catalogName).last(),
                link = url,
                name = doc.select("#container .serial a img").attr("alt"),
                about = doc.select("#contentMargin .about-summary > p > span").text(),
                type = "Комикс",
            )
        )
    }.onFailure { Timber.v(it, message = url) }.getOrNull()

    override suspend fun fullElement(element: SiteCatalogElement): SiteCatalogElement {
        val doc = connectManager.getDocument("${element.link}/about")

        val statusEdition = Status.UNKNOWN
        val statusTranslate = Translate.UNKNOWN

        val logo = doc.select("#container .serial a img").attr("src")
            .let { logo ->
                if (logo.isNotEmpty()) host + logo
                else ""
            }

        val genres = doc.select("#contentMargin .about-summary div > a").map { it.text() }

        var authors = listOf<String>()
        var volume = 0
        var populate = 0

        doc.select("#contentMargin .about-summary > p").forEach { p ->
            when {
                AUTHOR in p.text() -> authors = listOf(p.withoutPrefix(AUTHOR))
                VOLUME in p.text() -> volume = p.withoutPrefix(VOLUME).toInt()
                POPULATE in p.text() -> populate = p.withoutPrefix(POPULATE).toInt()
            }
        }

        return element.copy(
            statusEdition = statusEdition,
            statusTranslate = statusTranslate,
            logo = logo,
            genres = genres,
            authors = authors,
            volume = volume,
            populate = populate,
            isFull = true,
        )
    }

    private fun simpleParseElement(elem: Element): SiteCatalogElement {
        val link = elem.select(".catdata2 .title a").first()?.attr("href") ?: ""
        return SiteCatalogElement(
            host = host,
            catalogName = catalogName,
            name = elem.select(".catdata2 .title a").first()?.text() ?: "",
            link = link,
            shortLink = link.split(catalogName).last(),
            about = elem.select(".catdata2 .about").text(),
            logo = host + elem.select(".catdata1 a > img").attr("src"),
            type = "Комикс",
        )
    }

    override fun catalog() = flow {
        var docLocal = connectManager.getDocument(catalog).select(contentTemplate)
        var index = 0

        suspend fun isGetNext(): Boolean {
            docLocal = connectManager
                .getDocument(catalog + "&skip=${10 * index}")
                .select(contentTemplate)

            return docLocal.none { it.text().isBlank() }
        }

        do {
            docLocal.forEach { element -> emit(simpleParseElement(element)) }
            index++
        } while (isGetNext())
    }

    override suspend fun chapters(manga: Manga): List<Chapter> {
        return listOf(
            Chapter(
                manga = manga.name,
                name = manga.name,
                link = host + manga.shortLink,
                path = manga.path + "/" + manga.name
            )
        )
    }

    private val pool = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
    private val scope = CoroutineScope(pool)

    override suspend fun pages(item: Chapter): List<String> {
        val shortLink = getShortLink(item.link)

        var docLocal = connectManager
            .getDocument("${host + shortLink}/content")
            .select("#contentMargin .serial-content table td a")

        var index = 0
        var list = listOf<String>()

        suspend fun hasNext(): Boolean {
            docLocal = connectManager
                .getDocument("${host + shortLink}/content" + "?skip=${10 * index}")
                .select("#contentMargin .serial-content table td a")

            return docLocal.size != 0
        }

        val lock = Mutex()

        do {
            docLocal.map { element ->
                scope.launch {
                    kotlin.runCatching {
                        val link = host + connectManager
                            .getDocument(element.attr("href"))
                            .select("#mainImage")
                            .attr("src")

                        lock.withLock {
                            list = list + link
                        }
                    }.onFailure { it.printStackTrace() }
                }
            }.joinAll()
            index++
        } while (hasNext())

        return list
    }

    companion object {
        private const val contentTemplate = "#contentMargin .list-loadable"
        private const val AUTHOR = "Авторы:"
        private const val VOLUME = "Количество выпусков:"
        private const val POPULATE = "Количество подписчиков:"

        private fun Element.withoutPrefix(prefix: String) = text().removePrefix(prefix).trim()
    }

}
