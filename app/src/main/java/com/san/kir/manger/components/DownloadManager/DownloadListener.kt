package com.san.kir.manger.components.DownloadManager

import com.san.kir.manger.room.models.DownloadItem

interface DownloadListener {
    fun onProgress(item: DownloadItem) {}
    fun onCompleted(item: DownloadItem) {}
    fun onPaused(item: DownloadItem) {}
    fun onQueued(item: DownloadItem) {}
    fun onError(item: DownloadItem, cause: Throwable?) {}
}
