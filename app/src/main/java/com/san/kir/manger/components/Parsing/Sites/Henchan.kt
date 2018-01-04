package com.san.kir.manger.components.Parsing.Sites

import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.SiteCatalogElement
import org.jsoup.nodes.Element
import java.util.regex.Pattern

class Henchan : MangachanTemplate() {
    override val ID: Int = 4
    override val name: String = "Хентай - тян!"
    override val catalogName: String = "hentai-chan.me"
    override var volume = Main.db.siteDao.loadSite(name)?.volume ?: 0
    override var oldVolume = volume

    override fun init() = super.init() as Henchan

    override fun simpleParseElement(elem: Element): SiteCatalogElement {
        val element = SiteCatalogElement()

        element.host = host
        element.catalogName = catalogName
        element.siteId = ID

        element.name = elem.select("a.title_link").first().text()

        element.shotLink = elem.select("a.title_link").first().attr("href")
        element.link = host + element.shotLink

        element.type = "Манга"

        val mangakas = elem.select("a[href*=mangaka]")
        element.authors = mangakas.filter { it != mangakas.last() }.map { it.text() }

        element.statusEdition = ""

        element.statusTranslate = ""

        element.volume = 1

        elem.select(".genre > a").forEach { element.genres.add(it.text()) }

        element.about = elem.select("div.tags").text()

        element.logo = host + elem.select("div.manga_images > a > img").attr("src")

        element.populate = elem.select("div.row4_left").text().split(" ").component2().toInt()

        val matcher3 = Pattern.compile("\\d+")
                .matcher(elem.select(".manga_row4 span").first().id())
        if (matcher3.find())
            element.dateId = matcher3.group().toInt()

        element.isFull = true

        return element
    }


    override fun chapters(manga: Manga): List<Chapter> {
        val doc = ManageSites.getDocument(manga.site)
        val date = doc.select("#info_wrap .row5 .row4_right b").text()
        return doc.select("#right")
                .select(".extra_off")
                .filter { it.select("a").text() == "Читать онлайн" }
                .map {
                    Chapter(manga = manga.unic,
                            name = manga.unic,
                            date = date,
                            site = host + it.select("a").attr("href"),
                            path = "${manga.path}/${manga.unic}")
                }
    }
}
