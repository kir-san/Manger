package com.san.kir.data.parsing.sites

import com.google.gson.GsonBuilder
import com.san.kir.core.internet.ConnectManager
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.SiteCatalogElement
import com.san.kir.data.parsing.SiteCatalogClassic
import com.san.kir.data.parsing.Translate
import io.ktor.http.Parameters
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import timber.log.Timber

class ComX(private val connectManager: ConnectManager) : SiteCatalogClassic() {
    override val name = "COM-X.LIFE"
    override val catalogName = "com-x.life"
    override val host = "https://$catalogName"
    override val catalog = "$host/comix-read"
    override var volume = 0

    override suspend fun init(): ComX {
        var lastPage = 967
        val itemPerPage = 10

        volume = (lastPage - 1) * itemPerPage

        while (true) {
            val size = getDocument("$catalog/page/${lastPage++}")
                .select("#dle-content div.readed")
                .size

            volume += size

            if (size < 10) break
        }

        return this
    }

    override suspend fun elementByUrl(url: String): SiteCatalogElement? = runCatching {
        val document = getDocument(url)
        val doc = document.select("#dle-content")

        val name = doc.select(".page__header h1").text()

        val link = doc.select(".page__poster img").attr("data-src")
        val logo = if (catalogName in link) link else host + link

        var authors = listOf<String>()
        var statusEdition = ""
        var type = ""

        doc.select(".page__list > li").forEach { p ->
            val text = p.text()
            when {
                PUBLISHER in text -> authors = listOf(text.splitAndRemoveSurround())
                STATUS in text -> statusEdition = text.splitAndRemoveSurround()
                TYPE in text -> type = text.splitAndRemoveSurround()
            }
        }

        val genres = doc.select(".page__tags > a").map { it.text() }
        val shortLink = url.split(catalogName).last()

        fullElement(
            SiteCatalogElement(
                host = host,
                catalogName = catalogName,
                shortLink = shortLink,
                link = url,
                name = name,
                logo = logo,
                authors = authors,
                genres = genres,
                statusEdition = statusEdition,
                type = type,
            )
        )
    }.onFailure { Timber.v(it, message = url) }.getOrNull()

    override suspend fun fullElement(element: SiteCatalogElement): SiteCatalogElement {
        val doc = getDocument(element.link).select("#dle-content")

        val about = doc.select(".page__main .tabs__block .full-text").text()

        val volume =
            doc.select(".page__main .tabs__select-item")
                .firstOrNull { CHAPTERS in it.text() }
                ?.text()?.split("(")?.last()
                ?.removeSuffix(")")?.toIntOrNull() ?: 0

        return element.copy(
            about = about,
            statusTranslate = Translate.UNKNOWN,
            volume = volume,
            isFull = true
        )
    }

    private fun simpleParseElement(elem: Element): SiteCatalogElement {
        val name = elem.select(".readed__title > a").text()

        val link = elem.select(".readed__title > a").attr("href")
        val shotLink = link.split(catalogName).last()

        val authors = listOf(
            elem.selectFirst("div.readed__meta-item")?.text() ?: ""
        )
        val genres = elem
            .select("ul.readed__info > li > a")
            .map { it.text() }

        val about = elem
            .selectFirst("ul.readed__info > li > span")?.text().toString()

        val populate = elem.select("ul.unit-rating > li.current-ratting").text().toIntOrNull() ?: 0

        return SiteCatalogElement(
            host = host,
            link = link,
            shortLink = shotLink,
            catalogName = catalogName,
            name = name,
            authors = authors,
            genres = genres,
            about = about,
            populate = populate,
            dateId = shotLink.split("-").first().removePrefix("/").toIntOrNull() ?: 0
        )
    }

    override fun catalog() = flow {
        connectManager
            .getDocument(catalog)
            .select("#dle-content div.readed")
            .forEach { emit(simpleParseElement(it)) }

        var page = 2
        while (connectManager
                .getDocument("$catalog/page/${page++}")
                .select("#dle-content div.readed")
                .onEach { emit(simpleParseElement(it)) }
                .size == 10) {
        }
    }

    override suspend fun chapters(manga: Manga): List<Chapter> {
        val lastChapterLink =
            getDocument(host + manga.shortLink)
                .select("ul.comix-list > li > a")
                .first()
                ?.attr("href")

        val jsonData = jsonData(host + lastChapterLink)
        return jsonData.chapters.map { chapter ->
            Chapter(
                mangaId = manga.id,
                name = chapter.title,
                link = "$host/readcomix/${jsonData.news_id}/${chapter.id}.html",
                path = "${manga.path}/${chapter.title}",
            )
        }
    }

    override suspend fun pages(item: Chapter): List<String> {
        return jsonData(item.link).images.map { "http://img.$catalogName/comix/$it" }
    }

    private val gson = GsonBuilder().setLenient().create()
    private suspend fun jsonData(url: String): ChaptersData {
        val temp = getDocument(url)
            .select("script")
            .filterNot { it.toString().contains("window.__DATA__").not() }
            .toString()
            .removePrefix("[<script>window.__DATA__ = ")
            .removeSuffix(";</script>]")

        return gson.fromJson(temp, ChaptersData::class.java)
    }

    // Обертка над обычным вызовом, чтобы обходить проверку на работа
    private suspend fun getDocument(url: String): Document {
        delay(1000L)
        var document = connectManager.getDocument(url)
        var isRetry: Boolean

        do {
            isRetry = false
            if (document.html().contains("Если вы человек, нажмите на кнопку с цветом,")) {
                Timber.v("проверка на робота")
                isRetry = true
                val temp = document.select("script")
                    .toString()
                    .split("function Button() {")
                    .last()

                val tempDoc = Jsoup.parse(temp)

                val colorValue = tempDoc.select("button").map {
                    it.attr("value").removeSurrounding("\\\"")
                }.random()

                val formData = Parameters.build {
                    tempDoc.select("input").forEach {
                        append(
                            it.attr("name").removeSurrounding("\\\""),
                            it.attr("value").removeSurrounding("\\\"")
                        )
                    }

                    append("color", colorValue)
                }

                delay(1000L)
                document = connectManager.getDocument(url, formParams = formData)
            } else if (document.html().contains("<script>document.location=")) {
                Timber.v("странный ретрай")
                isRetry = true
                document = connectManager.getDocument(url)
            }
            Timber.v("isRetry = $isRetry")
        } while (isRetry)

        Timber.v("url is $url")

        return document
    }

    companion object {
        private const val STATUS = "Статус"
        private const val TYPE = "Тип выпуска"
        private const val PUBLISHER = "Издатель"
        private const val CHAPTERS = "Главы"

        private fun String.splitAndRemoveSurround() = split(":").last().trim()
    }
}

private data class ChaptersData(
    val is_logged: Int,
    val chapters: List<CH>,
    val images: List<String>,
    val pages: Int,
    val comix_id: Int,
    val news_id: Int,
    val prev: String,
    val next: String,
    val readed: Boolean,
    val bookmark: Int,
)

private data class CH(
    val id: Int,
    val title: String,
    val title_en: String,
)
