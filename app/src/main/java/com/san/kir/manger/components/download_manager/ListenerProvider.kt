package com.san.kir.manger.components.download_manager

import com.san.kir.manger.data.room.entities.Chapter
import java.util.*

class ListenerProvider {
    private val listeners =
        Collections.synchronizedSet(mutableSetOf<DownloadListener>())
    private val keys =
        Collections.synchronizedSet(mutableSetOf<String>())

    val mainListener = object : DownloadListener {
        override fun onQueued(item: Chapter) {
            listeners.forEach {
                it.onQueued(item)
            }
        }

        override fun onCompleted(item: Chapter) {
            listeners.forEach {
                it.onCompleted(item)
            }
        }

        override fun onError(
            item: Chapter,
            cause: Throwable?
        ) {
            listeners.forEach {
                it.onError(item, cause)
            }
        }

        override fun onProgress(item: Chapter) {
            listeners.forEach {
                it.onProgress(item)
            }
        }

        override fun onPaused(item: Chapter) {
            listeners.forEach {
                it.onPaused(item)
            }
        }
    }

    fun <T : Any> addListener(tag: T, listener: DownloadListener) {
        val key = tag::class.java.name
        if (!keys.contains(key)) {
            keys.add(key)
            listeners.add(listener)
        }
    }

    fun clear() {
        listeners.clear()
    }
}
