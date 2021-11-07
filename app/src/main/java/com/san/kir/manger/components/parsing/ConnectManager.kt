package com.san.kir.manger.components.parsing

import android.app.Application
import android.webkit.CookieManager
import com.san.kir.manger.utils.extensions.createDirs
import com.san.kir.manger.utils.extensions.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.internal.closeQuietly
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
class ConnectManager @Inject constructor(
    context: Application,
) {
    private val cacheDirectory = File(context.cacheDir, "http_cache")

    private val cache = Cache(
        cacheDirectory,
        50L * 1024L * 1024L // 50 MiB
    )

    private val cookieJar = AndroidCookieJar()

    private val defaultClient by lazy {
        OkHttpClient.Builder()
            .cache(cache)
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

    suspend fun downloadImage(file: File, url: String): Long {
        var contentLength = 0L

        file.delete()
        file.parentFile?.createDirs()

        withContext(Dispatchers.IO) {
            file.createNewFile()
        }

        val response = awaitNewCall(url)

        response.body?.contentLength()?.let { contentLength = it }

        withContext(Dispatchers.IO) {
            val sink = file.sink().buffer()
            response.body?.source()?.let { sink.writeAll(it) }
            sink.close()
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
                        if (!response.isSuccessful) {
                            continuation.resumeWithException(Exception("HTTP error ${response.code}"))
                            return
                        }

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
    return Request.Builder()
        .url(this)
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

class AndroidCookieJar : CookieJar {

    private val manager = CookieManager.getInstance()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlString = url.toString()
        cookies.forEach { manager.setCookie(urlString, it.toString()) }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return get(url)
    }

    fun get(url: HttpUrl): List<Cookie> {
        val cookies = manager.getCookie(url.toString())

        return if (cookies != null && cookies.isNotEmpty()) {
            cookies.split(";").mapNotNull { Cookie.parse(url, it) }
        } else {
            emptyList()
        }
    }

}
