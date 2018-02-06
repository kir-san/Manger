package com.san.kir.manger.components.Parsing.Sites

import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.components.Parsing.SiteCatalog
import com.san.kir.manger.components.Parsing.Status
import com.san.kir.manger.components.Parsing.Translate
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.utils.createDirs
import com.san.kir.manger.utils.getFullPath
import kotlinx.coroutines.experimental.channels.produce
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
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
            oldVolume = Main.db.siteDao.loadSite(name)?.volume ?: 0
            val doc = ManageSites.getDocument(siteCatalog)
            volume = doc.select("#pagination > b").text().split(" ").first().toInt()
            isInit = true
        }
        return this
    }

    override fun getFullElement(element: SiteCatalogElement) = element

    open fun simpleParseElement(elem: Element): SiteCatalogElement {
        val element = SiteCatalogElement()

        element.host = host
        element.catalogName = catalogName
        element.siteId = ID

        element.name = elem.select("a.title_link").first().text()

        element.shotLink = elem.select("a.title_link").first().attr("href")
        element.link = host + element.shotLink

        element.type = elem.select("a[href*=type]").text()

        val mangakas = elem.select("a[href*=mangaka]")
        element.authors = mangakas.filter { it != mangakas.last() }.map { it.text() }


        var s = elem.select(".manga_row3 .item2").html()
        element.statusEdition =
                when {
                    s.contains(Status.COMPLETE, true) -> Status.COMPLETE
                    s.contains(Status.NOT_COMPLETE, true) -> Status.NOT_COMPLETE
                    s.contains(Status.SINGLE, true) -> Status.SINGLE
                    else -> {
                        Status.UNKNOWN
                    }
                }

//        val text = elem.select(".manga_row3 span").html()
        s = elem.select(".manga_row3 .item2").toString()
        element.statusTranslate = when {
            s.contains(Translate.COMPLETE, true) -> Translate.COMPLETE
            s.contains(Translate.NOT_COMPLETE, true) -> Translate.NOT_COMPLETE
            s.contains(Translate.FREEZE, true) -> Translate.FREEZE
            else -> Translate.UNKNOWN
        }

        val matcher2 = Pattern.compile("\\d+")
            .matcher(elem.select(".manga_row3 b").text())
        if (matcher2.find())
            element.volume = matcher2.group().toInt()

        elem.select(".genre > a").forEach { element.genres.add(it.text()) }

        element.about = elem.select("div.tags").text()

        element.logo = elem.select("div.manga_images > a > img").attr("src")

        element.populate = elem.select("div.manga_images font b").text().toInt()

        val matcher3 = Pattern.compile("\\d+")
            .matcher(elem.select(".manga_row4 .row4_left .user_link_short").first().id())
        if (matcher3.find())
            element.dateId = matcher3.group().toInt()

        element.isFull = true

        return element
    }

    override fun getCatalog(context: CoroutineContext) = produce(context) {
        var docLocal: Document = ManageSites.getDocument(siteCatalog)

        fun isGetNext(): Boolean {
            val next = docLocal.select("#pagination > a:contains(Вперед)").attr("href")
            if (next.isNotEmpty())
                docLocal = ManageSites.getDocument(siteCatalog + next)

            return next.isNotEmpty()
        }

        do {
            docLocal.select("div.content_row").forEach { element ->
                send(simpleParseElement(element))
            }
        } while (isGetNext())
        close()
    }

    override fun chapters(manga: Manga) =
        ManageSites.getDocument(manga.site)
            .select(".table_cha")
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
                    date = it.select(".date").text(),
                    site = host + it.select("a").attr("href"),
                    path = "${manga.path}/$name"
                )
            }


    override fun pages(item: DownloadItem): List<String> {
        var list = listOf<String>()
        // Создаю папку/папки по указанному пути
        createDirs(getFullPath(item.path))
        val doc = ManageSites.getDocument(item.link)
        val content = doc.select("#content").html()
        // с помощью регулярных выражений ищу нужные данные
        val pat = Pattern.compile("\"fullimg\":\\[.+").matcher(content)
        // если данные найдены то продолжаю
        if (pat.find()) {
            // избавляюсь от ненужного и разделяю строку в список и отправляю
            list = pat.group().removeSuffix(",]")
                .removePrefix("\"fullimg\":[")
                .split(",")
        }
        return list
    }
}
