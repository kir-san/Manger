package com.san.kir.manger.workmanager

import android.content.Context
import androidx.work.WorkerParameters
import com.san.kir.manger.room.entities.action
import com.san.kir.manger.utils.enums.ChapterStatus
import kotlinx.coroutines.coroutineScope

class DownloadedLatestClearWorker(appContext: Context, workerParams: WorkerParameters) :
    LatestClearWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        kotlin.runCatching {
            mChapterRepository.update(
                *items()
                    .filter { it.isInUpdate }
                    .filter { it.action == ChapterStatus.DELETE }
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
