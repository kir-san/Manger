package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.SiteCatalog
import com.san.kir.manger.components.parsing.Status
import com.san.kir.manger.components.parsing.Translate
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.utils.createDirs
import com.san.kir.manger.utils.getFullPath
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.regex.Pattern

class Allhentai(siteRepository: SiteRepository) : SiteCatalog() {
    override val name = "All Hentai"
    override val catalogName = "allhentai.ru"
    override val siteCatalog = "$host/list?type=&sortType=RATING"
    override var volume = siteRepository.getItem(name)?.volume ?: 0
    override var oldVolume = volume

    override suspend fun init(): Allhentai {
        if (!isInit) {
            val doc = ManageSites.getDocument(host)
            doc.select(".rightContent h5")
                .filter { it.text() == "У нас сейчас" }
                .map { it.parent().select("li") }
                .map { it.first().text() }
                .map { it.split(" ").component4().toInt() }
                .forEach { volume = it }
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

        val status = doc.select("#mangaBox .leftContent .mangaSettings p")

        // Статус выпуска
        element.statusEdition = Status.COMPLETE
        if (status.text().contains(Status.SINGLE, true))
            element.statusEdition = "Выпуск завершен"
        else if (status.text().contains(Status.NOT_COMPLETE, true))
            element.statusEdition = Status.NOT_COMPLETE

        // Статус перевода
        element.statusTranslate = Translate.COMPLETE
        if (status.text().contains("продолжается", true)) {
            element.statusTranslate = Translate.NOT_COMPLETE
        }

        // Ссылка на лого
        element.logo = doc.select("#mangaBox .leftContent .mangaDescPicture img").attr("src")

        // Жанры
        status.first { it.text().contains("Жанры") }.select("a").mapTo(element.genres) { it.text() }

        getFullElement(element)
    }.fold(onSuccess = { it },
           onFailure = { null })

    ////
    override suspend fun getFullElement(element: SiteCatalogElement): SiteCatalogElement {
        val rootDoc = ManageSites.getDocument(element.link)
        val doc = rootDoc.select("div.leftContent")

        // Список авторов
        element.authors =
            doc.select(".mangaSettings .elementList a[href*=author]").map { it.text() }

        // Количество глав
        val volume =
            doc.select(".cTable tr").filter { !it.select("a").attr("href").contains("forum") }.size - 1
        element.volume = if (volume < 0) 0 else volume

        // Краткое описание
        element.about = rootDoc.select("meta[name=description]").attr("content")

        element.isFull = true

        return element
    }

    ////
    private var count = 1_000_000
    private val mutex = Mutex()

    ////
    private suspend fun simpleParseElement(elem: Element): SiteCatalogElement {
        val element = SiteCatalogElement()

        // Сохраняю в каждом елементе host и catalogName
        element.host = host
        element.catalogName = catalogName
        element.siteId = id

        // название манги
        element.name = elem.select("a").first().ownText()

        // ссылка в интернете
        element.shotLink = elem.select("a").attr("href")
        element.link = host + element.shotLink

        // Тип манги(Манга, Манхва или еще что
        element.type = "Манга"

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
        element.logo = elem.select("a.screenshot").attr("rel")

        // Жанры
        elem.select("a").first().attr("title").split(", ").forEach {
            element.genres.add(it)
        }


        // Порядок в базе данных
        try {
            val matcher3 = Pattern.compile("\\d+")
                .matcher(elem.select(".screenshot").first().attr("rel"))
            val dateId = StringBuilder()
            while (matcher3.find()) {
                dateId.append(matcher3.group())
            }
            element.dateId = dateId.toString().toInt()
        } catch (ex: NullPointerException) {
            val doc = ManageSites.getDocument(element.link).select("div.leftContent")
            val matcher3 = Pattern.compile("\\d+")
                .matcher(doc.select(".mangaSettings div[id*=user_rate]").first().id())
            if (matcher3.find()) {
                element.dateId = matcher3.group().toInt()
            }
        }

        mutex.withLock {
            element.populate = count--
        }

        return element
    }

    override fun getCatalog(context: ExecutorCoroutineDispatcher) = GlobalScope.produce(context) {
        var docLocal: Document = ManageSites.getDocument(siteCatalog)

        fun isGetNext(): Boolean {
            val next = docLocal.select(".pagination > a.nextLink").attr("href")
            return if (next.isNotEmpty()) {
                docLocal = ManageSites.getDocument(host + next)
                true
            } else
                false
        }

        do {
            docLocal.select("div.pageBlock .cTable td[style]").forEach { element ->
                launch(context) {
                    send(simpleParseElement(element))
                }
            }
        } while (isGetNext())

        close()
    }

    ///
    override suspend fun chapters(manga: Manga) =
        ManageSites.getDocument(manga.site)
            .select(".cTable")
            .select("tr")
            .map { it.select("td[align]").text() to it.select("a") }
            .filterNot { (_, it) -> it.attr("href").contains("forum") }
            .filter { (_, it) -> it.text().isNotEmpty() }
            .map { (date, select) ->
                val link = select.attr("href")
                Chapter(
                    manga = manga.unic,
                    name = select.text(),
                    date = date,
                    site = if (link.contains(host)) link else host + link,
                    path = "${manga.path}/${select.text()}"
                )
            }


    override suspend fun pages(item: DownloadItem): List<String> {
        var list = listOf<String>()
        // Создаю папку/папки по указанному пути
        createDirs(getFullPath(item.path))
        val doc = ManageSites.getDocument(item.link)

        // с помощью регулярных выражений ищу нужные данные
        val pat = Pattern.compile("var pictures.+").matcher(doc.body().html())
        // если данные найдены то продолжаю
        if (pat.find()) {
            // избавляюсь от ненужного и разделяю строку в список и отправляю
            val data = pat.group()
                .removeSuffix(";")
                .removePrefix("var pictures = ")
            val json = JSONArray(data)

            repeat(json.length()) { index ->
                var string = json.getJSONObject(index).getString("url")
                if (string[7] == 'c')
                    string = string.replaceFirst("c", "a")
                list = list + string
            }
        }
        return list
    }
}
