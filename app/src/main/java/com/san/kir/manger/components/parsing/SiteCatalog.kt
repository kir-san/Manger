package com.san.kir.manger.components.parsing

import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.SiteCatalogElement
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.channels.ReceiveChannel

interface SiteCatalog {
    var isInit: Boolean
    val id: Int
    val name: String
    val host: String
    val catalogName: String
    val siteCatalog: String
    var volume: Int
    var oldVolume: Int
    fun init(): SiteCatalog

    fun getFullElement(element: SiteCatalogElement): SiteCatalogElement
    fun getCatalog(context: ExecutorCoroutineDispatcher): ReceiveChannel<SiteCatalogElement>
    fun chapters(manga: Manga): List<Chapter>
    fun pages(item: DownloadItem): List<String>
}
