package com.san.kir.manger.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.san.kir.manger.room.dao.CategoryDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.coroutines.withDefaultContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

@HiltWorker
class UpdateCategoryInMangaWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val categoryDao: CategoryDao,
    private val mangaDao: MangaDao,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork() = coroutineScope {
        val categoryName = inputData.getString(cat)
        val oldCategory = inputData.getString(oldCat)


        if (categoryName != null && oldCategory != null) {

            val category = withDefaultContext {
                categoryDao.loadItem(categoryName).first()
            }

            kotlin.runCatching {
                withDefaultContext {
                    if (categoryName != oldCategory) {
                        mangaDao.update(
                            *(
                                    if (oldCategory == CATEGORY_ALL)
                                        mangaDao.getItems()
                                    else
                                        mangaDao.getMangaWhereCategoryNotAll(oldCategory)
                                    )
                                .onEach { it.categories = category.name }
                                .toTypedArray()
                        )
                    }
                }
            }.fold(
                onSuccess = {
                    Result.success()
                },
                onFailure = {
                    category.name = oldCategory
                    categoryDao.update(category)
                    it.printStackTrace()
                    Result.failure()
                }
            )
        } else {
            Result.retry()
        }
    }

    companion object {
        const val tag = "updateCategoryInManga"
        const val cat = "category"
        const val oldCat = "oldCata"

        fun addTask(ctx: Context, category: Category, oldCategory: String) {
            val data = workDataOf(cat to category.name, oldCat to oldCategory)
            val task = OneTimeWorkRequestBuilder<UpdateCategoryInMangaWorker>()
                .addTag(LatestClearWorker.tag)
                .setInputData(data)
                .build()
            WorkManager.getInstance(ctx).enqueue(task)
        }
    }
}
