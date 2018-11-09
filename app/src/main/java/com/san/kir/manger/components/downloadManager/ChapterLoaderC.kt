package com.san.kir.manger.components.downloadManager

import android.content.Context
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

class ChapterLoaderC(context: Context) {
    private val uiJob = JobContext(Executors.newSingleThreadExecutor())
    private val job = JobContext(Executors.newSingleThreadExecutor())
    private val listeners = ListenerProvider()
    private val networkManager = NetworkManager(context)
    private val downloadManager = DownloadManager(1)
    private val iteratorProcessor = IteratorProcessorC(job, downloadManager, networkManager)
    private val dbManager = Main.db.downloadDao

    init {
        downloadManager.delegate = DownloadManagerDelegateImplC(
            uiJob,
            job,
            listeners.mainListener,
            iteratorProcessor
        )
    }

    fun add(task: DownloadItem) {
        job.post {
            task.order = System.currentTimeMillis()
            task.status = DownloadStatus.queued
            dbManager.insert(task)

            startIteratorProcessor()

            uiJob.post {
                listeners.mainListener.onQueued(task)
            }

            if (!networkManager.isAvailable()) {
                pause(task)
            }
        }
    }

    fun addOrStart(task: DownloadItem) {
        if (hasTask(task)) {
            start(task)
        } else {
            add(task)
        }
    }

    fun pause(task: DownloadItem) {
        job.post {
            task.status = DownloadStatus.queued
            dbManager.update(task)

            if (isDownloading(task.id)) {
                cancelDownload(task.id)
            }
            if (canPauseDownload(task)) {
                task.status = DownloadStatus.pause
            }
            dbManager.update(task)

            uiJob.post {
                listeners.mainListener.onPaused(task)
            }
        }
    }

    fun start(task: DownloadItem) {
        job.post {
            if (isDownloading(task.id)) {
                cancelDownload(task.id)
            }
            if (!isDownloading(task.id) && canResumeDownload(task)) {
                task.order = System.currentTimeMillis()
                task.status = DownloadStatus.queued
            }

            dbManager.update(task)

            startIteratorProcessor()

            uiJob.post {
                listeners.mainListener.onQueued(task)
            }

            if (!networkManager.isAvailable()) {
                pause(task)
            }
        }
    }

    fun retry(task: DownloadItem) {
        job.post {
            if (canRetryDownload(task)) {
                task.order = System.currentTimeMillis()
                task.status = DownloadStatus.queued
            }

            dbManager.update(task)

            startIteratorProcessor()

            uiJob.post {
                listeners.mainListener.onQueued(task)
            }
        }
    }

    fun pauseAll() {
        job.post {
            val loading = dbManager.loadItems().filter { canPauseDownload(it) }
            if (loading.isNotEmpty()) {
                loading.forEach {
                    it.status = DownloadStatus.queued
                    dbManager.update(it)
                }
            }

            downloadManager.cancelAll()
            iteratorProcessor.stop()

            val downloads = dbManager.loadItems().filter { canPauseDownload(it) }
            if (downloads.isNotEmpty()) {
                downloads.forEach {
                    it.status = DownloadStatus.pause
                }
                dbManager.update(*downloads.toTypedArray())

                uiJob.post {
                    downloads.forEach {
                        listeners.mainListener.onPaused(it)
                    }
                }
            }
        }
    }

    fun startAll() {
        job.post {
            val downloads =
                dbManager.loadItems()
                    .filter { canResumeDownload(it) }
            if (downloads.isNotEmpty()) {
                downloads.forEach {
                    it.status = DownloadStatus.queued
                }
                dbManager.update(*downloads.toTypedArray())
            }

            iteratorProcessor.start()

            uiJob.post {
                downloads.forEach {
                    listeners.mainListener.onQueued(it)
                }
            }
        }
    }

    fun retryAll() {
        job.post {
            val downloads =
                dbManager.loadItems()
                    .filter { canRetryDownload(it) }
            if (downloads.isNotEmpty()) {
                downloads.forEach {
                    it.status = DownloadStatus.queued
                }
                dbManager.update(*downloads.toTypedArray())
            }

            iteratorProcessor.start()

            uiJob.post {
                downloads.forEach {
                    listeners.mainListener.onQueued(it)
                }
            }
        }
    }

    fun stop() = runBlocking {
        listeners.clear()
        iteratorProcessor.stop()
        downloadManager.close()
        job.close()
        uiJob.close()
    }

    fun <T : Any> addListener(tag: T, listener: DownloadListener) {
        listeners.addListener(tag, listener)
    }

    fun <T : Any> removeListeners(tag: T) {
        listeners.removeListeners(tag)
    }

    fun hasTask(task: DownloadItem): Boolean {
        val containedItem = dbManager.loadItem(task.link)
        return containedItem != null
    }

    fun setConcurrentPages(concurrent: Int) {
        downloadManager.changeConcurrentPages(concurrent)
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

    private fun isDownloading(id: Long): Boolean {
        return downloadManager.contains(id)
    }

    private fun cancelDownload(id: Long): Boolean {
        return downloadManager.cancel(id)
    }

    private fun canPauseDownload(task: DownloadItem): Boolean {
        return task.status == DownloadStatus.loading || task.status == DownloadStatus.queued
    }

    private fun canResumeDownload(task: DownloadItem): Boolean {
        return task.status == DownloadStatus.pause
    }

    private fun canRetryDownload(task: DownloadItem): Boolean {
        return task.status == DownloadStatus.error
    }

    private suspend fun startIteratorProcessor() {
        if (iteratorProcessor.isStopped) {
            iteratorProcessor.start()
        }
    }
}
