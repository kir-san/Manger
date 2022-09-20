package com.san.kir.background.works

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.san.kir.core.utils.getFullPath
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.base.Manga
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MangaDeleteWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val mangaDao: MangaDao,
    private val chapterDao: ChapterDao,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        val mangaId = inputData.getLong(Manga.Col.id, -1)
        val withFiles = inputData.getBoolean(withFilesTag, false)

        return runCatching {
            removeWithChapters(mangaId, withFiles)
        }.fold(
            onSuccess = {
                Result.success()
            },
            onFailure = {
                it.printStackTrace()
                Result.failure()
            }
        )
    }

    private suspend fun removeWithChapters(mangaId: Long, withFiles: Boolean = false) {
        val manga = mangaDao.itemById(mangaId)

        mangaDao.delete(manga)

        with(chapterDao.getItemsWhereManga(manga.name)) {
            chapterDao.delete(this)
        }

        if (withFiles) {
            getFullPath(manga.path).deleteRecursively()
        }
    }

    companion object {
        const val tag = "mangaDelete"

        const val withFilesTag = "withFiles"

        fun addTask(ctx: Context, mangaId: Long, withFiles: Boolean = false) {
            val data = workDataOf(Manga.Col.id to mangaId, withFilesTag to withFiles)
            val deleteManga = OneTimeWorkRequestBuilder<MangaDeleteWorker>()
                .setInputData(data)
                .addTag(tag)
                .build()
            WorkManager.getInstance(ctx).enqueue(deleteManga)
        }
    }
}
