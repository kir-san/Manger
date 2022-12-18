package com.san.kir.background.logic

import android.app.Application
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.san.kir.background.logic.repo.MangaWorkerRepository
import com.san.kir.background.works.UpdateMangaWorker
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.models.base.MangaTask
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateMangaManager @Inject constructor(
    private val context: Application,
    private val workerRepository: MangaWorkerRepository,
) {
    private val manager by lazy { WorkManager.getInstance(context) }

    suspend fun addTask(mangaId: Long) = withIoContext {
        if (workerRepository.task(mangaId) == null)
            workerRepository.add(MangaTask(mangaId = mangaId))

        startWorker()
    }

    suspend fun addTasks(mangaIds: List<Long>) = withIoContext {
        mangaIds.forEach {
            if (workerRepository.task(it) == null)
                workerRepository.add(MangaTask(mangaId = it))
        }

        startWorker()
    }

    private fun startWorker() =
        manager.enqueueUniqueWork(unique, ExistingWorkPolicy.KEEP, task)

    suspend fun removeTask(mangaId: Long) = withIoContext {
        workerRepository.task(mangaId)?.let {
            workerRepository.remove(it)
        }
    }

    fun loadTasks() = workerRepository.catalog
    fun loadTask(mangaId: Long) = workerRepository.loadTask(mangaId)

    companion object {
        private val taskId by lazy { UUID.randomUUID() }
        private val unique = "${UpdateMangaWorker::class.simpleName}UniqueName"
        private val task by lazy {
            OneTimeWorkRequestBuilder<UpdateMangaWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setId(taskId)
                .build()
        }
    }
}
