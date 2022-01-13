package com.san.kir.manger.foreground_work.workmanager

import android.content.Context
import androidx.work.WorkerParameters
import com.san.kir.core.utils.delChapters
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.shortPath
import com.san.kir.data.models.base.Manga

class ReadChapterDelete(appContext: Context, workerParams: WorkerParameters) :
    ChapterDeleteWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val manga = inputData.getString(Manga.Col.name)

        return kotlin.runCatching {
            deleteReadChapters(manga!!)
            updateStorageItem(manga)
        }.fold(
            onSuccess = { Result.success() },
            onFailure = {
                it.printStackTrace()
                Result.failure()
            }
        )
    }

    private suspend fun deleteReadChapters(manga: String) {
        val chapters = mChapterRepository
            .getItems(manga)
            .filter { chapter -> chapter.isRead }
            .map { it.path }

        delChapters(chapters)
    }

    private suspend fun updateStorageItem(unic: String) {
        val manga = mMangaRepository.getItem(unic)
        val storageItem = mStorageRepository.items()
            .first { it.path == getFullPath(manga.path).shortPath }

        mStorageRepository.update(
            mStorageRepository.getSizeAndIsNew(storageItem)
        )
    }
}
