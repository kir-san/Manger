package com.san.kir.core.internet

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.core.utils.createDirs
import com.san.kir.core.utils.log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.internal.closeQuietly
import okio.Buffer
import okio.BufferedSink
import okio.buffer
import okio.sink
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Singleton
class ConnectManager @Inject constructor(context: Application) {
    private val defaultCacheDirectory = File(context.cacheDir, "http_cache")

    private val defaultCache = Cache(
        defaultCacheDirectory,
        15L * 1024L * 1024L
    )

    private val cookieJar = AndroidCookieJar()

    private val defaultClient by lazy {
        OkHttpClient.Builder()
            .cache(defaultCache)
            .cookieJar(cookieJar)
            .build()
    }

    private val retryKey = "Retry-After"

    private suspend fun awaitNewCall(
        url: String = "",
        client: OkHttpClient = defaultClient,
        headers: Headers = defaultHeaders,
        cacheControl: CacheControl = defaultCacheControl,
        request: Request = url.getRequest(headers, cacheControl),
    ): Response {
        return client.newCall(request).await()
    }

    suspend fun getText(url: String = ""): String = getDocument(url = url).body().wholeText()

    @OptIn(ExperimentalTime::class)
    suspend fun getDocument(
        url: String = "",
        client: OkHttpClient = defaultClient,
        headers: Headers = defaultHeaders,
        cacheControl: CacheControl = defaultCacheControl,
        request: Request = url.getRequest(headers, cacheControl),
    ): Document = withIoContext {
        val responce = awaitNewCall(
            client = client,
            headers = headers,
            cacheControl = cacheControl,
            request = request,
        )

        when (responce.code) {
            429 -> {
                val toMultimap = responce.headers.toMultimap()
                val timeOut = toMultimap[retryKey]?.first()?.toLong()
                if (timeOut != null) {
                    log("delay $timeOut seconds")
                    delay(Duration.seconds(timeOut))
                } else {
                    delay(Duration.seconds(10))
                }
            }
            else -> {
                return@withIoContext Jsoup.parse(responce.body?.byteStream(), "UTF-8", "")
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
        onFinish: (bm: Bitmap?, size: Long, time: Long) -> Unit = { _, _, _ -> },
        onProgress: (percent: Float) -> Unit = {},
    ): Bitmap? =
        withIoContext {
            val buffer = Buffer()
            var size: Long = 0
            var time: Long = 0
            defaultClient.newCall(url.getRequest())
                .awaitDownload(buffer, onProgress) { s, t ->
                    size = s
                    time = t
                }

            val bm = tryGetBitmap(buffer)

            onFinish(bm, size, time)

            bm
        }

    private fun tryGetBitmap(buffer: Buffer): Bitmap? {
        return try {
            BitmapFactory.decodeStream(buffer.inputStream())
        } catch (ex: Throwable) {
            null
        }
    }

    suspend fun downloadFile(
        file: File,
        url: String,
        onFinish: (size: Long, time: Long) -> Unit = { _, _ -> },
        onProgress: ((percent: Float) -> Unit) = {},
    ): Long =
        withIoContext {
            file.delete()
            file.parentFile?.createDirs()

            file.createNewFile()

            defaultClient.newCall(url.getRequest(cacheControl = noCacheControl))
                .awaitDownload(file.sink().buffer(), onProgress, onFinish)

            file.length()
        }


    fun prepareUrl(url: String) = url.removeSurrounding("\"", "\"")

    // Based on https://github.com/gildor/kotlin-coroutines-okhttp
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun Call.await(): Response {
        return suspendCancellableCoroutine { continuation ->
            enqueue(
                object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        continuation.resume(response) {
                            response.body?.closeQuietly()
                        }
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        // Don't bother with resuming the continuation if it is already cancelled.
                        if (continuation.isCancelled) return
                        continuation.resumeWithException(e)
                    }
                }
            )

            continuation.invokeOnCancellation {
                try {
                    cancel()
                } catch (ex: Throwable) {
                    // Ignore cancel exception
                }
            }
        }
    }

    // Based on https://github.com/gildor/kotlin-coroutines-okhttp
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun Call.awaitDownload(
        sink: BufferedSink,
        onProgress: (percent: Float) -> Unit,
        onFinish: (size: Long, time: Long) -> Unit = { _, _ -> },
    ) {
        return suspendCancellableCoroutine { continuation ->
            enqueue(
                object : Callback {
                    private val SEGMENT_SIZE = 2048L // okio.Segment.SIZE

                    override fun onResponse(call: Call, response: Response) {
                        response.body?.let { body ->
                            kotlin.runCatching {
                                val startTime = System.currentTimeMillis()
                                val contentLength = body.contentLength()
                                var total: Long = 0
                                var read: Long

                                while (
                                    body.source().read(sink.buffer, SEGMENT_SIZE)
                                        .apply { read = this } != -1L
                                ) {
                                    total += read
                                    sink.emitCompleteSegments()
                                    onProgress(total.toFloat() / contentLength.toFloat())
                                }

                                sink.writeAll(body.source())
                                sink.close()
                                body.source().close()

                                onFinish(contentLength, System.currentTimeMillis() - startTime)
                                continuation.resume(Unit) {}
                            }.onFailure {
                                sink.close()
                                body.source().close()

                                it.printStackTrace()
                                continuation.resumeWithException(it)
                            }
                        }
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        // Don't bother with resuming the continuation if it is already cancelled.
                        if (continuation.isCancelled) return
                        continuation.resumeWithException(e)
                    }
                }
            )

            continuation.invokeOnCancellation {
                try {
                    cancel()
                } catch (ex: Throwable) {
                    // Ignore cancel exception
                }
            }
        }
    }

    companion object {

        val defaultHeaders = Headers.Builder()
            .add(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:87.0) Gecko/20100101 Firefox/87.0"
            )
            .add(
                "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
            )
            //                .add("Accept-Encoding", "gzip, deflate, br")
            .add("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
            .add("Cache-Control", "no-cache")
            .add("Connection", "keep-alive")
            .add("Upgrade-Insecure-Requests", "1").build()

        val defaultCacheControl = CacheControl.Builder().maxAge(10, TimeUnit.MINUTES).build()
        val noCacheControl = CacheControl.Builder().noCache().noStore().build()
    }
}

fun String.getRequest(
    headers: Headers = ConnectManager.defaultHeaders,
    cacheControl: CacheControl = ConnectManager.defaultCacheControl,
): Request {
    val prepare = trim().removeSurrounding("\"", "\"").trim()
    val temp = if (prepare.contains("http").not()) {
        prepare.removePrefix("/").removePrefix("/")
        "https://$prepare"
    } else {
        prepare
    }

    log("getRequest for $temp")

    return Request.Builder()
        .url(temp)
        .headers(headers)
        .cacheControl(cacheControl)
        .build()
}

fun String.postRequest(
    postData: RequestBody,
    headers: Headers = ConnectManager.defaultHeaders,
    cacheControl: CacheControl = ConnectManager.defaultCacheControl,
): Request {
    return Request.Builder()
        .url(this)
        .post(postData)
        .headers(headers)
        .cacheControl(cacheControl)
        .build()
}

