package com.san.kir.manger.components.parsing.sites

import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.SiteCatalogClassic
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
import org.jsoup.nodes.Element
import java.util.regex.Pattern

class Unicomics(siteRepository: SiteRepository) : SiteCatalogClassic() {
    override val name: String = "UniComics"
    override val catalogName: String = "unicomics.ru"
    override val siteCatalog: String = "$host/map"
    override var volume = siteRepository.getItem(name)?.volume ?: 0
    override var oldVolume = volume

    override suspend fun init(): Unicomics {
        if (!isInit) {
            val doc = ManageSites.getDocument(siteCatalog)
            volume = doc.select(".content .block table tr a").size
            isInit = true
        }
        return this
    }

    override suspend fun getElementOnline(url: String): SiteCatalogElement? = runCatching {
        val element = SiteCatalogElement()

        element.host = host
        element.catalogName = catalogName
        element.siteId = id

        element.shotLink = url.split(catalogName).last()
        element.link = url

        element.type = "Комикс"

        getFullElement(element)
    }.fold(onSuccess = { it },
           onFailure = { null })

    override suspend fun getFullElement(element: SiteCatalogElement): SiteCatalogElement {
        val doc = ManageSites.getDocument(element.link)

        val info = doc.select(".content .left_container .info")
        element.name = info.select("h1").text()

        element.statusEdition = Status.UNKNOWN
        element.statusTranslate = Translate.UNKNOWN

        // Ссылка на лого
        element.logo = doc.select(".content .left_container .image img").attr("src")

        info.select("table > tbody > tr").forEach { tr ->
            if (tr.text().contains("Издательство", true)) {
                element.authors = tr.select("td > a").map { it.text() }
            } else if (tr.text().contains("Жанр", true)) {
                element.genres = tr.select("td > a").map { it.text() }.drop(1).toMutableList()
            }
        }

        runCatching { info.select("p").last().text() }.onSuccess { element.about = it }

        element.volume = getChapters(element.link).size

        element.isFull = true

        return element
    }

    private fun simpleParseElement(elem: Element): SiteCatalogElement {
        val element = SiteCatalogElement()

        element.host = host
        element.catalogName = catalogName
        element.siteId = id

        element.name = elem.text()

        element.shotLink = elem.attr("href")
        element.link = host + element.shotLink

        element.type = "Комикс"

        return element
    }

    override fun getCatalog(context: ExecutorCoroutineDispatcher) = GlobalScope.produce(context) {
        val doc = ManageSites.getDocument(siteCatalog).select(".content .block table tr a")

        doc.forEach { link ->
            send(simpleParseElement(link))
        }

        close()
    }

    override suspend fun chapters(manga: Manga): List<Chapter> {
        return getChapters(manga.site)
            .map {
                val link = it.select("table > tbody .online > a").attr("href")
                val name = it.select("a.list_title").text()
                Chapter(
                    manga = manga.unic,
                    name = name,
                    site = if (link.contains(host)) link else host + link,
                    path = "${manga.path}/${name}"
                )
            }
    }

    private fun getChapters(url: String): List<Element> {
        var links = listOf<Element>()

        var doc = ManageSites.getDocument(url)

        var counter = 1
        var oldSize = 0

        while (true) {
            counter++

            links = links + doc.select(".content .left_container .list_comics .right_comics")
            if (oldSize == links.size) break

            oldSize = links.size

            doc = ManageSites.getDocument("$url/page/$counter")
        }

        return links
    }

    override suspend fun pages(item: DownloadItem): List<String> {
        var list = listOf<String>()
        createDirs(getFullPath(item.path))
        var doc = ManageSites.getDocument(item.link)

        val matcher = Pattern.compile("\"paginator1\", (\\d+)")
            .matcher(doc.body().html())

        var size = 0
        if (matcher.find()) {
            size = matcher.group().split(", ").last().toInt()
        }

        var counter = 1

        while (true) {
            counter++

            list = list + doc.select(".content #b_image").attr("src")

            if (counter > size) break

            doc = ManageSites.getDocument("${item.link}/$counter")
        }

        return list
    }

}
