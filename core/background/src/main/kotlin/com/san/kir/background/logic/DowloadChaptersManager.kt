package com.san.kir.background.logic

import android.app.Application
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.san.kir.background.logic.repo.ChapterRepository
import com.san.kir.background.logic.repo.ChapterWorkerRepository
import com.san.kir.background.works.DownloadChaptersWorker
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.models.base.ChapterTask
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadChaptersManager @Inject constructor(
    private val context: Application,
    private val workerRepository: ChapterWorkerRepository,
    private val chapterRepository: ChapterRepository,
) {
    private val manager by lazy { WorkManager.getInstance(context) }

    suspend fun addTask(chapterId: Long) = withIoContext {
        if (workerRepository.task(chapterId) == null) {
            workerRepository.add(ChapterTask(chapterId = chapterId))
            chapterRepository.addToQueue(chapterId)
        }

        startWorker()
    }

    suspend fun addTasks(chapterIds: List<Long>) = withIoContext {
        chapterIds.forEach { chapterId ->
            if (workerRepository.task(chapterId) == null) {
                workerRepository.add(ChapterTask(chapterId = chapterId))
                chapterRepository.addToQueue(chapterId)
            }
        }

        startWorker()
    }

    suspend fun removeTask(chapterId: Long) {
        workerRepository.task(chapterId)?.let {
            workerRepository.remove(it)
        }
        chapterRepository.pauseChapters(listOf(chapterId))
    }

    suspend fun addPausedTasks() {
        addTasks(chapterRepository.pausedChapters().map { it.id })
    }

    suspend fun removeAllTasks() {
        val currentTasks = workerRepository.catalog.first()
        workerRepository.remove(currentTasks)
        chapterRepository.pauseChapters(currentTasks.map { it.chapterId })
    }

    private fun startWorker() =
        manager.enqueueUniqueWork(unique, ExistingWorkPolicy.KEEP, task)

    companion object {
        private val taskId by lazy { UUID.randomUUID() }
        private val unique = "${DownloadChaptersWorker::class.simpleName}UniqueName"
        private val task by lazy {
            OneTimeWorkRequestBuilder<DownloadChaptersWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setId(taskId)
                .build()
        }
    }
}
