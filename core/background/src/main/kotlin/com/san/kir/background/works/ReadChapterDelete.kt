package com.san.kir.background.works

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.san.kir.core.utils.delChapters
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.shortPath
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.StorageDao
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.getSizeAndIsNew
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReadChapterDelete @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val mangaDao: MangaDao,
    private val chapterDao: ChapterDao,
    private val storageDao: StorageDao,
) : ChapterDeleteWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val mangaId = inputData.getLong("id", -1)
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
        val chapters = chapterDao
            .itemsByMangaId(manga.id)
            .filter { chapter -> chapter.isRead }
            .map { it.path }

        delChapters(chapters)
    }

    private suspend fun updateStorageItem(manga: Manga) {
        val storageItem = storageDao.items()
            .first { it.path == getFullPath(manga.path).shortPath }

        val file = getFullPath(storageItem.path)

        storageDao.update(
            storageItem.getSizeAndIsNew(file, false, chapterDao.itemsByMangaId(manga.id))
        )
    }
}
