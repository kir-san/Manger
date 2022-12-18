package com.san.kir.data.models.base

import com.san.kir.core.support.DownloadState

interface BaseTask<T : BaseTask<T>> {
    val id: Long
    val state: DownloadState

    fun setPaused(): T
}
