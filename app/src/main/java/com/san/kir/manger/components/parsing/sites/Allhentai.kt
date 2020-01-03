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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.regex.Pattern

class Allhentai(siteRepository: SiteRepository) : SiteCatalogClassic() {
    override val name = "All Hentai"
    override val catalogName = "allhentai.ru"
    override val siteCatalog = "$host/list?sortType=created"
    override var volume = siteRepository.getItem(name)?.volume ?: 0
    override var oldVolume = volume

    private val statusComplete = "Выпуск завершен"

    override suspend fun init(): Allhentai {
        if (!isInit) {
            val doc = ManageSites.getDocument("$host/list")
            volume =
                doc.select("#mangaBox .leftContent .pagination .step").last().text().toInt() * 70
            isInit = true
        }
        return this
    }

    override suspend fun getElementOnline(url: String): SiteCatalogElement? = runCatching {
        val element = SiteCatalogElement()

        val doc = ManageSites.getDocument(url)

        element.host = host
        element.catalogName = catalogName
        element.siteId = id

        element.name = doc.select("#mangaBox .leftContent meta[itemprop=name]").attr("content")

        element.shotLink = url.split(catalogName).last()
        element.link = url

        element.type = "Манга"

        val status = doc.select("#mangaBox .leftContent .expandable .subject-meta p")

        // Статус выпуска
        element.statusEdition = Status.COMPLETE
        if (status.text().contains(Status.SINGLE, true)) {
            element.statusEdition = Status.SINGLE
        } else if (status.text().contains(Status.NOT_COMPLETE, true))
            element.statusEdition = Status.NOT_COMPLETE

        // Статус перевода
        element.statusTranslate = Translate.COMPLETE
        if (status.text().contains("продолжается", true)) {
            element.statusTranslate = Translate.NOT_COMPLETE
        }

        getFullElement(element)
    }.fold(onSuccess = { it },
           onFailure = { null })

    ////
    override suspend fun getFullElement(element: SiteCatalogElement): SiteCatalogElement {
        val rootDoc = ManageSites.getDocument(element.link)
        val doc = rootDoc.select("div.leftContent")

        // Список авторов
        element.authors =
            doc.select(".expandable .elementList .elem_author .person-link").map { it.text() }

        // Количество глав
        val volume =
            doc.select(".chapters-link table tbody tr").size
        element.volume = if (volume < 0) 0 else volume

        // Краткое описание
        element.about = doc.select(".expandable .manga-description").text()

        // Обновляем лого на получше
        element.logo = doc.select(".expandable .subject-cower img").attr("data-full")

        element.isFull = true

        return element
    }

    ////
    private var count = 1
    private val mutex = Mutex()

    ////
    private suspend fun simpleParseElement(elem: Element): SiteCatalogElement {
        val element = SiteCatalogElement()

        // Сохраняю в каждом елементе host и catalogName
        element.host = host
        element.catalogName = catalogName
        element.siteId = id

        // название манги
        element.name = elem.select(".desc h3 a").first().ownText()

        // ссылка в интернете
        element.shotLink = elem.select(".desc h3 a").attr("href")
        element.link = host + element.shotLink

        // Тип манги(Манга, Манхва или еще что
        element.type = "Манга"

        // Статус выпуска
        element.statusEdition = "Выпуск продолжается"
        if (elem.select(".tags .mangaCompleted").text().isNotEmpty())
            element.statusEdition = statusComplete
        else if (elem.select(".tags .mangaSingle").text().isNotEmpty() and (element.volume > 0))
            element.statusEdition = "Сингл"

        // Статус перевода
        element.statusTranslate = "Перевод продолжается"
        if (elem.select(".tags .mangaTranslationCompleted").text().isNotEmpty()) {
            element.statusTranslate = "Перевод завершен"
            element.statusEdition = statusComplete
        }

        // Ссылка на лого
        element.logo = elem.select(".img .lazy").attr("data-original")

        // Жанры
        elem.select(".desc .tile-info .element-link").map { it.text() }.forEach {
            element.genres.add(it)
        }

        // Порядок в базе данных
        mutex.withLock {
            element.dateId = count++
        }

        kotlin.runCatching {
            element.populate =
                (elem.select(".desc .star-rate .rating").attr("title").split(" ").first().toFloat() * 10_000).toInt()
        }.onFailure {
            element.populate = 0
        }

        return element
    }

    override fun getCatalog() = flow {
        var docLocal: Document = ManageSites.getDocument(siteCatalog)

        fun isGetNext(): Boolean {
            val next = docLocal.select("#mangaBox .pagination a.nextLink").attr("href")
            return if (next.isNotEmpty()) {
                docLocal = ManageSites.getDocument(host + next)
                true
            } else
                false
        }

        do {
            docLocal.select("#mangaBox .leftContent .tiles .tile").forEach { element ->
                emit(simpleParseElement(element))
            }
        } while (isGetNext())
    }

    ///
    override suspend fun chapters(manga: Manga) =
        ManageSites.getDocument(host + manga.shortLink)
            .select(".chapters-link table tbody tr")
            .map {
                val select = it.select("a")
                val link = select.attr("href")
                Chapter(
                    manga = manga.unic,
                    name = select.text(),
                    date = it.select("td.hidden-xxs").text(),
                    site = if (link.contains(host)) link else host + link,
                    path = "${manga.path}/${select.text()}"
                )
            }


    override suspend fun pages(item: DownloadItem): List<String> {
        val list = mutableListOf<String>()
        // Создаю папку/папки по указанному пути
        getFullPath(item.path).createDirs()

        val shortLink = getShortLink(item.link)

        val doc = ManageSites.getDocument(host + shortLink)

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
