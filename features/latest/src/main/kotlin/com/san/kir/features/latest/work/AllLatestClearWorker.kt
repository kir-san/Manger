package com.san.kir.features.latest.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.san.kir.data.db.dao.ChapterDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AllLatestClearWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val chapterDao: ChapterDao,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        runCatching {
            chapterDao.update(
                *chapterDao.items()
                    .filter { it.isInUpdate }
                    .onEach { it.isInUpdate = false }
                    .toTypedArray()
            )
        }.fold(
            onSuccess = {
                return Result.success()
            },
            onFailure = {
                it.printStackTrace()
                return Result.failure()
            }
        )
    }
}

