package com.san.kir.manger.components.Parsing

import com.san.kir.manger.components.Parsing.Sites.Allhentai
import com.san.kir.manger.components.Parsing.Sites.Henchan
import com.san.kir.manger.components.Parsing.Sites.Mangachan
import com.san.kir.manger.components.Parsing.Sites.Mintmanga
import com.san.kir.manger.components.Parsing.Sites.Readmanga
import com.san.kir.manger.components.Parsing.Sites.Selfmanga
import com.san.kir.manger.dbflow.models.Chapter
import com.san.kir.manger.dbflow.models.DownloadItem
import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.dbflow.models.SiteCatalogElement
import com.san.kir.manger.dbflow.wrapers.MangaWrapper
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.SET_MEMORY
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import rx.Observable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext

object ManageSites {


    private val client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
//            .retryOnConnectionFailure(true)
            .build()

//    private val _client: OkHttpClient = OkHttpClient.Builder()
//            .cache(Cache(App.context.cacheDir, Int.MAX_VALUE.toLong()))
//            .addInterceptor { chain ->
//                val originalResponse = chain?.proceed(chain.request())
//                originalResponse?.newBuilder()?.header("Cache-Control",
//                                                       "max-age=${60 * 60 * 24 * 365}")?.build()
//            }
//            .build()

    val CATALOG_SITES = listOf(
            Mangachan(),
            Readmanga(),
            Mintmanga(),
            Selfmanga(),
            Henchan(),
            Allhentai()
    )

    fun openLink(url: String): Response {
        val request = Request.Builder().url(url).build()
        return client.newCall(request).execute()
    }

    fun getDocument(url: String): Document {
        // Закоментированна так специально, на будущее
//        val response = openLink(url)
//        val body = response.body()
//        val string = body.bytes().toString(Charset.forName("UTF-8"))
        return Jsoup.parse(openLink(url).body().bytes().toString(Charset.forName("UTF-8")))
    }

    suspend fun asyncOpenLink(url: String): Response {
        val request = Request.Builder().url(url).build()
        return client.newCall(request).execute()
    }

    suspend fun asyncGetDocument(url: String): Document {
        return async(CommonPool) {
            val response = asyncOpenLink(url)
            Jsoup.parse(response.body().bytes().toString(Charset.forName("UTF-8")))
        }.await()
    }


    fun asyncGetChapters(context: CoroutineContext,
                         element: SiteCatalogElement,
                         path: String) = produce(context) {

        val catalog = CATALOG_SITES[element.id].asyncGetChapters(context, element, path)

        try {
            for (item in catalog) {
                send(item)
            }
        } finally {
            catalog.cancel()
        }

    }

    fun getOnlineChapters(element: Manga): Observable<Chapter>? {
        CATALOG_SITES.forEach {
            if (element.host == it.host)
                return it.getChapters(element)
        }
        return null
    }

    // Загрузка каталога из локальной памяти
    fun loadCatalogFromLocal(context: CoroutineContext, id: Int) = produce(context) {
        val catalog = MangaWrapper.getAllManga()
        val name = CATALOG_SITES[id].catalogName
        val f: File = File("$SET_MEMORY/${DIR.CATALOGS}/$name")

        var fis: FileInputStream? = null
        var ois: ObjectInputStream? = null
        try {
            fis = FileInputStream(f)
            ois = ObjectInputStream(fis)

            while (true) {
                val item = ois.readObject() as SiteCatalogElement
                item.isAdded = MangaWrapper.contain(catalog, item.host, item.name)
                send(item)
            }
        } catch (ex: Exception) {
            ois?.close()
            fis?.close()
            close()
        }
    }

    // Скачивание и распарсивание в каталог из интернета
    fun loadCatalogFromInternet(context: CoroutineContext, id: Int) = produce(context) {
        // Получаем название каталога
        val name = CATALOG_SITES[id].catalogName
        // Получаем по полному пути файл
        val f = File("$SET_MEMORY/${DIR.CATALOGS}/$name")
        f.createNewFile()

        // Открываем потоки для записи в файл
        val fos = FileOutputStream(f)
        val oos = ObjectOutputStream(fos)

        val loadContext = newFixedThreadPoolContext(4, "LoadContext")

        val catalog = CATALOG_SITES[id].getCatalog(loadContext)

        try {
            for (item in catalog) {
                oos.writeObject(item)
                item.isAdded = MangaWrapper.asyncContain(item)
                send(item)
            }
        } finally {
            catalog.cancel()
            oos.flush()
            fos.close()
            oos.close()
            close()
        }
    }

    // Загрузка полной информации для элемента в каталоге
    suspend fun getFullElement(simpleElement: SiteCatalogElement): SiteCatalogElement {
        return CATALOG_SITES[simpleElement.id].getFullElement(simpleElement)
    }


    // Загрузка страниц для главы
    fun getPageList(item: DownloadItem,
                    observable: Observable<DownloadItem>): Observable<List<String>> {
        var site: SiteCatalog? = null
        CATALOG_SITES.forEach {
            if (item.link.contains(it.catalogName))
                site = it
        }
        return site?.getPages(observable)!!
    }
}
