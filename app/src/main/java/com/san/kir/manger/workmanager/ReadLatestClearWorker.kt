package com.san.kir.manger.workmanager

import android.content.Context
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope

class ReadLatestClearWorker(appContext: Context, workerParams: WorkerParameters) :
    LatestClearWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        kotlin.runCatching {
            mChapterRepository.update(
                *items()
                    .filter { it.isInUpdate && it.isRead }
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

