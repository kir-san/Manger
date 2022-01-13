package com.san.kir.manger.foreground_work.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.san.kir.core.support.CATEGORY_ALL
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.base.Category
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class UpdateCategoryInMangaWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val categoryDao: CategoryDao,
    private val mangaDao: MangaDao,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val categoryName = inputData.getString(cat)
        val oldCategory = inputData.getString(oldCat)


        if (categoryName != null && oldCategory != null) {

            val category = com.san.kir.core.utils.coroutines.withDefaultContext {
                categoryDao.loadItem(categoryName).first()
            }

            kotlin.runCatching {
                com.san.kir.core.utils.coroutines.withDefaultContext {
                    if (categoryName != oldCategory) {
                        mangaDao.update(
                            *(
                                    if (oldCategory == applicationContext.CATEGORY_ALL)
                                        mangaDao.getItems()
                                    else
                                        mangaDao.itemsWhereCategoryNotAll(oldCategory)
                                    )
                                .onEach { it.category = category.name }
                                .toTypedArray()
                        )
                    }
                }
            }.fold(
                onSuccess = {
                    return Result.success()
                },
                onFailure = {
                    category.name = oldCategory
                    categoryDao.update(category)
                    it.printStackTrace()
                    return Result.failure()
                }
            )
        } else {
            return Result.retry()
        }
    }

    companion object {
        const val tag = "updateCategoryInManga"
        const val cat = "category"
        const val oldCat = "oldCata"

        fun addTask(ctx: Context, category: Category, oldCategory: String) {
            val data = workDataOf(cat to category.name, oldCat to oldCategory)
            val task = OneTimeWorkRequestBuilder<UpdateCategoryInMangaWorker>()
                .addTag(tag)
                .setInputData(data)
                .build()
            WorkManager.getInstance(ctx).enqueue(task)
        }
    }
}
