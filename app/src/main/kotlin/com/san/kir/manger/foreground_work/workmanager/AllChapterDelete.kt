package com.san.kir.manger.foreground_work.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.san.kir.core.utils.delFiles
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.shortPath
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.base.Manga
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AllChapterDelete @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val mangaDao: MangaDao,
) : ChapterDeleteWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val mangaId = inputData.getLong(Manga.Col.id, -1)

        return kotlin.runCatching {
            val manga = mangaDao.itemById(mangaId)
            deleteAllChapters(manga)
            updateStorageItem(manga)
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

    private fun deleteAllChapters(manga: Manga) {
        val files = getFullPath(manga.path).listFiles()!!.map { it.shortPath }

        delFiles(files.toList())
    }

    private suspend fun updateStorageItem(manga: Manga) {
        val storageItem =
            mStorageRepository.items().first { it.path == getFullPath(manga.path).shortPath }

        mStorageRepository.update(
            mStorageRepository.getSizeAndIsNew(storageItem)
        )
    }
}
