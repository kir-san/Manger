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
import kotlinx.coroutines.experimental.run
import kotlinx.coroutines.experimental.sync.Mutex
import org.json.JSONArray
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable
import rx.schedulers.Schedulers
import java.util.regex.Pattern
import kotlin.coroutines.experimental.CoroutineContext

class Allhentai : SiteCatalog {
    override var isInit = false
    override val ID = 5
    override val name = "All Hentai"
    override val catalogName = "allhentai.ru"
    override val host = "http://$catalogName"
    override val siteCatalog = host + "/list?type=&sortType=RATING"
    override var volume = SiteWrapper.get(name)?.count ?: 0
    override var oldVolume = volume

    override fun init(): Allhentai {
        if (!isInit) {
            val doc = ManageSites.getDocument(host)
            doc.select(".rightContent h5").forEach {
                if (it.text() == "У нас сейчас") {
                    volume = it.parent().select("li").first().text().split(" ").component4().toInt()
                }

            }
            isInit = true
        }
        return this
    }

    override fun reInit() {
        oldVolume = SiteWrapper.get(name)?.count ?: 0
    }


    ////
    suspend override fun getFullElement(element: SiteCatalogElement): SiteCatalogElement {
        val rootdoc = ManageSites.asyncGetDocument(element.link)
        val doc = rootdoc.select("div.leftContent")

        // Список авторов
        val mangakas = doc.select(".mangaSettings .elementList a[href*=author]")
        mangakas.forEach { element.authors.add(it.text()) }

        // Количество глав
        val volume = doc.select(".cTable tr").filter { !it.select("a").attr("href").contains("forum") }.size - 1
        element.volume = if (volume < 0) 0 else volume

        // Краткое описание
        element.about = rootdoc.select("meta[name=description]").attr("content")

        element.isFull = true

        return element
    }
    ////

    private var count = 1_000_000
    private val mutex = Mutex()

    ////
    suspend fun simpleParseElement(elem: Element): SiteCatalogElement {
        val element = SiteCatalogElement()

        // Сохраняю в каждом елементе host и catalogName
        element.host = host
        element.catalogName = catalogName
        element.id = ID

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
            element.statusEdition = "Выпуск завершен"

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
            val doc = ManageSites.asyncGetDocument(element.link).select("div.leftContent")
            val matcher3 = Pattern.compile("\\d+")
                    .matcher(doc.select(".mangaSettings div[id*=user_rate]").first().id())
            if (matcher3.find()) {
                element.dateId = matcher3.group().toInt()
            }
        } finally {
            log = "watches " + element.dateId
        }


        mutex.lock()
        try {
            element.populate = count--
        } finally {
            mutex.unlock()
        }


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
            docLocal.select("div.pageBlock .cTable td[style]").forEach { element ->
                run(context) {
                    send(simpleParseElement(element))
                }
            }
        } while (isGetNext().await())

        close()
    }
    ///


    override fun asyncGetChapters(context: CoroutineContext,
                                  element: SiteCatalogElement,
                                  path: String) = produce(context) {
        val doc = ManageSites.asyncGetDocument(element.link)
        doc.select(".cTable").select("tr").forEach {
            val select = it.select("a")

            if (!select.attr("href").contains("forum"))
                if (select.text().isNotEmpty()) {
                    val name = select.text()
                    var link = select.attr("href")
                    if (!link.contains(host))
                        link = host + link
                    send(Chapter(manga = element.name,
                                 name = name,
                                 date = it.select("td[align]").text(),
                                 site = link,
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
                .map { it!!.select(".cTable") }
                .flatMap { Observable.from(it!!.select("tr")) }
                .filter { it.select("a").text() != "" }
                .map {
                    val name = it.select("a").text()
                    var link = it.select("a").attr("href")
                    if (!link.contains(host))
                        link = host + link
                    Chapter(manga = element.unic,
                            name = name,
                            date = it.select("td[align]").text(),
                            site = link,
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
                .map {
                    ManageSites.getDocument(it.link)
                }
                .observeOn(Schedulers.computation())
                // с помощью регулярных выражений ищу нужные данные
                .map { Pattern.compile("var pictures.+").matcher(it.body().html()) }
                // если данные найдены то продолжаю
                .filter { it.find() }
                // избавляюсь от ненужного
                .map {
                    it.group()
                            .removeSuffix(";")
                            .removePrefix("var pictures = ")
                }
                .map {
                    val json = JSONArray(it)

                    val list = mutableListOf<String>()

                    repeat(json.length()) { index ->
                        list.add(json.getJSONObject(index).getString("url"))
                    }

                    list
                }
    }
}
