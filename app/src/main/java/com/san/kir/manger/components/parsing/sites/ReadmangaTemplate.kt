package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.ConnectManager
import com.san.kir.manger.components.parsing.SiteCatalogClassic
import com.san.kir.manger.components.parsing.Status
import com.san.kir.manger.components.parsing.Translate
import com.san.kir.manger.components.parsing.getShortLink
import com.san.kir.manger.data.room.entities.Chapter
import com.san.kir.manger.data.room.entities.DownloadItem
import com.san.kir.manger.data.room.entities.Manga
import com.san.kir.manger.data.room.entities.SiteCatalogElement
import com.san.kir.manger.utils.extensions.createDirs
import com.san.kir.manger.utils.extensions.getFullPath
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.regex.Pattern
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

abstract class ReadmangaTemplate(private val connectManager: ConnectManager) :
    SiteCatalogClassic() {

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
            val doc = connectManager.getDocument(siteCatalog)
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

            val doc = connectManager.getDocument(url)

            element.host = host
            element.catalogName = catalogName

            element.name = doc.select("#mangaBox .leftContent span.name").text()

            allCatalogName.forEach { name ->
                url.split(name).apply {
                    if (size > 1) element.shotLink = last()
                }
            }
            element.shotLink =
                allCatalogName
                    .map { name -> url.split(name) }
                    .last { it.size > 1 }
                    .last()

            element.link = url

            val status = doc.select("#mangaBox .leftContent .expandable .subject-meta p")

            // Статус выпуска
            element.statusEdition = Status.COMPLETE
            if (status.first().text().contains(Status.SINGLE, true))
                element.statusEdition = Status.SINGLE
            else if (status.first().text().contains(Status.NOT_COMPLETE, true))
                element.statusEdition = Status.NOT_COMPLETE

            // Статус перевода
            element.statusTranslate = Translate.COMPLETE
            if (status[1].text().contains("продолжается", true)) {
                element.statusTranslate = Translate.NOT_COMPLETE
            }

            getFullElement(element)
        }.fold(onSuccess = { it },
            onFailure = { null })
    }

    override suspend fun getFullElement(element: SiteCatalogElement): SiteCatalogElement {

        val doc = connectManager.getDocument(element.link).select("div.leftContent")

        element.type = "Манга"
        doc.select(".flex-row .subject-meta .elem_category").forEach {
            if (categories.contains(it.select("a").text()))
                element.type = it.select("a").text()
        }

        // Список авторов
        element.authors = emptyList()
        val authorsTemp = doc.select(".flex-row .elementList .elem_author")
        element.authors = authorsTemp.map { it.select(".person-link").text() }

        // Количество глав
        val volume = doc.select(".chapters-link tr").size - 1
        element.volume = if (volume < 0) 0 else volume

        // Жанры
        element.genres =
            doc.select("span.elem_genre").map { it.select("a.element-link").text() }

        // Краткое описание
        element.about = doc.select("meta[itemprop=description]").attr("content")

        // Ссылка на лого
        element.logo = doc.select(".expandable .subject-cover img").attr("src")

        element.isFull = true

        return element
    }

    private fun simpleParseElement(elem: Element): SiteCatalogElement {
        val element = SiteCatalogElement()

        // Сохраняю в каждом елементе host и catalogName
        element.host = host
        element.catalogName = catalogName

        // название манги
        element.name = elem.select(".img a").select("img").attr("title")

        // ссылка в интернете
        element.shotLink = elem.select(".img a").attr("href")
        element.link = host + element.shotLink

        // Статус выпуска
        element.statusEdition = "Выпуск продолжается"
        if (elem.select("span.mangaCompleted").text().isNotEmpty())
            element.statusEdition = "Выпуск завершен"
        else if (elem.select("span.mangaSingle").text().isNotEmpty() and (element.volume > 0))
            element.statusEdition = "Сингл"

        // Статус перевода
        element.statusTranslate = "Перевод продолжается"
        if (elem.select("span.mangaTranslationCompleted").text().isNotEmpty()) {
            element.statusTranslate = "Перевод завершен"
            element.statusEdition = "Выпуск завершен"
        }

        // Ссылка на лого
        element.logo = elem.select(".img a").select("img").attr("data-original")

        // Порядок в базе данных
        element.dateId = elem.select("span.bookmark-menu").attr("data-id").toInt()

        kotlin.runCatching {
            element.populate =
                (elem.select(".desc .star-rate .rating")
                    .attr("title")
                    .split(" ")
                    .first()
                    .toFloat() * 10_000)
                    .toInt()
        }.onFailure {
            element.populate = 0
        }

        // Тип манги(Манга, Манхва или еще что
        element.type = "Манга"

        // Жанры
        element.genres = elem.select(".desc .tile-info a.element-link").map { it.text() }

        return element
    }

    override fun getCatalog() = flow {
        var docLocal: Document = connectManager.getDocument(siteCatalog)

        suspend fun isGetNext(): Boolean {
            val next = docLocal.select(".pagination > a.nextLink").attr("href")
            if (next.isNotEmpty())
                docLocal = connectManager.getDocument(host + next)
            return next.isNotEmpty()
        }

        do {
            docLocal.select("div.tile").forEach { element ->
                emit(simpleParseElement(element))
            }
        } while (isGetNext())
    }

    override suspend fun chapters(manga: Manga) =
        connectManager.getDocument(host + manga.shortLink)
            .select("div.leftContent .chapters-link")
            .select("tr")
            .filter { it.select("a").text().isNotEmpty() }
            .map {
                var name = it.select("a").text()
                val pat = Pattern.compile("v.+").matcher(name)
                if (pat.find())
                    name = pat.group()
                Chapter(
                    manga = manga.name,
                    name = name,
                    date = it.select("td").last().text(),
                    link = host + it.select("a").attr("href"),
                    path = "${manga.path}/$name"
                )
            }

    @OptIn(ExperimentalTime::class)
    override suspend fun pages(item: DownloadItem): List<String> {
        val list = mutableListOf<String>()
        // Создаю папку/папки по указанному пути
        (getFullPath(item.path)).createDirs()

        val shortLink = getShortLink(item.link)

        delay(Duration.seconds(1))
        val doc = connectManager.getDocument("$host$shortLink?mtr=1")
        // с помощью регулярных выражений ищу нужные данные
        val pat = Pattern.compile("rm_h.init.+").matcher(doc.body().html())
        // если данные найдены то продолжаю
        if (pat.find()) {
            // избавляюсь от ненужного и разделяю строку в список и отправляю
            val data = "[" + pat.group()
                .removeSuffix(");")
                .removePrefix("rm_h.initReader( ") + "]"

//            log("data = $data")

            val json = JSONArray(data).getJSONArray(1)

            repeat(json.length()) { index ->
                val jsonArray = json.getJSONArray(index)
                val url =
//                    jsonArray.getString(1) +
                    jsonArray.getString(0) +
                            jsonArray.getString(2)
                list += url
            }
        }
        return list
    }
}
