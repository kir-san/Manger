package com.san.kir.core.internet

import android.app.Application
import android.graphics.BitmapFactory
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.core.utils.createDirs
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.contentLength
import io.ktor.util.StringValuesBuilderImpl
import kotlinx.coroutines.delay
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.logging.HttpLoggingInterceptor
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class ConnectManager @Inject constructor(context: Application) {
    private val defaultCacheDirectory = File(context.cacheDir, "http_cache")

    private val defaultCache = Cache(
        defaultCacheDirectory,
        15L * 1024L * 1024L
    )

    private val cookieJar = AndroidCookieJar()

    private val defaultClient by lazy {
        HttpClient(OkHttp) {

            expectSuccess = true

            engine {
                config {
                    cache(defaultCache)
                    cookieJar(cookieJar)
                    retryOnConnectionFailure(true)
                    callTimeout(20_0000L, TimeUnit.MILLISECONDS)
                    readTimeout(20_0000L, TimeUnit.MILLISECONDS)
                    writeTimeout(20_0000L, TimeUnit.MILLISECONDS)
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    })
                }
            }

            defaultRequest {
                headers { appendAll(defaultHeaders) }
            }

            BrowserUserAgent()

        }
    }

    private val retryKey = "Retry-After"

    suspend fun getText(url: String = ""): String = getDocument(url = url).body().wholeText()

    suspend fun getDocument(
        url: String = "",
        formParams: Parameters? = null,
    ): Document =
        withIoContext {
            val response =
                formParams
                    ?.let { defaultClient.submitForm(url.prepare(), it) }
                    ?: defaultClient.get(url.prepare())

            when (response.status) {
                HttpStatusCode.TooManyRequests -> {
                    val toMultimap = response.headers
                    val timeOut = toMultimap[retryKey]?.first()?.code?.toLong()
                    if (timeOut != null) {
                        Timber.v("delay $timeOut seconds")
                        delay(timeOut.seconds)
                    } else {
                        delay(10.seconds)
                    }
                }

                else -> {
                    return@withIoContext Jsoup.parse(response.bodyAsText(), url)
                }
            }
            Document("")
        }

    fun nameFromUrl(url: String): String {
        val pat = Pattern.compile("[\\w.-]+\\.[a-z]{3,4}")
            .matcher(prepareUrl(url))
        var name = ""
        while (pat.find())
            name = pat.group()
        return name
    }

    fun nameFromUrl2(url: String): String {
        return url.split("/").last()
    }

    suspend fun downloadBitmap(
        url: String,
        onProgress: (percent: Float) -> Unit = {},
    ): Result<BitmapResult> = kotlin.runCatching {
        withIoContext {
            val (source, length, time) = download(url, onProgress)
            BitmapResult(
                bitmap = BitmapFactory.decodeByteArray(source, 0, source.size),
                size = length,
                time = time
            )
        }
    }

    suspend fun downloadFile(
        file: File,
        url: String,
        onProgress: suspend ((percent: Float) -> Unit) = {},
    ): Result<DownloadResult> = runCatching {
        withIoContext {
            runCatching { download(url, onProgress) }
                .onSuccess { result ->
                    file.delete()
                    file.parentFile?.createDirs()
                    file.createNewFile()
                    file.writeBytes(result.source)
                }
                .onFailure { file.delete() }.getOrThrow()
        }
    }

    fun prepareUrl(url: String) = url.removeSurrounding("\"", "\"")

    private fun String.prepare(): String {
        val prepare = trim().removeSurrounding("\"", "\"").trim()
        return if (prepare.contains("http").not()) {
            prepare.removePrefix("/").removePrefix("/")
            "https://$prepare"
        } else {
            prepare
        }
    }

    private suspend fun download(
        url: String,
        onProgress: suspend ((percent: Float) -> Unit) = {},
    ): DownloadResult {
        val startTime = System.currentTimeMillis()
        val contentLength: Long

        val source: ByteArray =
            defaultClient.get(url.prepare()) {
                onDownload { bytesSentTotal, contentLength ->
                    onProgress(bytesSentTotal.toFloat() / contentLength)
                }
            }.apply { contentLength = contentLength() ?: 1 }.body()

        return DownloadResult(
            source,
            contentLength,
            time = System.currentTimeMillis() - startTime
        )
    }

    companion object {
        val defaultHeaders = StringValuesBuilderImpl(true, 4).apply {
            append(
                HttpHeaders.Accept,
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
            )
            append(HttpHeaders.AcceptLanguage, "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
            append(HttpHeaders.Connection, "keep-alive")
            append("Upgrade-Insecure-Requests", "1")
        }.build()

        val defaultCacheControl = CacheControl.Builder().maxAge(10, TimeUnit.MINUTES).build()
        val noCacheControl = CacheControl.Builder().noCache().noStore().build()
    }
}

