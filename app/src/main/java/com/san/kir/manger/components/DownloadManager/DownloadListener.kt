package com.san.kir.manger.components.DownloadManager

import com.san.kir.manger.room.models.DownloadItem

interface DownloadListener {
    fun onUpdate(item: DownloadItem) {}
    fun onComplete(item: DownloadItem) {}
    fun onStart(item: DownloadItem) {}
    fun onPause(item: DownloadItem) {}
    fun onDelete(item: DownloadItem) {}
    fun onResume(item: DownloadItem) {}
    fun onAdd(item: DownloadItem) {}
    fun onDeleteAll() {}
    fun onResumeAll(items: ArrayList<DownloadItem>) {}
    fun onError(item: DownloadItem) {}
}
