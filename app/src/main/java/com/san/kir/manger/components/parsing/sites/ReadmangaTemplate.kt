package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.SiteCatalogClassic
import com.san.kir.manger.components.parsing.Status
import com.san.kir.manger.components.parsing.Translate
import com.san.kir.manger.components.parsing.getShortLink
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.utils.extensions.createDirs
import com.san.kir.manger.utils.extensions.getFullPath
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.regex.Pattern

abstract class ReadmangaTemplate(private val siteRepository: SiteRepository) : SiteCatalogClassic() {
    override val siteCatalog
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
        if (!isInit) {
            oldVolume = siteRepository.getItem(name)?.volume ?: 0
            val doc = ManageSites.getDocument(siteCatalog)
            volume = doc.select("#mangaBox .leftContent .pagination a.step")
                .last {
                    val text = it.attr("href")
                    text.contains("offset", true)
                }
                .attr("href")
                .split("&")
                .first { it.contains("offset", true) }
                .split("=")
                .last().toInt()
            isInit = true
        }
        return this
    }

    override suspend fun getElementOnline(url: String): SiteCatalogElement? {
        return kotlin.runCatching {
            val element = SiteCatalogElement()

            val doc = ManageSites.getDocument(url)

            element.host = host
            element.catalogName = catalogName
            element.siteId = id

            element.name = doc.select("#mangaBox .leftContent span.name").text()

            element.shotLink = url.split(catalogName).last()
            element.link = url

            val status = doc.select("#mangaBox .leftContent .expandable .subject-meta p")

            element.statusEdition = Status.COMPLETE
            if (status.first().text().contains(Status.SINGLE, true))
                element.statusEdition = Status.SINGLE
            else if (status.first().text().contains(Status.NOT_COMPLETE, true))
                element.statusEdition = Status.NOT_COMPLETE

            element.statusTranslate = Translate.COMPLETE
            if (status[1].text().contains("продолжается", true)) {
                element.statusTranslate = Translate.NOT_COMPLETE
            }

            element.logo = doc.select("#mangaBox .leftContent .expandable .subject-cower img").attr("src")

            getFullElement(element)
        }.fold(onSuccess = { it },
               onFailure = { null })
    }

    override suspend fun getFullElement(element: SiteCatalogElement): SiteCatalogElement {

        val doc = ManageSites.getDocument(element.link).select("div.leftContent")

        element.type = "Манга"
        doc.select(".flex-row .subject-meta .elem_tag").forEach {
            if (categories.contains(it.select("a").text()))
                element.type = it.select("a").text()
        }

        element.authors = emptyList()
        val authorsTemp = doc.select(".flex-row .elementList .elem_author")
        element.authors = authorsTemp.map { it.select(".person-link").text() }

        val volume = doc.select(".chapters-link tr").size - 1
        element.volume = if (volume < 0) 0 else volume

        element.genres.clear()
        doc.select("span.elem_genre")
            .forEach { element.genres.add(it.select("a.element-link").text()) }

        element.about = doc.select("meta[itemprop=description]").attr("content")

        element.isFull = true


        return element
    }

    private fun simpleParseElement(elem: Element): SiteCatalogElement {
        val element = SiteCatalogElement()

        element.host = host
        element.catalogName = catalogName
        element.siteId = id

        element.name = elem.select(".img a").select("img").attr("title")

        element.shotLink = elem.select(".img a").attr("href")
        element.link = host + element.shotLink

        element.statusEdition = "Выпуск продолжается"
        if (elem.select("span.mangaCompleted").text().isNotEmpty())
            element.statusEdition = "Выпуск завершен"
        else if (elem.select("span.mangaSingle").text().isNotEmpty() and (element.volume > 0))
            element.statusEdition = "Сингл"

        element.statusTranslate = "Перевод продолжается"
        if (elem.select("span.mangaTranslationCompleted").text().isNotEmpty()) {
            element.statusTranslate = "Перевод завершен"
            element.statusEdition = "Выпуск завершен"
        }

        element.logo = elem.select(".img a").select("img").attr("data-original")

        element.dateId = elem.select("span.bookmark-menu").attr("data-id").toInt()

        try {
            element.populate = elem.select(".desc .tile-info p.small").first().ownText().toInt()
        } catch (ex: Throwable) {
            element.populate = 0
        }

        element.type = "Манга"

        element.authors = elem.select(".desc .tile-info .person-link")
            .map { it.select(".person-link").text() }

        elem.select(".desc .tile-info a.element-link").forEach { element.genres.add(it.text()) }

        return element
    }

    override fun getCatalog() = flow {
        var docLocal: Document = ManageSites.getDocument(siteCatalog)

        fun isGetNext(): Boolean {
            val next = docLocal.select(".pagination > a.nextLink").attr("href")
            if (next.isNotEmpty())
                docLocal = ManageSites.getDocument(host + next)
            return next.isNotEmpty()
        }

        do {
            docLocal.select("div.tile").forEach { element ->
                emit(simpleParseElement(element))
            }
        } while (isGetNext())
    }
    ///


    override suspend fun chapters(manga: Manga) =
        ManageSites.getDocument(host + manga.shortLink)
            .select("div.leftContent .chapters-link")
            .select("tr")
            .filter { it.select("a").text().isNotEmpty() }
            .map {
                var name = it.select("a").text()
                val pat = Pattern.compile("v.+").matcher(name)
                if (pat.find())
                    name = pat.group()
                Chapter(
                    manga = manga.unic,
                    name = name,
                    date = it.select("td").last().text(),
                    site = host + it.select("a").attr("href"),
                    path = "${manga.path}/$name"
                )
            }

    override suspend fun pages(item: DownloadItem): List<String> {
        val list = mutableListOf<String>()
        // Создаю папку/папки по указанному пути
        (getFullPath(item.path)).createDirs()

        val shortLink = getShortLink(item.link)

        val doc = ManageSites.getDocument("$host$shortLink?mature=1")
        // с помощью регулярных выражений ищу нужные данные
        val pat = Pattern.compile("rm_h.init.+").matcher(doc.body().html())
        // если данные найдены то продолжаю
        if (pat.find()) {
            // избавляюсь от ненужного и разделяю строку в список и отправляю
            val data = pat.group()
                .removeSuffix(", 0, false);")
                .removePrefix("rm_h.init( ")

            val json = JSONArray(data)

            repeat(json.length()) { index ->
                val jsonArray = json.getJSONArray(index)
                val url = jsonArray.getString(1) +
                        jsonArray.getString(0) +
                        jsonArray.getString(2)
                list += url
            }
        }
        return list
    }
}
