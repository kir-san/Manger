package com.san.kir.background.works

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.san.kir.core.support.DIR
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.shortPath
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.StorageDao
import com.san.kir.data.db.dao.itemByPath
import com.san.kir.data.models.base.Storage
import com.san.kir.data.models.base.getSizeAndIsNew
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/*
    Worker для обновления данных о занимаемом месте
*/
@HiltWorker
class StoragesUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val storageDao: StorageDao,
    private val chapterDao: ChapterDao,
    private val mangaDao: MangaDao,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        kotlin.runCatching {
            val list = storageDao.items()

            val storageList = getFullPath(DIR.MANGA).listFiles()

            if (storageList != null && (list.isEmpty() || storageList.size != list.size)) {
                storageList.forEach { dir ->
                    dir.listFiles()?.forEach { item ->
                        if (list.none { it.name == item.name }) {
                            storageDao.insert(
                                Storage(
                                    name = item.name,
                                    path = item.shortPath,
                                    catalogName = dir.name
                                )
                            )
                        }
                    }
                }
            }

            list.map { storage ->
                val file = getFullPath(storage.path)
                val manga = mangaDao.itemByPath(file)

                storageDao.update(
                    storage.getSizeAndIsNew(
                        file,
                        manga == null,
                        manga?.let { chapterDao.itemsWhereMangaId(it.id) })
                )
            }
        }.fold(
            onSuccess = {
                return Result.success()
            },
            onFailure = {
                it.printStackTrace()
                return Result.failure()
            }
        )
    }

    companion object {
        const val tag = "updateStorages"

        fun runTask(ctx: Context) {
            val task = OneTimeWorkRequestBuilder<StoragesUpdateWorker>()
                .addTag(tag)
                .build()
            WorkManager.getInstance(ctx).enqueue(task)
        }
    }
}
