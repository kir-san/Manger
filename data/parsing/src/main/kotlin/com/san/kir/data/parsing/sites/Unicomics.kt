package com.san.kir.data.parsing.sites

import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.utils.createDirs
import com.san.kir.core.utils.getFullPath
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.DownloadItem
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.SiteCatalogElement
import com.san.kir.data.parsing.SiteCatalogClassic
import com.san.kir.data.parsing.Status
import com.san.kir.data.parsing.Translate
import com.san.kir.data.parsing.getShortLink
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Element
import java.util.regex.Pattern

class Unicomics(private val connectManager: ConnectManager) : SiteCatalogClassic() {

    override val host: String
        get() = "https://$catalogName"

    override val name: String = "UniComics"
    override val catalogName: String = "unicomics.ru"
    override val siteCatalog: String = "$host/map"
    override var volume = 0

    override suspend fun init(): Unicomics {
        if (!isInit) {
            val doc = connectManager.getDocument(siteCatalog)
            volume = doc.select(".content .block table tr a").size
            isInit = true
        }
        return this
    }

    override suspend fun getElementOnline(url: String): SiteCatalogElement? = runCatching {
        val element = SiteCatalogElement()

        element.host = host
        element.catalogName = catalogName

        element.shotLink = url.split(catalogName).last()
        element.link = url

        element.type = "Комикс"

        getFullElement(element)
    }.fold(onSuccess = { it },
           onFailure = { null })

    override suspend fun getFullElement(element: SiteCatalogElement): SiteCatalogElement {
        val doc = connectManager.getDocument(element.link)

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

        runCatching { info.select("p").last()!!.text() }.onSuccess { element.about = it }

        element.volume = getChapters(element.link).size

        element.isFull = true

        return element
    }

    private fun simpleParseElement(elem: Element): SiteCatalogElement {
        val element = SiteCatalogElement()

        element.host = host
        element.catalogName = catalogName

        element.name = elem.text()

        element.shotLink = elem.attr("href")
        element.link = host + element.shotLink

        element.type = "Комикс"

        return element
    }

    override fun getCatalog() = flow {
        val doc = connectManager.getDocument(siteCatalog).select(".content .block table tr a")

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
                    manga = manga.name,
                    name = name,
                    link = if (link.contains(host)) link else host + link,
                    path = "${manga.path}/${name}"
                )
            }
    }

    private suspend fun getChapters(url: String): List<Element> {
        var links = listOf<Element>()

        var doc = connectManager.getDocument(url)

        var counter = 1
        var oldSize = 0

        while (true) {
            counter++

            links = links + doc.select(".content .left_container .list_comics .right_comics")
            if (oldSize == links.size) break

            oldSize = links.size

            doc = connectManager.getDocument("$url/page/$counter")
        }

        return links
    }

    override suspend fun pages(item: DownloadItem): List<String> {
        var list = listOf<String>()
        getFullPath(item.path).createDirs()

        val shortLink = getShortLink(item.link)
        var doc = connectManager.getDocument(host + shortLink)

        val matcher = Pattern.compile("\"paginator1\", (\\d+)")
            .matcher(doc.body().html())

        var size = 0
        if (matcher.find()) {
            size = matcher.group().split(", ").last().toInt()
        }

        var counter = 1

        while (true) {
            counter++

            list = list + doc.select("#b_image").attr("src")

            if (counter > size) break

            doc = connectManager.getDocument("${host + shortLink}/$counter")
        }

        return list
    }

}
