package com.san.kir.manger.components.parsing.sites

import com.google.gson.GsonBuilder
import com.san.kir.manger.components.parsing.ConnectManager
import com.san.kir.manger.components.parsing.SiteCatalogClassic
import com.san.kir.manger.components.parsing.Translate
import com.san.kir.manger.components.parsing.postRequest
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.utils.extensions.log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import okhttp3.FormBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class ComX(private val connectManager: ConnectManager) : SiteCatalogClassic() {
    override val name: String = "COM-X.LIFE"
    override val catalogName: String = "com-x.life"
    override val host: String
        get() = "https://$catalogName"
    override val siteCatalog: String =
        "$host/ComicList"
    override var volume = 0

    override suspend fun init(): ComX {
        if (!isInit) {
            val lastPage = 150
            val itemPerPage = 50
            volume = lastPage * itemPerPage

            var docLocal = getDocument("$siteCatalog/page/${lastPage + 1}")

            suspend fun isGetNext(): Boolean {
                val next = docLocal.select(".bnnavi > .nextprev > a")
                val check = next.select(".pnext")

                if (check.isNotEmpty())
                    docLocal = getDocument(next.last().attr("href"))

                return check.isNotEmpty()
            }

            do {
                val additionalSize = docLocal
                    .select("#dle-content")
                    .select("div.comiclist-item")
                    .size
                volume += additionalSize
            } while (isGetNext())

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

        val document = getDocument(url)
        val doc = document.select("#dle-content .fullstory")
        element.name = doc.select(".dpad h1").text()

        val link = doc.select(".fullstory__poster img").attr("src")
        element.logo = if (link.contains(element.host)) link else element.host + link

        doc.select(".fullstory__infoSection").forEach { p ->
            when {
                p.text().contains("Издатель") -> {
                    element.authors = listOf(p.select(".fullstory__infoSectionContent").text())
                }
                p.text().contains("Жанр") -> {
                    element.genres = p.select(".fullstory__infoSectionContent a").map { it.text() }
                }
                p.text().contains("Статус") -> {
                    element.statusEdition = p.select(".fullstory__infoSectionContent").text()
                }
                p.text().contains("Тип выпуска") -> {
                    element.type = p.select(".fullstory__infoSectionContent").text()
                }
            }
        }

        element.dateId = element.shotLink.split("-").first().removePrefix("/").toInt()

        getFullElement(element)
    }.fold(onSuccess = { it },
        onFailure = {
            it.printStackTrace()
            null
        })

    override suspend fun getFullElement(element: SiteCatalogElement): SiteCatalogElement {
        val doc = getDocument(element.link).select("#dle-content .fullstory")

        element.about = doc.select(".fullstory__mainInfo > div[style~=margin]").text()

        element.statusTranslate = Translate.UNKNOWN
        if (doc.select(".fullstory__infoSection").text().contains("Комикс перевели")) {
            element.statusTranslate = Translate.COMPLETE
        } else {
            element.statusTranslate = Translate.NOT_COMPLETE
        }

        element.volume =
            doc.select("ul.comix-list > li > i").first().text().removePrefix("#").toInt()

        element.isFull = true

        return element
    }

    private fun simpleParseElement(elem: Element): SiteCatalogElement {
        val element = SiteCatalogElement()

        element.host = host
        element.catalogName = catalogName

        element.name = elem.select("a.comiclist-item-title").text()

        element.link = elem.select("a.comiclist-item-title").attr("href")
        element.shotLink = element.link.split(catalogName).last()

        elem.select("div.comiclist-item-popup p").forEach { p ->
            when {
                p.text().contains("Издатель") -> {
                    element.authors =
                        listOf(p.text().split(":").last().removeSurrounding(" ", "\""))
                }
                p.text().contains("Жанр") -> {
                    element.genres = p.select("a").map { it.text() }
                }
                p.text().contains("Статус") -> {
                    element.statusEdition = p.text().split(":").last().removeSurrounding(" ", "\"")
                }
                p.text().contains("Тип выпуска") -> {
                    element.type = p.text().split(":").last().removeSurrounding(" ", "\"")
                }
            }
        }

        element.dateId = element.shotLink.split("-").first().removePrefix("/").toInt()

        return element
    }

    override fun getCatalog() = flow {
        var docLocal = getDocument(siteCatalog)

        suspend fun isGetNext(): Boolean {
            val next = docLocal.select(".bnnavi > .nextprev > a")
            val check = next.select(".pnext")

            if (check.isNotEmpty())
                docLocal = getDocument(next.last().attr("href"))

            return check.isNotEmpty()
        }
        do {
            docLocal.select("#dle-content").select("div.comiclist-item").forEach { element ->
                emit(simpleParseElement(element))
            }
        } while (isGetNext())
    }

    override suspend fun chapters(manga: Manga): List<Chapter> {
        val lastChapterLink =
            getDocument(host + manga.shortLink)
                .select("ul.comix-list > li > a")
                .first()
                .attr("href")

        val jsonData = jsonData(host + lastChapterLink)
        return jsonData.chapters.map { chapter ->
            Chapter(
                manga = manga.unic,
                name = chapter.title,
                link = "$host/readcomix/${jsonData.news_id}/${chapter.id}.html",
                path = "${manga.path}/${chapter.title}",
            )
        }
    }

    override suspend fun pages(item: DownloadItem): List<String> {
        return jsonData(item.link).images.map { "http://img.$catalogName/comix/$it" }
    }

    private val gson = GsonBuilder().setLenient().create()
    private suspend fun jsonData(url: String): ChaptersData {
        val temp = getDocument(url)
            .select("script")
            .filter { it.toString().contains("window.__DATA__") }
            .toString()
            .removePrefix("[<script>window.__DATA__ = ")
            .removeSuffix(";</script>]")

        return gson.fromJson(temp, ChaptersData::class.java)
    }

    private suspend fun getDocument(url: String): Document {
        delay(1000L)
        var document = connectManager.getDocument(url)
        var isRetry: Boolean

        do {
            isRetry = false
            if (document.html().contains("Если вы человек, нажмите на кнопку с цветом,")) {
                log("проверка на робота")
                isRetry = true
                val temp = document.select("script")
                    .toString()
                    .split("function Button() {")
                    .last()
                val tempDoc = Jsoup.parse(temp)
                val formData = FormBody.Builder()
                tempDoc.select("input").forEach {
                    formData.add(
                        it.attr("name").removeSurrounding("\\\""),
                        it.attr("value").removeSurrounding("\\\"")
                    )
                }
                val colorValue = tempDoc.select("button").map {
                    it.attr("value").removeSurrounding("\\\"")
                }.random()

                formData.add("color", colorValue)

                delay(1000L)
                document = connectManager.getDocument(request = url.postRequest(formData.build()))
            } else if (document.html().contains("<script>document.location=")) {
                log("странный ретрай")
                isRetry = true
                document = connectManager.getDocument(url)
            }
            log("isRetry = $isRetry")
        } while (isRetry)

        log("url is $url")
//        log("parser start \n")
//        log(document.toString())
//        log("\nparser stop")

        return document
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
