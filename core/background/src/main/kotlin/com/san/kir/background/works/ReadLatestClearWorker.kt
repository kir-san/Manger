package com.san.kir.background.works

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.san.kir.data.db.dao.ChapterDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReadLatestClearWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val chapterDao: ChapterDao,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        kotlin.runCatching {
            chapterDao.update(
                *chapterDao.items()
                    .filter { it.isInUpdate && it.isRead }
                    .map { it.copy(isInUpdate = false) }
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
