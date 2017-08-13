package com.san.kir.manger.components.Parsing.Sites

import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.components.Parsing.SiteCatalog
import com.san.kir.manger.dbflow.models.Chapter
import com.san.kir.manger.dbflow.models.DownloadItem
import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.dbflow.models.SiteCatalogElement
import com.san.kir.manger.dbflow.wrapers.SiteWrapper
import com.san.kir.manger.utils.createDirs
import com.san.kir.manger.utils.log
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.launch
import org.json.JSONArray
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable
import rx.schedulers.Schedulers
import java.util.regex.Pattern
import kotlin.coroutines.experimental.CoroutineContext

open class ReadmangaTemplate : SiteCatalog {
    override var isInit = false
    override val ID = 1
    override val name = "Read Manga"
    override val catalogName = "readmanga.me"
    override val host
        get() = "http://$catalogName"
    override val siteCatalog
        get() = "$host/list?sortType=created"
    override var volume = 0
    override var oldVolume = 0
    open val categories = listOf("Ёнкома",
                                 "Комикс западный",
                                 "Манхва",
                                 "Маньхуа",
                                 "В цвете",
                                 "Веб",
                                 "Сборник")

    override fun init(): ReadmangaTemplate {
        if (!isInit) {
            oldVolume = SiteWrapper.get(name)?.count ?: 0
            val doc = ManageSites.getDocument(host)
            doc.select(".rightContent h5").forEach {
                if (it.text() == "У нас сейчас")
                    volume = it.parent().select("li b").first().text().toInt()
            }
            isInit = true
        }
        return this
    }

    override fun reInit() {
        oldVolume = SiteWrapper.get(name)?.count ?: 0
    }


    ///
    override suspend fun getFullElement(element: SiteCatalogElement): SiteCatalogElement {

        val doc = ManageSites.asyncGetDocument(element.link).select("div.leftContent")

        // Тип манги(Манга, Манхва или еще что
        element.type = "Манга"
        doc.select(".flex-row .subject-meta .elem_tag").forEach {
            if (categories.contains(it.select("a").text()))
                element.type = it.select("a").text()
        }

        // Список авторов
        element.authors.clear()
        val mangakas = doc.select(".flex-row .elementList .elem_author")
        mangakas.forEach { element.authors.add(it.select(".person-link").text()) }

        // Количество глав
        val volume = doc.select(".chapters-link tr").size - 1
        element.volume = if (volume < 0) 0 else volume

        // Список жанров
        element.genres.clear()
        doc.select("span.elem_genre").forEach { element.genres.add(it.select("a.element-link").text()) }

        // Краткое описание
        element.about = doc.select("meta[itemprop=description]").attr("content")

        element.isFull = true

        return element
    }
    ///


    ///
    suspend fun simpleParseElement(elem: Element): SiteCatalogElement {
        val element = SiteCatalogElement()

        // Сохраняю в каждом елементе host и catalogName
        element.host = host
        element.catalogName = catalogName
        element.id = ID

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
            element.statusEdition = "Выпуск завершен"

        // Статус перевода
        element.statusTranslate = "Перевод продолжается"
        if (elem.select("span.mangaTranslationCompleted").text().isNotEmpty()) {
            element.statusTranslate = "Перевод завершен"
            element.statusEdition = "Выпуск завершен"
        }

        // Ссылка на лого
        element.logo = elem.select(".img a").select("img").attr("src")

        // порядок элемента на сайте, необходим для сортировки по дате
        element.dateId = elem.select(".chapters span.bookmark-menu").attr("data-id").toInt()

        // популярность
        try {
            element.populate = elem.select(".desc .tile-info p.small").first().ownText().toInt()
            log = "watches " + element.populate
        } catch (ex: Throwable) {
            element.populate = 0
        }

        // Тип манги(Манга, Манхва или еще что
        element.type = "Манга"

        // Список авторов
        elem.select(".desc .tile-info .person-link").forEach { element.authors.add(it.select(".person-link").text()) }

        // Список жанров
        elem.select(".desc .tile-info a.element-link").forEach { element.genres.add(it.text()) }

        return element
    }

    override fun getCatalog(context: CoroutineContext) = produce(context) {
        var docLocal: Document = ManageSites.asyncGetDocument(siteCatalog)

        fun isGetNext() = async(context) {
            val next = docLocal.select(".pagination > a.nextLink").attr("href")
            if (next.isNotEmpty()) {
                docLocal = ManageSites.asyncGetDocument(host + next)
                true
            } else
                false
        }

        do {
            docLocal.select("div.tile").forEach { element ->
                launch(context) {
                    send(simpleParseElement(element))
                }
            }
        } while (isGetNext().await())
        close()
    }
    ///


    override fun asyncGetChapters(context: CoroutineContext,
                                  element: SiteCatalogElement,
                                  path: String)
            = produce(context) {
        val doc = ManageSites.asyncGetDocument(element.link)
        doc.select("div.leftContent .chapters-link").select("tr").forEach {
            if (it.select("a").text().isNotEmpty()) {
                var name = it.select("a").text()
                val pat = Pattern.compile("v.+").matcher(name)
                if (pat.find())
                    name = pat.group()
                send(Chapter(manga = element.name,
                             name = name,
                             date = it.select("td").last().text(),
                             site = host + it.select("a").attr("href"),
                             path = "$path/$name"))
            }
        }
    }

    override fun getChapters(element: Manga): Observable<Chapter> {
        return Observable.create<Document> { go ->
            go.onNext(ManageSites.getDocument(element.site))
            go.onCompleted()
        }
                .observeOn(Schedulers.computation())
                .map { it!!.select("div.leftContent .chapters-link") }
                .flatMap { Observable.from(it!!.select("tr")) }
                .filter { it.select("a").text() != "" }
                .map {
                    var name = it.select("a").text()
                    val pat = Pattern.compile("v.+").matcher(name)
                    if (pat.find())
                        name = pat.group()
                    Chapter(manga = element.unic,
                            name = name,
                            date = it.select("td").last().text(),
                            site = host + it.select("a").attr("href"),
                            path = "${element.path}/$name"
                    )
                }
                .subscribeOn(Schedulers.io())
    }


    override fun getPages(observable: Observable<DownloadItem>): Observable<List<String>> {
        return observable
                .observeOn(Schedulers.io())
                .filter { createDirs(it.path) } // Создаю папку/папки по указанному пути
                .map {
                    ManageSites.getDocument(it.link + "?mature=1")
                } // С помощью okhttp получаю содержимое страницы и отдаю его на парсинг в jsoup
                .observeOn(Schedulers.computation())
                .map { Pattern.compile("rm_h.init.+").matcher(it.body().html()) } // с помощью регулярных выражений ищу нужные данные
                .filter { it.find() } // если данные найдены то продолжаю
                .map { it.group().removeSuffix(", 0, false);").removePrefix("rm_h.init( ") }// избавляюсь от ненужного и разделяю строку
                .map {
                    val json = JSONArray(it)

                    val list = mutableListOf<String>()

                    kotlin.repeat(json.length()) { index ->
                        val jsonArray = json.getJSONArray(index)
                        val url = jsonArray.getString(1) + jsonArray.getString(0) + jsonArray.getString(
                                2)
                        list.add(url)
                    }
                    list
                }
    }
}
