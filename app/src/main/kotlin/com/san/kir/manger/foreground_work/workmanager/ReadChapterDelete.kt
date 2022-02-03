package com.san.kir.manger.foreground_work.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.san.kir.core.utils.delChapters
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.shortPath
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.base.Manga
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReadChapterDelete @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val mangaDao: MangaDao,
) : ChapterDeleteWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val mangaId = inputData.getLong(Manga.Col.id, -1)
        val manga = mangaDao.itemById(mangaId)

        return kotlin.runCatching {
            deleteReadChapters(manga)
            updateStorageItem(manga)
        }.fold(
            onSuccess = { Result.success() },
            onFailure = {
                it.printStackTrace()
                Result.failure()
            }
        )
    }

    private suspend fun deleteReadChapters(manga: Manga) {
        val chapters = mChapterRepository
            .getItems(manga.name)
            .filter { chapter -> chapter.isRead }
            .map { it.path }

        delChapters(chapters)
    }

    private suspend fun updateStorageItem(manga: Manga) {
        val storageItem = mStorageRepository.items()
            .first { it.path == getFullPath(manga.path).shortPath }

        mStorageRepository.update(
            mStorageRepository.getSizeAndIsNew(storageItem)
        )
    }
}
