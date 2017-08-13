package com.san.kir.manger.components.Parsing.Sites

import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.components.Parsing.SiteCatalog
import com.san.kir.manger.dbflow.models.Chapter
import com.san.kir.manger.dbflow.models.DownloadItem
import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.dbflow.models.SiteCatalogElement
import com.san.kir.manger.dbflow.wrapers.SiteWrapper
import com.san.kir.manger.utils.createDirs
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.launch
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable
import rx.schedulers.Schedulers
import java.util.regex.Pattern
import kotlin.coroutines.experimental.CoroutineContext

open class MangachanTemplate : SiteCatalog {


    override var isInit = false
    override val ID: Int = 0
    override val name: String = "Манга - тян"
    override val catalogName: String = "mangachan.ru"
    override val host: String
        get() = "http://" + catalogName
    override val siteCatalog: String
        get() = host + "/manga/new"
    override var volume: Int = 0
    override var oldVolume: Int = 0

    override fun init(): MangachanTemplate {
        if (!isInit) {
            oldVolume = SiteWrapper.get(name)?.count ?: 0
            val doc = ManageSites.getDocument(siteCatalog)
            volume = doc.select("#pagination > b").text().split(" ").first().toInt()
            isInit = true
        }
        return this
    }

    override fun reInit() {
        oldVolume = SiteWrapper.get(name)?.count ?: 0
    }


    suspend override fun getFullElement(element: SiteCatalogElement): SiteCatalogElement {
        return element
    }

    open suspend fun simpleParseElement(elem: Element): SiteCatalogElement {
        val element = SiteCatalogElement()

        // Сохраняю в каждом елементе host и catalogName
        element.host = host
        element.catalogName = catalogName
        element.id = ID

        // название манги
        element.name = elem.select("a.title_link").first().text()

        // ссылка в интернете
        element.shotLink = elem.select("a.title_link").first().attr("href")
        element.link = host + element.shotLink

        // Тип манги(Манга, Манхва или еще что
        element.type = elem.select("a[href*=type]").text()

        // Список авторов
        val mangakas = elem.select("a[href*=mangaka]")
        mangakas.forEach { if (it != mangakas.last()) element.authors.add(it.text()) }

        // Статус выпуска
        val matcher = Pattern.compile("[А-Яа-я]+ [А-Яа-я]+")
                .matcher(elem.select(".manga_row3 .item2").html())
        if (matcher.find())
            element.statusEdition =
                    if (matcher.group().contains("перевод", true)) "cингл"
                    else matcher.group()

        // Статус перевода
        element.statusTranslate = elem.select(".manga_row3 span").text()
                .removeSurrounding(" ").split(",").last()

        // Количество глав
        val matcher2 = Pattern.compile("\\d+")
                .matcher(elem.select(".manga_row3 b").text())
        if (matcher2.find())
            element.volume = matcher2.group().toInt()

        // Список жанров
        elem.select(".genre > a").forEach { element.genres.add(it.text()) }

        // Краткое описание
        element.about = elem.select("div.tags").text()

        // Ссылка на лого
        element.logo = elem.select("div.manga_images > a > img").attr("src")

        // Популярность
        element.populate = elem.select("div.manga_images font b").text().toInt()

        // Порядок в базе данных
        val matcher3 = Pattern.compile("\\d+")
                .matcher(elem.select(".manga_row4 .row4_left .user_link_short").first().id())
        if (matcher3.find())
            element.dateId = matcher3.group().toInt()

        element.isFull = true

        return element
    }

    override fun getCatalog(context: CoroutineContext) = produce(context) {
        var docLocal: Document = ManageSites.asyncGetDocument(siteCatalog)

        fun isGetNext() = async(context) {
            val next = docLocal.select("#pagination > a:contains(Вперед)").attr("href")
            if (next.isNotEmpty()) {
                docLocal = ManageSites.asyncGetDocument(siteCatalog + next)
                true
            } else
                false

        }

        do {
            docLocal.select("div.content_row").forEach { element ->
                launch(context) {
                    send(simpleParseElement(element))
                }
            }
        } while (isGetNext().await())
        close()
    }


    override fun asyncGetChapters(context: CoroutineContext,
                                  element: SiteCatalogElement,
                                  path: String)
            = produce(context) {
        val doc = ManageSites.asyncGetDocument(element.link)
        doc.select(".table_cha").select("tr").forEach {
            if (it.select("a").text().isNotEmpty()) {
                var name = it.select("a").text()
                val pat = Pattern.compile("v.+").matcher(name)
                if (pat.find())
                    name = pat.group()
                send(Chapter(manga = element.name,
                             name = name,
                             date = it.select(".date").text(),
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
                .map { it!!.select(".table_cha") }
                .flatMap { Observable.from(it!!.select("tr")) }
                .filter { it.select("a").text() != "" }
                .map {
                    var name = it.select("a").text()
                    val pat = Pattern.compile("v.+").matcher(name)
                    if (pat.find())
                        name = pat.group()
                    Chapter(manga = element.unic,
                            name = name,
                            date = it.select(".date").text(),
                            site = host + it.select("a").attr("href"),
                            path = "${element.path}/$name"
                    )
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getPages(observable: Observable<DownloadItem>): Observable<List<String>> {
        return observable
                .observeOn(Schedulers.io())
                // Создаю папку/папки по указанному пути
                .filter { createDirs(it.path) }
                // С помощью okhttp получаю содержимое страницы и отдаю его на парсинг в jsoup
                .map { ManageSites.getDocument(it.link) }
                .observeOn(Schedulers.computation())
                .map { it.select("#content").html() }
                // с помощью регулярных выражений ищу нужные данные
                .map { Pattern.compile("\"fullimg\":\\[.+").matcher(it) }
                // если данные найдены то продолжаю
                .filter { it.find() }
                // избавляюсь от ненужного и разделяю строку
                .map {
                    it.group()
                            .removeSuffix(",]")
                            .removePrefix("\"fullimg\":[")
                            .split(",")
                }
    }
}
