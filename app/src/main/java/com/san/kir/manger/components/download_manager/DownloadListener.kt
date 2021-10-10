package com.san.kir.manger.components.download_manager

import com.san.kir.manger.room.entities.Chapter

interface DownloadListener {
    fun onProgress(item: Chapter)
    fun onCompleted(item: Chapter)
    fun onPaused(item: Chapter)
    fun onQueued(item: Chapter)
    fun onError(item: Chapter, cause: Throwable?)
}
