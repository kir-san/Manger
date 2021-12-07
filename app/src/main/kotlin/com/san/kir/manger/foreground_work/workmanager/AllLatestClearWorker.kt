package com.san.kir.manger.foreground_work.workmanager

import android.content.Context
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope

class AllLatestClearWorker(appContext: Context, workerParams: WorkerParameters) :
    LatestClearWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        runCatching {
            mChapterRepository.update(
                *items()
                    .filter { it.isInUpdate }
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

