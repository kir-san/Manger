package com.san.kir.manger.foreground_work.workmanager

import android.content.Context
import androidx.work.WorkerParameters
import com.san.kir.core.support.ChapterStatus
import kotlinx.coroutines.coroutineScope

class DownloadedLatestClearWorker(appContext: Context, workerParams: WorkerParameters) :
    LatestClearWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        kotlin.runCatching {
            mChapterRepository.update(
                *items()
                    .filter { it.isInUpdate }
                    .filter { it.action == com.san.kir.core.support.ChapterStatus.DELETE }
                    .onEach { it.isInUpdate = false }
                    .toTypedArray()
            )
        }.fold(
            onSuccess = { Result.success() },
            onFailure = {
                it.printStackTrace()
                Result.failure()
            }
        )
    }
}
