package com.san.kir.data.parsing.sites

import com.san.kir.core.internet.AuthorizationException
import com.san.kir.core.internet.ConnectManager
import com.san.kir.data.models.catalog.SiteCatalogElement
import com.san.kir.data.models.main.Chapter
import com.san.kir.data.models.main.Manga
import com.san.kir.data.parsing.SiteCatalogClassic
import com.san.kir.data.parsing.Status
import com.san.kir.data.parsing.Translate
import com.san.kir.data.parsing.getShortLink
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import timber.log.Timber
import java.util.regex.Pattern

internal abstract class ReadmangaTemplate(protected val connectManager: ConnectManager) :
    SiteCatalogClassic() {

    override val catalog
        get() = "$host/list?sortType=created"

    open val categories = listOf(
        "Ёнкома",
        "Комикс западный",
        "Манхва",
        "Маньхуа",
        "В цвете",
        "Веб",
        "Сборник"
    )

    override suspend fun init(): ReadmangaTemplate {
        val doc = connectManager.getDocument(catalog)
        volume = doc.select("#mangaBox .leftContent .pagination a.step")
            .last { it.attr("href").contains("offset", true) }
            .attr("href")
            .split("&")
            .first { it.contains("offset", true) }
            .split("=")
            .last().toInt()
        return this
    }

    override suspend fun elementByUrl(url: String) = kotlin.runCatching {
        val doc = connectManager.getDocument(url)

        if (checkAuthorization(doc)) throw AuthorizationException()

        val name = doc.select("#mangaBox .leftContent span.name").text()
        val shortLink = allCatalogName
            .map { url.split(it) }
            .last { it.size > 1 }
            .last()

        val status = doc.select("#mangaBox .leftContent .expandable .subject-meta p")

        val statusEdition = when {
            status.first()?.text()?.contains(Status.SINGLE, true) == true -> Status.SINGLE
            status.first()?.text()?.contains(Status.NOT_COMPLETE, true) == true -> Status.NOT_COMPLETE
            else -> Status.COMPLETE
        }

        val statusTranslate =
            if (status[1].text().contains("продолжается", true))
                Translate.NOT_COMPLETE
            else Translate.COMPLETE

        fullElement(
            SiteCatalogElement(
                host = host,
                catalogName = catalogName,
                link = url,
                name = name,
                shortLink = shortLink,
                statusEdition = statusEdition,
                statusTranslate = statusTranslate
            )
        )
    }.onFailure { Timber.v(it, message = url) }.getOrNull()

    override suspend fun fullElement(element: SiteCatalogElement): SiteCatalogElement {
        val doc = connectManager.getDocument(element.link)

        if (checkAuthorization(doc)) throw AuthorizationException()

        val content = doc.select("div.leftContent")
        var type = "Манга"
        content.select(".flex-row .subject-meta .elem_category").forEach {
            if (categories.contains(it.select("a").text()))
                type = it.select("a").text()
        }

        val volume = maxOf(content.select(".chapters-link tr").size - 1, 0)
        val about = content.select("meta[itemprop=description]").attr("content")
        val logo = content.select(".expandable .subject-cover img").attr("src")

        val authors = content
            .select(".flex-row .elementList .elem_author")
            .map { it.select(".person-link").text() }
            .ifEmpty {
                content.select(".flex-row .elementList .elem_publisher span").map { it.text() }
            }

        var genres = emptyList<String>()
        content.select(".flex-row .subject-meta .elementList")
            .forEach { elem ->
                if (elem.text().contains("Жанры")) {
                    genres = elem.select("a.element-link").map { it.text() }
                }
            }


        return element.copy(
            type = type,
            volume = volume,
            about = about,
            logo = logo,
            authors = authors,
            genres = genres,
            isFull = true
        )
    }

    private fun simpleParseElement(elem: Element): SiteCatalogElement {

        val name = elem.select(".img a").select("img").attr("title")
        val shotLink = elem.select(".img a").attr("href")

        var statusEdition = when {
            elem.select("span.mangaCompleted").text().isNotEmpty() -> Status.COMPLETE
            elem.select("span.mangaSingle").text().isNotEmpty() -> Status.SINGLE
            else -> Status.NOT_COMPLETE
        }

        val statusTranslate =
            if (elem.select("span.mangaTranslationCompleted").text().isNotEmpty()) {
                statusEdition = Status.COMPLETE
                Translate.COMPLETE
            } else
                Translate.NOT_COMPLETE

        val logo = elem.select(".img a").select("img").attr("data-original")
        val dateId = elem.select("span.bookmark-menu").attr("data-id").toIntOrNull() ?: 0

        val populate = kotlin.runCatching {
            (elem.select(".desc .star-rate .rating")
                .attr("title")
                .split(" ")
                .first()
                .toFloat() * 10_000)
                .toInt()
        }.getOrNull() ?: 0

        val genres = elem.select(".desc .tile-info a.element-link").map { it.text() }

        return SiteCatalogElement(
            host = this.host,
            catalogName = this.catalogName,
            name = name,
            shortLink = shotLink,
            link = host + shotLink,
            statusEdition = statusEdition,
            statusTranslate = statusTranslate,
            logo = logo,
            dateId = dateId,
            populate = populate,
            type = "Манга",
            genres = genres
        )
    }

    override fun catalog() = flow {
        var docLocal: Document = connectManager.getDocument(catalog)

        if (checkAuthorization(docLocal)) throw AuthorizationException()

        suspend fun isGetNext(): Boolean {
            val next = docLocal.select(".pagination > a.nextLink").attr("href")
            if (next.isNotEmpty()) docLocal = connectManager.getDocument(host + next)
            return next.isNotEmpty()
        }

        do {
            docLocal
                .select("div.tile")
                .forEach { element -> emit(simpleParseElement(element)) }
        } while (isGetNext())
    }

    override suspend fun chapters(manga: Manga) =
        connectManager.getDocument(host + manga.shortLink)
            .select("#chapters-list")
            .select("tr")
            .filterNot {
                val text = it.select("a").text()
                text.isEmpty() || text.contains("Загрузить обложку")
            }
            .map {
                var name = it.select(".item-title > a").text()
                val pat = Pattern.compile("v.+").matcher(name)
                if (pat.find())
                    name = pat.group()
                Chapter(
                    mangaId = manga.id,
                    name = name,
                    date = it.select("td").last()?.text() ?: "",
                    link = host + it.select(".item-title > a").attr("href"),
                    _path = "${manga.path}/$name"
                )
            }

    override suspend fun pages(item: Chapter): List<String> {

        val shortLink = getShortLink(item.link)

        //        delay(1.seconds)

        return runCatching {
            val doc = documentForPages(shortLink)

            if (checkAuthorization(doc)) throw AuthorizationException()
            val html = doc.body().html()

            var list = tryReaderDoInit(html)
            if (list.isEmpty()) {
                list = tryReaderInit(html)
            }

            val clearUrls = if (list.isNotEmpty()) {
                if (list.any { it.contains("/static/deleted.jpg", true) }) throw AuthorizationException()

                val response = runCatching { connectManager.url(list.first()) }.getOrNull()
                Timber.w("$response")

                if (response != null && !response.request.url.host.contains("wikimedia", true)) {
                    response.status in listOf(HttpStatusCode.Forbidden, HttpStatusCode.MultipleChoices)
                } else {
                    true
                }
            } else false

            return list.map {
                if (it.contains("?t=") && clearUrls) it.split("?t=").first()
                else it
            }
        }.onFailure(Timber.Forest::e).getOrThrow()
    }

    open fun checkAuthorization(document: Document) = false

    protected open suspend fun documentForPages(shortLink: String): Document {
        return connectManager.getDocument("$host$shortLink?mtr=1")
    }

    private fun tryReaderDoInit(html: String): List<String> {
        val list = mutableListOf<String>()
        val pat = Pattern.compile("rm_h.readerDoInit\\(.+").matcher(html)
        if (pat.find()) {
            val group = pat.group()
            val data = group.replace("rm_h.readerDoInit(", "")
            Timber.v("data: $data")
            val json = JSONArray(data)
            Timber.v("json: $json")
            repeat(json.length()) { index ->
                val jsonArray = json.getJSONArray(index)
                val url = jsonArray.getString(0) + jsonArray.getString(2)
                list.add(url)
            }
        }
        return list
    }

    private fun tryReaderInit(html: String): List<String> {
        val list = mutableListOf<String>()
        val pat = Pattern.compile("rm_h.readerInit\\(.+").matcher(html)
        if (pat.find()) {
            val group = pat.group()
            val data = group.replace("rm_h.readerInit(", "[").replace(");", "]")
            Timber.v("data: $data")
            var json = JSONArray(data).optJSONArray(1)
            if (json == null) json = JSONArray(data).optJSONArray(0)
            if (json == null) return emptyList()
            Timber.v("json: $json")
            repeat(json.length()) { index ->
                val jsonArray = json.getJSONArray(index)
                val url = jsonArray.getString(0) + jsonArray.getString(2)
                list.add(url)
            }
        }
        return list
    }
}
