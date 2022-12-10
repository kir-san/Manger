package com.san.kir.background.logic

import android.app.Application
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.san.kir.background.logic.repo.WorkersRepository
import com.san.kir.background.works.UpdateCatalogWorker
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.models.base.CatalogTask
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateCatalogManager @Inject constructor(
    private val context: Application,
    private val workersRepository: WorkersRepository,
) {
    private val manager by lazy { WorkManager.getInstance(context) }

    suspend fun addTask(name: String) = withIoContext {
        if (workersRepository.item(name) == null)
            workersRepository.add(CatalogTask(name = name))

        startWorker()
    }

    private fun startWorker() =
        manager.enqueueUniqueWork(unique, ExistingWorkPolicy.KEEP, task)

    suspend fun removeTask(name: String) = withIoContext {
        workersRepository.item(name)?.let {
            workersRepository.remove(it)
        }
    }

    fun loadTasks() = workersRepository.catalog
    fun loadTask(name: String) = workersRepository.loadItem(name)

    companion object {
        private val taskId by lazy { UUID.randomUUID() }
        private val unique = "${UpdateCatalogWorker::class.simpleName}UniqueName"
        private val task by lazy {
            OneTimeWorkRequestBuilder<UpdateCatalogWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setId(taskId)
                .build()
        }
    }
}
