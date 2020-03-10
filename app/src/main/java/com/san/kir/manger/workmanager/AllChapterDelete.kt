package com.san.kir.manger.workmanager

import android.content.Context
import androidx.work.WorkerParameters
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.MangaColumn
import com.san.kir.manger.utils.extensions.delFiles
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.shortPath
import kotlinx.coroutines.coroutineScope

class AllChapterDelete(appContext: Context, workerParams: WorkerParameters) :
    ChapterDeleteWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        val unic = inputData.getString(MangaColumn.unic)

        kotlin.runCatching {
            val manga = mMangaRepository.getItem(unic!!)
            deleteAllChapters(manga)
            updateStorageItem(manga)
        }.fold(
            onSuccess = { Result.success() },
            onFailure = {
                it.printStackTrace()
                Result.failure()
            }
        )
    }

    private fun deleteAllChapters(manga: Manga) {
        val files = getFullPath(manga.path).listFiles()!!.map { it.shortPath }

        delFiles(files.toList())
    }

    private suspend fun updateStorageItem(manga: Manga) {
        val storageItem = mStorageRepository.items().first { it.path == getFullPath(manga.path).shortPath }

        mStorageRepository.update(
            mStorageRepository.getSizeAndIsNew(storageItem)
        )
    }
}
