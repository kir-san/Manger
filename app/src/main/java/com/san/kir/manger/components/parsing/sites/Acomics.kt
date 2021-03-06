package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.SiteCatalogAlternative
import com.san.kir.manger.components.parsing.Status
import com.san.kir.manger.components.parsing.Translate
import com.san.kir.manger.components.parsing.getShortLink
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.SiteCatalogElement
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.concurrent.Executors

class Acomics(private val siteRepository: SiteRepository) : SiteCatalogAlternative() {
    override val name: String = "Авторский комикс"
    override val catalogName: String = "acomics.ru"
    override val host: String
        get() = "https://$catalogName"
    override val siteCatalog: String =
        "$host/comics?categories=&ratings[]=1&ratings[]=2&ratings[]=3&ratings[]=4&ratings[]=5&ratings[]=6&type=0&updatable=0&issue_count=1&sort=last_update"
    override var volume = siteRepository.getItem(name)?.volume ?: 0
    override var oldVolume = volume

    private val contentTemplate = "#contentMargin .list-loadable"

    override suspend fun init(): Acomics {
        if (!isInit) {
            oldVolume = siteRepository.getItem(name)?.volume ?: 0

            var docLocal = Elements()
            var i = 357
            volume = 3560

            fun isGetNext(): Boolean {
                val document = ManageSites.getDocument(siteCatalog + "&skip=${10 * i}")

                docLocal = document.select(contentTemplate)

                return docLocal.none { it.text().isBlank() }
            }
            while (isGetNext()) {
                volume += docLocal.size
                i++
            }

            isInit = true
        }
        return this
    }

    override suspend fun getElementOnline(url: String): SiteCatalogElement? = runCatching {
        val element = SiteCatalogElement()

        element.host = host
        element.catalogName = catalogName
        element.siteId = id

        element.shotLink = url.split(catalogName).last()
        element.link = url

        val doc = ManageSites.getDocument("$url/about")
        element.name = doc.select("#container .serial a img").attr("alt")
        element.about = doc.select("#contentMargin .about-summary > p > span").text()

        element.type = "Комикс"

        getFullElement(element)
    }.fold(onSuccess = { it },
           onFailure = { null })

    override suspend fun getFullElement(element: SiteCatalogElement): SiteCatalogElement {
        val doc = ManageSites.getDocument("${element.link}/about")

        element.statusEdition = Status.UNKNOWN
        element.statusTranslate = Translate.UNKNOWN

        // Ссылка на лого
        val logo = doc.select("#container .serial a img").attr("src")
        if (logo.isNotEmpty())
            element.logo = host + logo

        element.genres =
            doc.select("#contentMargin .about-summary div > a").map { it.text() }.toMutableList()

        doc.select("#contentMargin .about-summary > p").forEach { p ->
            when {
                p.text().contains("Автор оригинала:") -> {
                    element.authors = listOf(p.text().removePrefix("Автор оригинала:").trim())
                }
                p.text().contains("Количество выпусков:") -> {
                    element.volume = p.text().removePrefix("Количество выпусков:").trim().toInt()
                }
                p.text().contains("Количество подписчиков:") -> {
                    element.populate =
                        p.text().removePrefix("Количество подписчиков:").trim().toInt()
                }
            }
        }

        element.isFull = true

        return element
    }

    private fun simpleParseElement(elem: Element): SiteCatalogElement {
        val element = SiteCatalogElement()

        element.host = host
        element.catalogName = catalogName
        element.siteId = id

        element.name = elem.select(".catdata2 .title a").first().text()

        element.link = elem.select(".catdata2 .title a").first().attr("href")
        element.shotLink = element.link.split(catalogName).last()

        element.about = elem.select(".catdata2 .about").text()

        element.logo = host + elem.select(".catdata1 a > img").attr("src")

        element.type = "Комикс"

        return element
    }

    override fun getCatalog() = flow {
        var docLocal = ManageSites.getDocument(siteCatalog).select(contentTemplate)
        var i = 0

        fun isGetNext(): Boolean {
            val document = ManageSites.getDocument(siteCatalog + "&skip=${10 * i}")
            docLocal = document.select(contentTemplate)

            return docLocal.none { it.text().isBlank() }
        }
        do {
            docLocal.forEach { element ->
                emit(simpleParseElement(element))
            }
            i++
        } while (isGetNext())
    }

    override suspend fun chapters(manga: Manga): List<Chapter> {
        return listOf(
            Chapter(
                manga = manga.unic,
                name = manga.unic,
                site = host + manga.shortLink,
                path = manga.path + "/" + manga.unic
            )
        )
    }

    override suspend fun pages(item: DownloadItem): List<String> {
        val shortLink = getShortLink(item.link)

        var docLocal = ManageSites.getDocument("${host + shortLink}/content")
            .select("#contentMargin .serial-content table td a")
        var i = 0
        var list = listOf<String>()

        fun isGetNext(): Boolean {
            val document = ManageSites.getDocument("${host + shortLink}/content" + "?skip=${10 * i}")
            docLocal = document.select("#contentMargin .serial-content table td a")

            return docLocal.size != 0
        }

        val pool = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
        val lock = Mutex()

        do {
            docLocal.map { element ->
                GlobalScope.launch(pool) {
                    val url = element.attr("href")
                    val document = ManageSites.getDocument(url)
                    val link = host + document.select("#mainImage").attr("src")

                    lock.withLock {
                        list = list + link
                    }
                }
            }.joinAll()
            i++
        } while (isGetNext())

        return list
    }

}
