package com.san.kir.background.works

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.san.kir.core.support.MainMenuType
import com.san.kir.data.db.dao.MainMenuDao
import com.san.kir.data.models.base.MainMenuItem
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class UpdateMainMenuWorker @AssistedInject constructor(
    @Assisted private val ctx: Context,
    @Assisted params: WorkerParameters,
    private val mainMenuDao: MainMenuDao,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        kotlin.runCatching {
            checkNewItems()
            updateMenuItems()
            checkItemsForRemove()
        }.onFailure { return Result.retry() }
        return Result.success()
    }

    private suspend fun checkNewItems() {
        // Добавление новых
        val items = mainMenuDao.getItems()
        MainMenuType.values()
            .filter { it.added }
            .filter { type ->
                items.none { it.type == type }
            }
            .forEach {
                mainMenuDao.insert(
                    MainMenuItem(name = ctx.getString(it.stringId()), order = 100, type = it)
                )
            }
    }

    private suspend fun updateMenuItems() {
        // Обновление старых
        mainMenuDao.update(*mainMenuDao
            .getItems()
            .map { item ->
                item.copy(name = ctx.getString(item.type.stringId()))
            }
            .toTypedArray())


    }

    private suspend fun checkItemsForRemove() {
        // Удаление не нужных
        val notNeeded = MainMenuType.values().filter { it.added.not() }
        if (notNeeded.isNotEmpty())
            mainMenuDao.getItems().filter { type ->
                notNeeded.any { it == type.type }
            }.forEach {
                mainMenuDao.delete(it)
            }
    }

    companion object {
        const val tag = "updateMainMenu"

        fun addTask(ctx: Context): Operation {
            val task = OneTimeWorkRequestBuilder<UpdateMainMenuWorker>()
                .addTag(tag)
                .build()
            return WorkManager.getInstance(ctx).enqueueUniqueWork(
                tag + "Unique",
                ExistingWorkPolicy.KEEP,
                task
            )
        }
    }
}
