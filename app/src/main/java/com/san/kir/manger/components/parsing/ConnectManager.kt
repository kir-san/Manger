package com.san.kir.manger.components.parsing

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.san.kir.manger.BuildConfig
import com.san.kir.manger.utils.extensions.closeAsync
import com.san.kir.manger.utils.extensions.createDirs
import com.san.kir.manger.utils.extensions.createNewFileAsync
import com.san.kir.manger.utils.extensions.log
import com.san.kir.manger.utils.extensions.sinkAsync
import com.san.kir.manger.utils.extensions.writeAllAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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
import okio.buffer
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
    private val imageCacheDirectory = File(context.cacheDir, "image_cache")

    private val defaultCache = Cache(
        defaultCacheDirectory,
        15L * 1024L * 1024L
    )

    private val imageCache = Cache(
        imageCacheDirectory,
        50L * 1024L * 1024L
    )

    private val cookieJar = AndroidCookieJar()

    private val defaultClient by lazy {
        OkHttpClient.Builder()
            .cache(defaultCache)
            .cookieJar(cookieJar)
            .build()
    }

    private val imageDownloadClient by lazy {
        OkHttpClient.Builder()
            .cache(imageCache)
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

    @OptIn(ExperimentalTime::class)
    suspend fun getDocument(
        url: String = "",
        client: OkHttpClient = defaultClient,
        headers: Headers = defaultHeaders,
        cacheControl: CacheControl = defaultCacheControl,
        request: Request = url.getRequest(headers, cacheControl),
    ): Document {
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
                return withContext(Dispatchers.IO) {
                    Jsoup.parse(responce.body?.byteStream(), "UTF-8", "")
                }
            }
        }
        return Document("")
    }

    fun nameFromUrl(url: String): String {
        val pat = Pattern.compile("[\\w.-]+\\.[a-z]{3,4}")
            .matcher(prepareUrl(url))
        var name = ""
        while (pat.find())
            name = pat.group()
        return name
    }

    suspend fun downloadBitmap(url: String): Bitmap? {
        val response = awaitNewCall(url, client = imageDownloadClient)

        val buffer = Buffer()

        return withContext(Dispatchers.IO) {
            response.body?.source()?.let { source ->
                buffer.writeAllAsync(source)
                BitmapFactory.decodeStream(buffer.inputStream())
            } ?: kotlin.run {
                null
            }
        }
    }

    suspend fun downloadFile(file: File, url: String): Long {
        var contentLength = 0L

        file.delete()
        file.parentFile?.createDirs()

        file.createNewFileAsync()

        val response = awaitNewCall(url)

        response.body?.contentLength()?.let { contentLength = it }

        withContext(Dispatchers.IO) {
            val sink = file.sinkAsync().buffer()
            response.body?.source()?.let { sink.writeAllAsync(it) }
            sink.closeAsync()
        }

        return contentLength
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

    companion object {

        val defaultHeaders = Headers.Builder()
            .add("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:87.0) Gecko/20100101 Firefox/87.0")
            .add("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            //                .add("Accept-Encoding", "gzip, deflate, br")
            .add("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
            .add("Cache-Control", "no-cache")
            .add("Connection", "keep-alive")
            .add("Upgrade-Insecure-Requests", "1").build()

        val defaultCacheControl = CacheControl.Builder().maxAge(10, TimeUnit.MINUTES).build()
    }
}

fun String.getRequest(
    headers: Headers = ConnectManager.defaultHeaders,
    cacheControl: CacheControl = ConnectManager.defaultCacheControl,
): Request {
    val temp = if (this.contains("http").not()) {
        this.removePrefix("/").removePrefix("/")
        "https://$this"
    } else {
        this
    }
    if (BuildConfig.DEBUG) {
        log("getRequest for $temp")
    }
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

