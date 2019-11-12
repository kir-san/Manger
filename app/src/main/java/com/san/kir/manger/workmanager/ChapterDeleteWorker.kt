package com.san.kir.manger.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.StorageRepository
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.MangaColumn

abstract class ChapterDeleteWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val mChapterRepository = ChapterRepository(appContext)
    val mMangaRepository = MangaRepository(appContext)
    val mStorageRepository = StorageRepository(appContext)

    suspend fun items() = mChapterRepository.getItems()

    companion object {
        const val tag = "deleteChapter"
        inline fun <reified T : ChapterDeleteWorker> addTask(ctx: Context, manga: Manga) {
            val data = workDataOf(MangaColumn.unic to manga.unic)
            val task = OneTimeWorkRequestBuilder<T>()
                .addTag(tag)
                .setInputData(data)
                .build()
            WorkManager.getInstance(ctx).enqueue(task)
        }
    }
}

