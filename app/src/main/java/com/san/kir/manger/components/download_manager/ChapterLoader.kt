package com.san.kir.manger.components.download_manager

import com.san.kir.manger.room.dao.DownloadDao
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.utils.JobContext
import com.san.kir.manger.utils.NetworkManager
import com.san.kir.manger.utils.enums.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChapterLoader @Inject constructor(
    private val job: JobContext,
    private val networkManager: NetworkManager,
    private val downloadManager: DownloadManager,
    private val iteratorProcessor: IteratorProcessor,
    private val downloadDao: DownloadDao,
    private val listeners: ListenerProvider,
    delegateImpl: DownloadManagerDelegateImpl,
) {
    init {
        downloadManager.delegate = delegateImpl
    }

    fun add(task: DownloadItem) {
        job.post {
            task.order = System.currentTimeMillis()
            task.status = DownloadStatus.queued
            task.isError = false

            downloadDao.insert(task)

            startIteratorProcessor()

            listeners.mainListener.onQueued(task)

            if (!networkManager.isAvailable()) {
                pause(task)
            }
        }
    }

    fun addOrStart(task: DownloadItem) {
        job.post {
            if (hasTask(task)) {
                start(task)
            } else {
                add(task)
            }
        }
    }

    fun pause(task: DownloadItem) {
        job.post {
            val lTask =
                if (task.id != 0L) task
                else downloadDao.getItem(task.link)


            if (lTask == null) {
                listeners.mainListener.onError(task, null)
                return@post
            }

            lTask.status = DownloadStatus.queued
            downloadDao.update(lTask)

            if (isDownloading(lTask.id)) {
                cancelDownload(lTask.id)
            }
            if (canPauseDownload(lTask)) {
                lTask.status = DownloadStatus.pause
            }
            downloadDao.update(lTask)

            listeners.mainListener.onPaused(lTask)
        }
    }

    fun start(task: DownloadItem) {
        job.post {
            val lTask =
                if (task.id != 0L) task
                else downloadDao.getItem(task.link)


            if (lTask == null) {
                listeners.mainListener.onError(task, null)
                return@post
            }

            if (isDownloading(lTask.id)) {
                cancelDownload(lTask.id)
            }

            lTask.order = System.currentTimeMillis()
            lTask.status = DownloadStatus.queued
            lTask.isError = false

            downloadDao.update(lTask)

            startIteratorProcessor()

            listeners.mainListener.onQueued(lTask)

            if (!networkManager.isAvailable()) {
                pause(lTask)
            }
        }
    }

    fun pauseAll() {
        job.post {
            val loading = downloadDao.getItems().filter { canPauseDownload(it) }
            if (loading.isNotEmpty()) {
                loading.forEach {
                    it.status = DownloadStatus.queued
                    downloadDao.update(it)
                }
            }

            downloadManager.cancelAll()
            iteratorProcessor.stop()

            val downloads = downloadDao.getItems().filter { canPauseDownload(it) }
            if (downloads.isNotEmpty()) {
                downloads.forEach {
                    it.status = DownloadStatus.pause
                }
                downloadDao.update(*downloads.toTypedArray())

                downloads.forEach {
                    listeners.mainListener.onPaused(it)
                }
            }
        }
    }

    fun startAll() {
        job.post {
            val downloads =
                downloadDao.getItems()
                    .filter { canResumeDownload(it) }
            if (downloads.isNotEmpty()) {
                downloads.forEach {
                    it.status = DownloadStatus.queued
                    it.isError = false
                }
                downloadDao.update(*downloads.toTypedArray())
            }

            iteratorProcessor.start()

            downloads.forEach {
                listeners.mainListener.onQueued(it)
            }
        }
    }

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

    suspend fun hasTask(task: DownloadItem): Boolean {
        val containedItem = withContext(job.coroutineContext + Dispatchers.Default) {
            downloadDao.getItem(
                task.link
            )
        }
        return containedItem != null
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

    private fun canPauseDownload(task: DownloadItem): Boolean {
        return task.status == DownloadStatus.loading || task.status == DownloadStatus.queued
    }

    private fun canResumeDownload(task: DownloadItem): Boolean {
        return task.status == DownloadStatus.pause
    }

    private suspend fun startIteratorProcessor() {
        if (iteratorProcessor.isStopped) {
            iteratorProcessor.start()
        }
    }
}
