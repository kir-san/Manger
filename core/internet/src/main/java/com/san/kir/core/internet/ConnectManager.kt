package com.san.kir.core.internet

import android.app.Application
import android.graphics.BitmapFactory
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.core.utils.createDirs
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.delay
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import okio.BufferedSink
import okio.buffer
import okio.sink
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
            val buffer = Buffer()
            val (size, time) = download(buffer, url, onProgress)
            BitmapResult(
                bitmap = BitmapFactory.decodeStream(buffer.inputStream()),
                size = size,
                time = time
            )
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun downloadFile(
        file: File,
        url: String,
        onProgress: ((percent: Float) -> Unit) = {},
    ): Result<DownloadResult> = runCatching {
        withIoContext {
            file.delete()
            file.parentFile?.createDirs()
            file.createNewFile()

            kotlin.runCatching {
                download(file.sink().buffer(), url, onProgress)
            }.onFailure { file.delete() }.getOrThrow()
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
        buffer: BufferedSink,
        url: String,
        onProgress: ((percent: Float) -> Unit) = {},
    ): DownloadResult {
        val startTime = System.currentTimeMillis()

        val response = defaultClient.get(url.prepare())
        val source = response.bodyAsChannel()
        val contentLength = response.contentLength() ?: 1

        var total = 0
        var read: Int

        val tempBuffer = Buffer()
        tempBuffer.use {
            val byteBuffer = ByteArray(SEGMENT_SIZE)
            do {
                read = source.readAvailable(byteBuffer, 0, SEGMENT_SIZE)

                if (read > 0) {
                    it.write(
                        if (read < SEGMENT_SIZE) {
                            byteBuffer.sliceArray(0 until read)
                        } else {
                            byteBuffer
                        }
                    )
                    total += read
                    onProgress(total.toFloat() / contentLength)
                }
            } while (read >= 0)
        }

        buffer.use {
            it.writeAll(tempBuffer)
        }

        return DownloadResult(contentLength, System.currentTimeMillis() - startTime)
    }

    companion object {
        private val SEGMENT_SIZE = 2048 // okio.Segment.SIZE

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
