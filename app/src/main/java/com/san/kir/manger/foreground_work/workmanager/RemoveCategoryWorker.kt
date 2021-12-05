package com.san.kir.manger.foreground_work.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.san.kir.manger.data.room.entities.Category
import com.san.kir.data.db.getDatabase
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.core.utils.coroutines.withDefaultContext
import kotlinx.coroutines.coroutineScope

class RemoveCategoryWorker(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {

    private val categoryDao = com.san.kir.data.db.getDatabase(appContext).categoryDao
    private val mangaDao = com.san.kir.data.db.getDatabase(appContext).mangaDao

    override suspend fun doWork() = coroutineScope {
        val categoryName = inputData.getString(cat)

        if (categoryName != null) {
            val category = com.san.kir.core.utils.coroutines.withDefaultContext {
                categoryDao.loadItem(categoryName).first()
            }
            kotlin.runCatching {
                com.san.kir.core.utils.coroutines.withDefaultContext {
                    mangaDao.update(
                        *mangaDao
                            .itemsWhereCategoryNotAll(category.name)
                            .onEach {
                                it.categories = CATEGORY_ALL
                            }
                            .toTypedArray()
                    )
                    categoryDao.delete(category)
                }
            }.fold(
                onSuccess = {
                    Result.success()
                },
                onFailure = {
                    it.printStackTrace()
                    Result.failure()
                }
            )
        } else {
            Result.retry()
        }
    }

    companion object {
        const val tag = "removeCategory"
        const val cat = "category"

        fun addTask(ctx: Context, category: Category) {
            val data = workDataOf(cat to category.name)
            val task = OneTimeWorkRequestBuilder<RemoveCategoryWorker>()
                .addTag(tag)
                .setInputData(data)
                .build()
            WorkManager.getInstance(ctx).enqueue(task)
        }
    }
}
