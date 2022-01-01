package com.san.kir.core.download

import com.san.kir.data.models.Chapter

interface DownloadListener {
    fun onProgress(item: Chapter)
    fun onCompleted(item: Chapter)
    fun onPaused(item: Chapter)
    fun onQueued(item: Chapter)
    fun onError(item: Chapter, cause: Throwable?)
}
