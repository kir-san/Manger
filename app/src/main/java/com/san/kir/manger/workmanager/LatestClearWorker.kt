package com.san.kir.manger.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.san.kir.manger.repositories.ChapterRepository

abstract class LatestClearWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val mChapterRepository = ChapterRepository(appContext)

    suspend fun items() = mChapterRepository.getItems()

    companion object {
        const val tag = "cleanLatest"
        inline fun <reified T : LatestClearWorker> addTask(ctx: Context) {
            val task = OneTimeWorkRequestBuilder<T>().addTag(tag).build()
            WorkManager.getInstance(ctx).enqueue(task)
        }
    }
}
