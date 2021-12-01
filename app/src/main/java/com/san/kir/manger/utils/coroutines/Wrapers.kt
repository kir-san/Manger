package com.san.kir.manger.utils.coroutines

import okio.BufferedSource
import okio.Sink
import okio.sink
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.InputStream
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun File.createNewFileAsync(): Boolean {
    return suspendCoroutine { continuation ->
        thread {
            kotlin.runCatching {
                continuation.resume(createNewFile())
            }.onFailure {
                continuation.resumeWithException(it)
            }
        }
    }
}

suspend fun File.sinkAsync(): Sink {
    return suspendCoroutine { continuation ->
        thread {
            kotlin.runCatching {
                continuation.resume(sink())
            }.onFailure {
                continuation.resumeWithException(it)
            }
        }
    }
}

suspend fun parseAsync(input: InputStream?, charsetName: String, baseUri: String): Document {
    return suspendCoroutine { continuation ->
        thread {
            runCatching {
                continuation.resume(Jsoup.parse(input, charsetName, baseUri))
            }.onFailure {
                continuation.resumeWithException(it)
            }
        }
    }
}

suspend fun BufferedSource.closeAsync() {
    return suspendCoroutine { continuation ->
        thread {
            kotlin.runCatching {
                continuation.resume(close())
            }.onFailure {
                continuation.resumeWithException(it)
            }
        }
    }
}
