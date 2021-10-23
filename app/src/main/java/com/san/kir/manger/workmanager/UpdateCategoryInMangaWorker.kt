package com.san.kir.manger.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.san.kir.manger.di.DefaultDispatcher
import com.san.kir.manger.room.dao.CategoryDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.utils.CATEGORY_ALL
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@HiltWorker
class UpdateCategoryInMangaWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    @DefaultDispatcher private val default: CoroutineDispatcher,
    private val categoryDao: CategoryDao,
    private val mangaDao: MangaDao,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork() = coroutineScope {
        val categoryName = inputData.getString(cat)
        val oldCategory = inputData.getString(oldCat)


        if (categoryName != null && oldCategory != null) {

            val category = withContext(default) {
                categoryDao.loadItem(categoryName).first()
            }

            kotlin.runCatching {
                withContext(default) {
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
