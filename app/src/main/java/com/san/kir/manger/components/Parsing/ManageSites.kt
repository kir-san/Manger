package com.san.kir.manger.components.Parsing

import com.san.kir.manger.BuildConfig
import com.san.kir.manger.Extending.BaseActivity
import com.san.kir.manger.R
import com.san.kir.manger.components.Parsing.Sites.Allhentai
import com.san.kir.manger.components.Parsing.Sites.Henchan
import com.san.kir.manger.components.Parsing.Sites.Mangachan
import com.san.kir.manger.components.Parsing.Sites.Mintmanga
import com.san.kir.manger.components.Parsing.Sites.Readmanga
import com.san.kir.manger.components.Parsing.Sites.Selfmanga
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.SiteCatalogElement
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse
import org.jetbrains.anko.longToast
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import rx.Observable
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.coroutines.experimental.CoroutineContext

object ManageSites {


    private val client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
//            .retryOnConnectionFailure(true)
            .build()

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

    fun getDocument(url: String): Document =// Закоментированна так специально, на будущее
//        val response = openLink(url)
//        val body = response.body()
//        val string = body.bytes().toString(Charset.forName("UTF-8"))
            Jsoup.parse(openLink(url).body()?.bytes()?.toString(Charset.forName("UTF-8")))

    suspend fun asyncOpenLink(url: String): Response {
        val request = Request.Builder().url(url).build()
        return client.newCall(request).execute()
    }

    suspend fun asyncGetDocument(url: String): Document {
        val response = asyncOpenLink(url)
        return Jsoup.parse(response.body()?.bytes()?.toString(Charset.forName("UTF-8")))
    }


    fun asyncGetChapters(context: CoroutineContext,
                         element: SiteCatalogElement,
                         path: String) =
            CATALOG_SITES[element.siteId].asyncGetChapters(context, element, path)


    fun getOnlineChapters(element: Manga): Observable<Chapter>? {
        CATALOG_SITES.forEach {
            if (element.host == it.host)
                return it.getChapters(element)
        }
        return null
    }

    // Загрузка полной информации для элемента в каталоге
    fun getFullElement(simpleElement: SiteCatalogElement) = async {
        CATALOG_SITES[simpleElement.siteId].getFullElement(simpleElement)
    }


    // Получение страниц для главы
    fun getPages(item: DownloadItem) = CATALOG_SITES
            .first { item.link.contains(it.catalogName) }
            .asyncGetPages(item)

    private val url = "http://4pda.ru/forum/index.php?showtopic=772886&st=0#entry53336845"

    class UpdateApp(private val act: BaseActivity) {
        // функция проверки новой версии приложения на сайте 4pda.ru
        fun checkNewVersion(user: Boolean = false) = launch(CommonPool) {
            try {
                val doc = asyncGetDocument(url)
                val matcher = Pattern.compile("[0-9]\\.[0-9]\\.[0-9]")
                        .matcher(doc.select("#post-53336845 span > b").text())
                if (matcher.find()) {
                    val version = matcher.group()
                    var message = ""
                    if (version != BuildConfig.VERSION_NAME)
                        message = act.getString(R.string.main_check_app_ver_find,
                                                version,
                                                BuildConfig.VERSION_NAME)
                    else
                        if (user)
                            message = act.getString(R.string.main_check_app_ver_no_find)

                    if (message.isNotEmpty())
                        launch(UI) {
                            act.alert {
                                this.message = message
                                positiveButton(R.string.main_check_app_ver_close) {}
                                negativeButton(R.string.main_check_app_ver_go_to) {
                                    act.browse(url)
                                }
                            }.show()
                        }
                }
            } catch (ex: Throwable) {
                launch(UI) {
                    act.longToast(R.string.main_check_app_ver_error)
                }
            }
        }
    }
}
