package com.san.kir.manger.components.Parsing

import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.SiteCatalogElement
import kotlinx.coroutines.experimental.channels.produce
import kotlin.coroutines.experimental.CoroutineContext

class EmptySiteCatalog : SiteCatalog {
    override var isInit = false
    override val ID = -1
    override val name = ""
    override val host = ""
    override val catalogName = ""
    override val siteCatalog = ""
    override var volume = 0
    override var oldVolume = 0

    override fun init() = this

     override fun getFullElement(element: SiteCatalogElement) = SiteCatalogElement()

    override fun getCatalog(context: CoroutineContext) = produce { send(SiteCatalogElement()) }

    override fun chapters(manga: Manga): List<Chapter> = emptyList()

    override fun pages(item: DownloadItem) = listOf("")
}
