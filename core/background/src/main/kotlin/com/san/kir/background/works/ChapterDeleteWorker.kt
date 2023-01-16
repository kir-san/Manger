package com.san.kir.background.works

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.san.kir.data.models.base.Manga

abstract class ChapterDeleteWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val tag = "deleteChapter"
        inline fun <reified T : ChapterDeleteWorker> addTask(ctx: Context, manga: Manga) {
            val data = workDataOf("id" to manga.id)
            val task = OneTimeWorkRequestBuilder<T>()
                .addTag(tag)
                .setInputData(data)
                .build()
            WorkManager.getInstance(ctx).enqueue(task)
        }
    }
}

