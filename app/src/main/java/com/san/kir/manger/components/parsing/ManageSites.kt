package com.san.kir.manger.components.parsing

import android.support.v7.app.AppCompatActivity
import com.san.kir.manger.BuildConfig
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.sites.Allhentai
import com.san.kir.manger.components.parsing.sites.Henchan
import com.san.kir.manger.components.parsing.sites.Mangachan
import com.san.kir.manger.components.parsing.sites.Mintmanga
import com.san.kir.manger.components.parsing.sites.Readmanga
import com.san.kir.manger.components.parsing.sites.Selfmanga
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.SiteCatalogElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse
import org.jetbrains.anko.longToast
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

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

    fun getDocument(url: String): Document =
        Jsoup.parse(openLink(url).body()?.bytes()?.toString(Charset.forName("UTF-8")))


    fun chapters(manga: Manga): List<Chapter>? {
        val site = CATALOG_SITES.firstOrNull { manga.host == it.host }
        return site?.chapters(manga)
    }

    // Загрузка полной информации для элемента в каталоге
    fun getFullElement(simpleElement: SiteCatalogElement) = GlobalScope.async {
        CATALOG_SITES[simpleElement.siteId].getFullElement(simpleElement)
    }


    // Получение страниц для главы
    fun pages(item: DownloadItem) = CATALOG_SITES
        .first { item.link.contains(it.catalogName) }
        .pages(item)


}

private const val url = "http://4pda.ru/forum/index.php?showtopic=772886&st=0#entry53336845"
private var isCheck = false

// функция проверки новой версии приложения на сайте 4pda.ru
fun checkNewVersion(act: AppCompatActivity, user: Boolean = false) {
    if (!isCheck || user)
        GlobalScope.launch(Dispatchers.Default) {
            try {
                val doc = ManageSites.getDocument(url)
                val matcher = Pattern.compile("[0-9]\\.[0-9]\\.[0-9]")
                    .matcher(doc.select("#post-53336845 span > b").text())
                if (matcher.find()) {
                    val version = matcher.group()
                    var message = ""
                    if (version != BuildConfig.VERSION_NAME)
                        message = act.getString(
                            R.string.main_check_app_ver_find,
                            version,
                            BuildConfig.VERSION_NAME
                        )
                    else
                        if (user) {
                            message = act.getString(R.string.main_check_app_ver_no_find)
                            isCheck = true
                        }

                    if (message.isNotEmpty())
                        launch(Dispatchers.Main) {
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
                launch(Dispatchers.Main) {
                    act.longToast(R.string.main_check_app_ver_error)
                }
            }
        }
}
