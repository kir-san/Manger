package com.san.kir.manger.components.DownloadManager

import com.san.kir.manger.room.models.DownloadItem
import java.util.*
import kotlin.collections.HashMap

class ListenerProvider {
    private val listeners =
        Collections.synchronizedMap(HashMap<String, MutableSet<DownloadListener>>())

    val mainListener = object : DownloadListener {
        override fun onQueued(item: DownloadItem) {
            listeners.forEach { (_, l) ->
                l.forEach {
                    it.onQueued(item)
                }
            }
        }

        override fun onCompleted(item: DownloadItem) {
            listeners.forEach { (_, l) ->
                l.forEach {
                    it.onCompleted(item)
                }
            }
        }

        override fun onError(
            item: DownloadItem,
            cause: Throwable?
        ) {
            listeners.forEach { (_, l) ->
                l.forEach {
                    it.onError(item, cause)
                }
            }
        }

        override fun onProgress(item: DownloadItem) {
            listeners.forEach { (_, l) ->
                l.forEach {
                    it.onProgress(item)
                }
            }
        }

        override fun onPaused(item: DownloadItem) {
            listeners.forEach { (_, l) ->
                l.forEach {
                    it.onPaused(item)
                }
            }
        }
    }

    fun <T : Any> addListener(tag: T, listener: DownloadListener) {
        val Tag = tag::class.java.name
        if (!listeners.containsKey(Tag)) {
            listeners[Tag] = mutableSetOf()
        }
        listeners[Tag]?.add(listener)
    }

    fun <T : Any> removeListeners(tag: T) {
        val Tag = tag::class.java.name
        if (listeners.containsKey(Tag)) {
            listeners[Tag] = mutableSetOf()
        }
    }

    fun clear() {
        listeners.clear()
    }
}
