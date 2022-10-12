package com.san.kir.core.download

import com.san.kir.core.support.DownloadState
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.models.base.Chapter
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ChapterLoader @Inject constructor(
    private val job: JobContext,
    private val networkManager: NetworkManager,
    private val downloadManager: DownloadManager,
    private val iteratorProcessor: IteratorProcessor,
    private val chapterDao: ChapterDao,
    private val listeners: ListenerProvider,
    delegateImpl: DownloadManagerDelegateImpl,
) {
    init {
        downloadManager.delegate = delegateImpl
    }

    fun pause(task: Chapter) {
        job.post {

            var task = task.copy(status = DownloadState.QUEUED)
            chapterDao.update(task)

            if (isDownloading(task.id)) {
                cancelDownload(task.id)
            }
            if (canPauseDownload(task)) {
                task = task.copy(status = DownloadState.PAUSED)
                chapterDao.update(task)
            }

            listeners.mainListener.onPaused(task)
        }
    }

    fun start(task: Chapter) {
        job.post {
            if (isDownloading(task.id)) {
                cancelDownload(task.id)
            }

            var task = task.copy(
                order = System.currentTimeMillis(),
                status = DownloadState.QUEUED,
                isError = false
            )

            chapterDao.update(task)

            startIteratorProcessor()

            listeners.mainListener.onQueued(task)

            if (!networkManager.isAvailable()) {
                pause(task)
            }
        }
    }

    fun pauseAll() {
        job.post {
            val loading = chapterDao.items().filter { canPauseDownload(it) }
            if (loading.isNotEmpty()) {
                chapterDao.update(
                    loading.map { it.copy(status = DownloadState.QUEUED) }
                )
            }

            downloadManager.cancelAll()
            iteratorProcessor.stop()

            val downloads = chapterDao.items().filter { canPauseDownload(it) }
            if (downloads.isNotEmpty()) {
                val temp = downloads.map {
                    it.copy(status = DownloadState.PAUSED)
                }
                chapterDao.update(*temp.toTypedArray())

                temp.forEach {
                    listeners.mainListener.onPaused(it)
                }
            }
        }
    }

    fun startAll() {
        job.post {
            val downloads = chapterDao.items().filter { canResumeDownload(it) }
            if (downloads.isNotEmpty()) {
                chapterDao.update(*downloads.map {
                    it.copy(
                        status = DownloadState.QUEUED,
                        isError = false
                    )
                }.toTypedArray())
            }

            iteratorProcessor.start()

            downloads.forEach {
                listeners.mainListener.onQueued(it)
            }
        }
    }

    @Suppress("unused")
    fun stop() = runBlocking {
        listeners.clear()
        iteratorProcessor.stop()
        downloadManager.close()
        job.close()
    }

    fun <T : Any> addListener(tag: T, listener: DownloadListener) {
        listeners.addListener(tag, listener)
    }

    fun clearListeners() {
        listeners.clear()
    }

    fun setConcurrentPages(concurrent: Int) {
        job.post {
            downloadManager.changeConcurrentPages(concurrent)
        }
    }

    fun setRetryOnError(isRetry: Boolean) = runBlocking {
        iteratorProcessor.setRetry(isRetry)
    }

    fun isWifiOnly(isWifi: Boolean) {
        val oldValue = networkManager.isWifi
        networkManager.isWifi = isWifi
        if (oldValue != isWifi && isWifi && !networkManager.isAvailable()) {
            pauseAll()
        }

    }

    private suspend fun isDownloading(id: Long): Boolean {
        return downloadManager.contains(id)
    }

    private suspend fun cancelDownload(id: Long): Boolean {
        return downloadManager.cancel(id)
    }

    private fun canPauseDownload(task: Chapter): Boolean {
        return task.status == DownloadState.LOADING || task.status == DownloadState.QUEUED
    }

    private fun canResumeDownload(task: Chapter): Boolean {
        return task.status == DownloadState.PAUSED
    }

    private suspend fun startIteratorProcessor() {
        if (iteratorProcessor.isStopped) {
            iteratorProcessor.start()
        }
    }
}
