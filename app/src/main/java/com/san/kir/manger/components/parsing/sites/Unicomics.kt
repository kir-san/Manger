package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.Parsing
import com.san.kir.manger.components.parsing.SiteCatalogClassic
import com.san.kir.manger.components.parsing.Status
import com.san.kir.manger.components.parsing.Translate
import com.san.kir.manger.components.parsing.getShortLink
import com.san.kir.manger.room.dao.SiteDao
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.utils.extensions.createDirs
import com.san.kir.manger.utils.extensions.getFullPath
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Element
import java.util.regex.Pattern

class Unicomics(
    private val parsing: Parsing,
    siteDao: SiteDao
) : SiteCatalogClassic() {
    override val name: String = "UniComics"
    override val catalogName: String = "unicomics.ru"
    override val siteCatalog: String = "$host/map"
    override var volume = siteDao.getItem(name)?.volume ?: 0
    override var oldVolume = volume

    override suspend fun init(): Unicomics {
        if (!isInit) {
            val doc = parsing.getDocument(siteCatalog)
            volume = doc.select(".content .block table tr a").size
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

        element.type = "Комикс"

        getFullElement(element)
    }.fold(onSuccess = { it },
           onFailure = { null })

    override suspend fun getFullElement(element: SiteCatalogElement): SiteCatalogElement {
        val doc = parsing.getDocument(element.link)

        val info = doc.select(".content .left_container .info")
        element.name = info.select("h1").text()

        element.statusEdition = Status.UNKNOWN
        element.statusTranslate = Translate.UNKNOWN

        // Ссылка на лого
        element.logo = doc.select(".content .left_container .image img").attr("src")

        info.select("table > tbody > tr").forEach { tr ->
            if (tr.text().contains("Издательство", true)) {
                element.authors = tr.select("td > a").map { it.text() }
            } else if (tr.text().contains("Жанр", true)) {
                element.genres = tr.select("td > a").map { it.text() }.drop(1).toMutableList()
            }
        }

        runCatching { info.select("p").last().text() }.onSuccess { element.about = it }

        element.volume = getChapters(element.link).size

        element.isFull = true

        return element
    }

    private fun simpleParseElement(elem: Element): SiteCatalogElement {
        val element = SiteCatalogElement()

        element.host = host
        element.catalogName = catalogName
        element.siteId = id

        element.name = elem.text()

        element.shotLink = elem.attr("href")
        element.link = host + element.shotLink

        element.type = "Комикс"

        return element
    }

    override fun getCatalog() = flow {
        val doc = parsing.getDocument(siteCatalog).select(".content .block table tr a")

        doc.forEach { link ->
            emit(simpleParseElement(link))
        }
    }

    override suspend fun chapters(manga: Manga): List<Chapter> {
        return getChapters(host + manga.shortLink)
            .map {
                val link = it.select("table > tbody .online > a").attr("href")
                val name = it.select("a.list_title").text()
                Chapter(
                    manga = manga.unic,
                    name = name,
                    site = if (link.contains(host)) link else host + link,
                    path = "${manga.path}/${name}"
                )
            }
    }

    private fun getChapters(url: String): List<Element> {
        var links = listOf<Element>()

        var doc = parsing.getDocument(url)

        var counter = 1
        var oldSize = 0

        while (true) {
            counter++

            links = links + doc.select(".content .left_container .list_comics .right_comics")
            if (oldSize == links.size) break

            oldSize = links.size

            doc = parsing.getDocument("$url/page/$counter")
        }

        return links
    }

    override suspend fun pages(item: DownloadItem): List<String> {
        var list = listOf<String>()
        getFullPath(item.path).createDirs()

        val shortLink = getShortLink(item.link)
        var doc = parsing.getDocument(host + shortLink)

        val matcher = Pattern.compile("\"paginator1\", (\\d+)")
            .matcher(doc.body().html())

        var size = 0
        if (matcher.find()) {
            size = matcher.group().split(", ").last().toInt()
        }

        var counter = 1

        while (true) {
            counter++

            list = list + doc.select(".content #b_image").attr("src")

            if (counter > size) break

            doc = parsing.getDocument("${host + shortLink}/$counter")
        }

        return list
    }

}
