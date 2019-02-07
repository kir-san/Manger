package com.san.kir.manger.components.parsing

import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.SiteCatalogElement
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.channels.ReceiveChannel

abstract class SiteCatalog {
    open var isInit: Boolean = false
    open var id: Int = 0

    abstract val name: String
    abstract val catalogName: String

    open val host: String
        get() = "http://$catalogName"
    open val allCatalogName: List<String>
        get() = listOf(catalogName)

    abstract val siteCatalog: String

    abstract var volume: Int
    abstract var oldVolume: Int

    abstract suspend fun init(): SiteCatalog

    open suspend fun getFullElement(element: SiteCatalogElement): SiteCatalogElement = element
    abstract fun getCatalog(context: ExecutorCoroutineDispatcher): ReceiveChannel<SiteCatalogElement>
    abstract suspend fun chapters(manga: Manga): List<Chapter>
    abstract suspend fun pages(item: DownloadItem): List<String>
    abstract suspend fun getElementOnline(url: String): SiteCatalogElement?
}
