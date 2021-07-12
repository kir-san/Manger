package com.san.kir.manger.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.san.kir.manger.repositories.CategoryRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.utils.extensions.log
import com.san.kir.manger.utils.extensions.logVar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class UpdateCategoryInMangaWorker(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {

    private val mCategoryRepository = CategoryRepository(appContext)
    private val mMangaRepository = MangaRepository(appContext)

    override suspend fun doWork() = coroutineScope {
        val categoryName = inputData.getString(cat)
        val oldCategory = inputData.getString(oldCat)



        if (categoryName != null && oldCategory != null) {
            val category = withContext(Dispatchers.Default) {
                mCategoryRepository.loadItem(categoryName).first()
            }
            log("categoryName = ${category.name}")
            oldCategory.logVar("oldCategory")
            kotlin.runCatching {
                withContext(Dispatchers.Default) {
                    if (categoryName != oldCategory) {
                        mMangaRepository.update(
                            *mMangaRepository
                                .getItemsWhere(oldCategory)
                                .onEach {
                                    it.categories = categoryName
                                }
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
                    mCategoryRepository.update(category)
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
            log("task is started")
        }
    }
}
