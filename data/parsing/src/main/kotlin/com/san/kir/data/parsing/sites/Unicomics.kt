package com.san.kir.data.parsing.sites

import com.san.kir.core.internet.ConnectManager
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.SiteCatalogElement
import com.san.kir.data.parsing.SiteCatalogClassic
import com.san.kir.data.parsing.Status
import com.san.kir.data.parsing.Translate
import com.san.kir.data.parsing.getShortLink
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Element
import timber.log.Timber
import java.util.regex.Pattern

class Unicomics(private val connectManager: ConnectManager) : SiteCatalogClassic() {

    override val host: String
        get() = "https://$catalogName"

    override val name: String = "UniComics"
    override val catalogName: String = "unicomics.ru"
    override val catalog: String = "$host/map"
    override var volume = 0

    override suspend fun init(): Unicomics {
        val doc = connectManager.getDocument(catalog)
        volume = doc.select(".content .block table tr a").size
        return this
    }

    override suspend fun elementByUrl(url: String): SiteCatalogElement? = runCatching {
        fullElement(
            SiteCatalogElement(
                host = host,
                catalogName = catalogName,
                shortLink = url.split(catalogName).last(),
                link = url,
                type = "Комикс"
            )
        )
    }.onFailure { Timber.v(it, message = url) }.getOrNull()

    override suspend fun fullElement(element: SiteCatalogElement): SiteCatalogElement {
        val doc = connectManager.getDocument(element.link)
        val info = doc.select(".content .left_container .info")
        val name = info.select("h1").text()

        val logo = doc.select(".content .left_container .image img").attr("src")
        val about = runCatching { info.select("p").last()!!.text() }.getOrNull() ?: ""

        var authors = listOf<String>()
        var genres = listOf<String>()
        var volume = 0
        info.select("table > tbody > tr").forEach { tr ->
            when {
                AUTHOR in tr.text() -> authors = tr.select("td > a").map { it.text() }
                GENRE in tr.text() -> genres = tr.select("td > a").map { it.text() }.drop(1)
                VOLUME in tr.text() -> volume = tr.select("td").last()?.text()
                    ?.split(" ")?.last()?.toIntOrNull() ?: 0
            }
        }

        if (volume == 0) volume = chapters(element.link).size

        return element.copy(
            name = name,
            statusEdition = Status.UNKNOWN,
            statusTranslate = Translate.UNKNOWN,
            logo = logo,
            authors = authors,
            genres = genres,
            about = about,
            volume = volume,
            isFull = true
        )
    }

    private fun simpleParseElement(elem: Element): SiteCatalogElement {
        return SiteCatalogElement(
            host = host,
            catalogName = catalogName,
            name = elem.text(),
            shortLink = elem.attr("href"),
            link = host + elem.attr("href"),
            type = "Комикс"
        )
    }

    override fun catalog() = flow {
        val doc = connectManager.getDocument(catalog).select(".content .block table tr a")
        doc.forEach { link -> emit(simpleParseElement(link)) }
    }

    override suspend fun chapters(manga: Manga): List<Chapter> {
        return chapters(host + manga.shortLink)
            .map {
                val link = it.select("table > tbody .online > a").attr("href")
                val name = it.select("a.list_title").text()
                Chapter(
                    mangaId = manga.id,
                    name = name,
                    link = if (host in link) link else host + link,
                    path = "${manga.path}/${name}"
                )
            }
    }

    private suspend fun chapters(url: String): List<Element> {
        var doc = connectManager.getDocument(url)

        var counter = 1
        var oldSize = 0

        var links = listOf<Element>()
        while (true) {
            counter++

            links = links + doc.select(".content .left_container .list_comics .right_comics")
            if (oldSize == links.size) break

            oldSize = links.size

            doc = connectManager.getDocument("$url/page/$counter")
        }

        return links
    }

    override suspend fun pages(item: Chapter): List<String> {
        var list = listOf<String>()
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

    private companion object {
        private const val AUTHOR = "Издательство:"
        private const val VOLUME = "Выпуски:"
        private const val GENRE = "Жанр:"
    }

}
