package com.san.kir.manger.components.Parsing

import com.san.kir.manger.dbflow.models.Chapter
import com.san.kir.manger.dbflow.models.DownloadItem
import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.dbflow.models.SiteCatalogElement
import kotlinx.coroutines.experimental.channels.ProducerJob
import rx.Observable
import kotlin.coroutines.experimental.CoroutineContext

interface SiteCatalog {
    var isInit: Boolean
    val ID: Int
    val name: String
    val host: String
    val catalogName: String
    val siteCatalog: String
    var volume: Int
    var oldVolume: Int
    fun init(): SiteCatalog
    fun reInit(): Unit

    suspend fun getFullElement(element: SiteCatalogElement): SiteCatalogElement
    fun getCatalog(context: CoroutineContext): ProducerJob<SiteCatalogElement>

    fun asyncGetChapters(context: CoroutineContext,
                         element: SiteCatalogElement,
                         path: String): ProducerJob<Chapter>

    fun getChapters(element: Manga): Observable<Chapter>

    fun getPages(observable: Observable<DownloadItem>): Observable<List<String>>
}
