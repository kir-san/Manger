package com.san.kir.ui.latest.work

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager


object LatestClearWorkers {
    const val tag = "cleanLatest"

    fun clearAll(ctx: Context) {
        val task = OneTimeWorkRequestBuilder<AllLatestClearWorker>()
            .addTag(tag)
            .build()

        WorkManager
            .getInstance(ctx)
            .enqueue(task)
    }

    fun clearDownloaded(ctx: Context) {
        val task = OneTimeWorkRequestBuilder<DownloadedLatestClearWorker>()
            .addTag(tag)
            .build()

        WorkManager
            .getInstance(ctx)
            .enqueue(task)
    }

    fun clearReaded(ctx: Context) {
        val task = OneTimeWorkRequestBuilder<ReadLatestClearWorker>()
            .addTag(tag)
            .build()

        WorkManager
            .getInstance(ctx)
            .enqueue(task)
    }
}

