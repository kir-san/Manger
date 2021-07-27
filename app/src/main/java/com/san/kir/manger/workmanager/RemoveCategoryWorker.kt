package com.san.kir.manger.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.utils.CATEGORY_ALL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class RemoveCategoryWorker(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {

    private val categoryDao = getDatabase(appContext).categoryDao
    private val mangaDao = getDatabase(appContext).mangaDao

    override suspend fun doWork() = coroutineScope {
        val categoryName = inputData.getString(cat)

        if (categoryName != null) {
            val category = withContext(Dispatchers.Default) {
                categoryDao.loadItem(categoryName).first()
            }
            kotlin.runCatching {
                withContext(Dispatchers.Default) {
                    mangaDao.update(
                        *mangaDao
                            .loadMangaWhereCategoryNotAll(category.name)
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