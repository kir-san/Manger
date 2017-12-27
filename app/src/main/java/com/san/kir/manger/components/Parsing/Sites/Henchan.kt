package com.san.kir.manger.components.Parsing.Sites

import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.SiteCatalogElement
import kotlinx.coroutines.experimental.channels.produce
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable
import rx.schedulers.Schedulers
import java.util.regex.Pattern
import kotlin.coroutines.experimental.CoroutineContext

class Henchan : MangachanTemplate() {
    override val ID: Int = 4
    override val name: String = "Хентай - тян!"
    override val catalogName: String = "hentai-chan.me"
    override var volume = Main.db.siteDao.loadSite(name)?.volume ?: 0
    override var oldVolume = volume

    override fun init() = super.init() as Henchan

    override suspend fun simpleParseElement(elem: Element): SiteCatalogElement {
        val element = SiteCatalogElement()

        // Сохраняю в каждом елементе host и catalogName
        element.host = host
        element.catalogName = catalogName
        element.siteId = ID

        // название манги
        element.name = elem.select("a.title_link").first().text()

        // ссылка в интернете
        element.shotLink = elem.select("a.title_link").first().attr("href")
        element.link = host + element.shotLink

        // Тип манги(Манга, Манхва или еще что
        element.type = "Манга"

        // Список авторов
        val mangakas = elem.select("a[href*=mangaka]")
        element.authors = mangakas.filter { it != mangakas.last() }.map { it.text() }

        // Статус выпуска
        element.statusEdition = ""

        // Статус перевода
        element.statusTranslate = ""

        // Количество глав
        element.volume = 1

        // Список жанров
        elem.select(".genre > a").forEach { element.genres.add(it.text()) }

        // Краткое описание
        element.about = elem.select("div.tags").text()

        // Ссылка на лого
        element.logo = host + elem.select("div.manga_images > a > img").attr("src")

        // Популярность
        element.populate = elem.select("div.row4_left").text().split(" ").component2().toInt()

        // Порядок в базе данных
        val matcher3 = Pattern.compile("\\d+")
                .matcher(elem.select(".manga_row4 span").first().id())
        if (matcher3.find())
            element.dateId = matcher3.group().toInt()

        element.isFull = true

        return element
    }


    override fun asyncGetChapters(context: CoroutineContext, element: SiteCatalogElement, path: String)
            = produce(context) {
        val doc = ManageSites.asyncGetDocument(element.link)
        val date = doc.select("#info_wrap .row5 .row4_right b").text()
        doc.select("#right").select(".extra_off").forEach {
            if (it.select("a").text() == "Читать онлайн") {

                send(Chapter(manga = element.name,
                             name = element.name,
                             date = date,
                             site = host + it.select("a").attr("href"),
                             path = "$path/$element.name"))
            }
        }
    }


    override fun getChapters(element: Manga): Observable<Chapter> {
        var date = ""
        return Observable.create<Document> { go ->
            go.onNext(ManageSites.getDocument(element.site))
            go.onCompleted()
        }
                .observeOn(Schedulers.computation())
                .map {
                    date = it.select("#info_wrap .row5 .row4_right b").text()
                    it.select("#right")
                }
                .flatMap { Observable.from(it!!.select(".extra_off")) }
                .filter { it.select("a").text() == "Читать онлайн" }
                .map {
                    Chapter(manga = element.unic,
                            name = element.name,
                            date = date,
                            site = host + it.select("a").attr("href"),
                            path = "${element.path}/$name"
                    )
                }
                .subscribeOn(Schedulers.io())
    }
}
