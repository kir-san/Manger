package com.san.kir.manger.workmanager

import android.content.Context
import androidx.work.WorkerParameters
import com.san.kir.manger.room.entities.MangaColumn
import com.san.kir.manger.utils.extensions.delChapters
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.shortPath
import kotlinx.coroutines.coroutineScope

class ReadChapterDelete(appContext: Context, workerParams: WorkerParameters) :
    ChapterDeleteWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        val manga = inputData.getString(MangaColumn.unic)

        kotlin.runCatching {
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
