package com.san.kir.background.works

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.base.Category
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/*
    Worker для удаления категории,
    У всей манги, которая была свазанна с удаляемой категорией,
    применяется категория по умолчанию
*/
@HiltWorker
class RemoveCategoryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val categoryDao: CategoryDao,
    private val mangaDao: MangaDao,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val categoryId = inputData.getLong(cat, -1L)

        if (categoryId != -1L) {
            // Получение удаляемой категории
            val category = withDefaultContext {
                categoryDao.itemById(categoryId)
            }

            // Получение категории "Все"
            val categoryAll = withDefaultContext {
                categoryDao.defaultCategory(applicationContext)
            }

            kotlin.runCatching {
                withDefaultContext {
                    mangaDao.update(
                        mangaDao
                            // Получение всей манги, которая связана с удаляемой категорией
                            .itemsByCategoryId(category.id)
                            .onEach {
                                // Замена на категорию "Все"
                                it.categoryId = categoryAll.id
                            }
                    )
                    categoryDao.delete(category)
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
        } else {
            return Result.retry()
        }
    }

    companion object {
        const val tag = "removeCategory"
        const val cat = "category_id"

        fun addTask(ctx: Context, category: Category) {
            val data = workDataOf(cat to category.id)
            val task = OneTimeWorkRequestBuilder<RemoveCategoryWorker>()
                .addTag(tag)
                .setInputData(data)
                .build()
            WorkManager.getInstance(ctx).enqueue(task)
        }
    }
}
