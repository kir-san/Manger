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
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import timber.log.Timber
import java.util.regex.Pattern

abstract class MangachanTemplate(private val connectManager: ConnectManager) :
    SiteCatalogClassic() {

    override val catalog: String
        get() = "$host/manga/new"

    override suspend fun init(): MangachanTemplate {
        val doc = connectManager.getDocument(catalog)
        volume = doc.select("#pagination > b").text().split(" ").first().toInt()
        return this
    }

    override suspend fun elementByUrl(url: String): SiteCatalogElement? = runCatching {
        val doc = connectManager.getDocument(url)
        val infoWrap = doc.select("#content #right #info_wrap")

        val name = infoWrap.select(".name_row > h1 > a").text()
        var type = ""
        var authors = listOf<String>()
        var statusEdition = ""
        var statusTranslate = ""
        var volume = 0
        var genres = listOf<String>()

        infoWrap
            .select(".mangatitle tr")
            .forEach { element ->
                when (element.select("td.item").text()) {
                    "Тип" -> type = element.select("td.item2 > h2 > span > a").text()
                    "Автор" -> authors =
                        element.select("td.item2 > span > a").map { it.text() }.dropLast(1)

                    "Статус (Томов)" -> statusEdition =
                        element.select("td.item2 > h2").text().statusEdition()

                    "Загружено" -> {
                        val s = element.select("td.item2 > h2").text()

                        statusTranslate = s.statusTranslate()

                        val matcher2 = Pattern.compile("\\d+").matcher(s)
                        if (matcher2.find())
                            volume = matcher2.group().toInt()
                    }

                    "Тэги" -> genres = element.select("td.item2 > span > a").map { it.text() }
                }
            }

        val about = doc.select("#content #description").text()
        val logo = doc.select("#content #left #manga_images > a > img").attr("src")
        val populate = doc.select("#content div#left div font b").text().toInt()
        val dateId = doc.select("#content #right .ext .user_link").dateId()

        SiteCatalogElement(
            host = host,
            catalogName = catalogName,
            shortLink = url.split(catalogName).last(),
            link = url,
            type = type,
            name = name,
            authors = authors,
            statusEdition = statusEdition,
            statusTranslate = statusTranslate,
            volume = volume,
            genres = genres,
            about = about,
            logo = logo,
            populate = populate,
            dateId = dateId,
        )
    }.onFailure { Timber.v(it, message = url) }.getOrNull()

    override suspend fun fullElement(element: SiteCatalogElement): SiteCatalogElement {
        return element.copy(host = host)
    }

    open fun simpleParseElement(elem: Element): SiteCatalogElement {
        val name = elem.select("a.title_link").first()?.text() ?: ""
        val shortLink = elem.select("a.title_link").first()?.attr("href") ?: ""
        val link = host + shortLink
        val type = elem.select("a[href*=/type/]").text()
        val authorsTemp = elem.select("a[href*=mangaka]")
        val authors = authorsTemp.filterNot { it == authorsTemp.last() }.map { it.text() }
        val statusEdition = elem.select(".manga_row3 .item2").html().statusEdition()
        val statusTranslate = elem.select(".manga_row3 .item2").toString().statusTranslate()

        val matcher2 = Pattern.compile("\\d+")
            .matcher(elem.select(".manga_row3 b").text())
        val volume = if (matcher2.find()) matcher2.group().toInt() else 0
        val genres = elem.select(".genre > a").map { it.text() }
        val about = elem.select("div.tags").text()
        val logo = elem.select("div.manga_images > a > img").attr("src")
        val populate = elem.select("div.manga_images font b").text().toInt()
        val dateId = elem.select(".manga_row4 .row4_left .user_link_short").dateId()

        return SiteCatalogElement(
            host = host,
            catalogName = catalogName,
            name = name,
            shortLink = shortLink,
            link = link,
            type = type,
            authors = authors,
            statusEdition = statusEdition,
            statusTranslate = statusTranslate,
            volume = volume,
            genres = genres,
            about = about,
            logo = logo,
            populate = populate,
            dateId = dateId,
            isFull = true
        )
    }

    override fun catalog() = flow {
        var docLocal: Document = connectManager.getDocument(catalog)

        suspend fun hasNext(): Boolean {
            val next = docLocal
                .select("#pagination > a:contains(Вперед)")
                .attr("href")

            if (next.isNotEmpty()) docLocal = connectManager.getDocument(catalog + next)

            return next.isNotEmpty()
        }

        do {
            docLocal
                .select("div.content_row")
                .forEach { element -> emit(simpleParseElement(element)) }
        } while (hasNext())
    }

    override suspend fun chapters(manga: Manga) =
        connectManager.getDocument(host + manga.shortLink)
            .select(".table_cha")
            .select("tr")
            .filterNot { it.select("a").text().isEmpty() }
            .map { element ->
                var name = element.select("a").text()
                val pat = Pattern.compile("v.+").matcher(name)
                if (pat.find()) name = pat.group()

                Chapter(
                    manga = manga.name,
                    name = name,
                    date = element.select(".date").text(),
                    link = host + element.select("a").attr("href"),
                    path = "${manga.path}/$name"
                )
            }

    override suspend fun pages(item: Chapter): List<String> {
        var list = listOf<String>()
        val shortLink = getShortLink(item.link)

        val pat = Pattern
            .compile("\"fullimg\":\\[.+")
            .matcher(connectManager.getDocument(host + shortLink).select("#content").html())
        if (pat.find()) {
            list = pat.group()
                .removeSuffix(",]")
                .removePrefix("\"fullimg\":[")
                .split(",")
        }
        return list
    }

    private fun String.statusTranslate(): String = when {
        contains(Translate.COMPLETE, true) -> Translate.COMPLETE
        contains(Translate.NOT_COMPLETE, true) -> Translate.NOT_COMPLETE
        contains(Translate.FREEZE, true) -> Translate.FREEZE
        else -> Translate.UNKNOWN
    }

    private fun String.statusEdition(): String = when {
        contains(Status.COMPLETE, true) -> Status.COMPLETE
        contains(Status.NOT_COMPLETE, true) -> Status.NOT_COMPLETE
        contains(Status.SINGLE, true) -> Status.SINGLE
        else -> Status.UNKNOWN
    }

    private fun Elements.dateId(): Int {
        val matcher = Pattern.compile("\\d+").matcher(first()?.id() ?: "")
        return if (matcher.find()) matcher.group().toInt() else 0
    }
}
