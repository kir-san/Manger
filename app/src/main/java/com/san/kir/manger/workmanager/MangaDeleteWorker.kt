package com.san.kir.manger.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.MangaColumn
import com.san.kir.manger.utils.extensions.getFullPath
import kotlinx.coroutines.coroutineScope

class MangaDeleteWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {

        val manga = inputData.getString(MangaColumn.unic)
        val withFiles = inputData.getBoolean("withFiles", false)

        kotlin.runCatching {
            removeWithChapters(manga!!, withFiles)
        }.fold(
            onSuccess = { Result.success() },
            onFailure = {
                it.printStackTrace()
                Result.failure()
            }
        )
    }

    private suspend fun removeWithChapters(unic: String, withFiles: Boolean = false) {

        val mangaRepo = MangaRepository(applicationContext)
        val manga = mangaRepo.getItem(unic)

        mangaRepo.delete(manga)

        ChapterRepository(applicationContext).deleteItems(manga.unic)

        if (withFiles) {
            getFullPath(manga.path).deleteRecursively()
        }
    }

    companion object {
        const val tag = "mangaDelete"

        fun addTask(ctx: Context, manga: Manga, withFiles: Boolean = false) {
            val data = workDataOf(MangaColumn.unic to manga.unic, "withFiles" to withFiles)
            val deleteManga = OneTimeWorkRequestBuilder<MangaDeleteWorker>()
                .setInputData(data)
                .addTag(tag)
                .build()
            WorkManager.getInstance(ctx).enqueue(deleteManga)
        }
    }
}
