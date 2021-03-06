package com.san.kir.manger.components.download_manager

import com.san.kir.manger.room.entities.DownloadItem

interface DownloadListener {
    fun onProgress(item: DownloadItem)
    fun onCompleted(item: DownloadItem)
    fun onPaused(item: DownloadItem)
    fun onQueued(item: DownloadItem)
    fun onError(item: DownloadItem, cause: Throwable?)
}
