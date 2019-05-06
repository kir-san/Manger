package com.san.kir.manger.components.download_manager

import android.content.Context
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus
import com.san.kir.manger.utils.JobContext
import com.san.kir.manger.utils.NetworkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

class ChapterLoader(context: Context) {
    private val uiJob = JobContext(Executors.newSingleThreadExecutor())
    private val job = JobContext(Executors.newSingleThreadExecutor())
    private val listeners = ListenerProvider()
    private val networkManager = NetworkManager(context)
    private val downloadManager = DownloadManager(1)
    private val mDbManager = getDatabase(context)
    private val iteratorProcessor =
        IteratorProcessor(job, downloadManager, networkManager, mDbManager)
    private val mDownloadDao = mDbManager.downloadDao

    init {
        downloadManager.delegate = DownloadManagerDelegateImpl(
            uiJob,
            job,
            listeners.mainListener,
            iteratorProcessor,
            mDbManager
        )
    }

    fun add(task: DownloadItem) {
        job.post {
            task.order = System.currentTimeMillis()
            task.status = DownloadStatus.queued
            task.isError = false

            mDownloadDao.insert(task)

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
                else mDownloadDao.getItem(task.link)


            if (lTask == null) {
                uiJob.post {
                    listeners.mainListener.onError(task, null)
                }
                return@post
            }

            lTask.status = DownloadStatus.queued
            mDownloadDao.update(lTask)

            if (isDownloading(lTask.id)) {
                cancelDownload(lTask.id)
            }
            if (canPauseDownload(lTask)) {
                lTask.status = DownloadStatus.pause
            }
            mDownloadDao.update(lTask)

            uiJob.post {
                listeners.mainListener.onPaused(lTask)
            }
        }
    }

    fun start(task: DownloadItem) {
        job.post {
            val lTask =
                if (task.id != 0L) task
                else mDownloadDao.getItem(task.link)


            if (lTask == null) {
                uiJob.post {
                    listeners.mainListener.onError(task, null)
                }
                return@post
            }

            if (isDownloading(lTask.id)) {
                cancelDownload(lTask.id)
            }

            lTask.order = System.currentTimeMillis()
            lTask.status = DownloadStatus.queued
            lTask.isError = false

            mDownloadDao.update(lTask)

            startIteratorProcessor()

            uiJob.post {
                listeners.mainListener.onQueued(lTask)
            }

            if (!networkManager.isAvailable()) {
                pause(lTask)
            }
        }
    }

    fun pauseAll() {
        job.post {
            val loading = mDownloadDao.getItems().filter { canPauseDownload(it) }
            if (loading.isNotEmpty()) {
                loading.forEach {
                    it.status = DownloadStatus.queued
                    mDownloadDao.update(it)
                }
            }

            downloadManager.cancelAll()
            iteratorProcessor.stop()

            val downloads = mDownloadDao.getItems().filter { canPauseDownload(it) }
            if (downloads.isNotEmpty()) {
                downloads.forEach {
                    it.status = DownloadStatus.pause
                }
                mDownloadDao.update(*downloads.toTypedArray())

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
                mDownloadDao.getItems()
                    .filter { canResumeDownload(it) }
            if (downloads.isNotEmpty()) {
                downloads.forEach {
                    it.status = DownloadStatus.queued
                    it.isError = false
                }
                mDownloadDao.update(*downloads.toTypedArray())
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

    suspend fun hasTask(task: DownloadItem): Boolean {
        val containedItem = withContext(job.coroutineContext + Dispatchers.Default) {
            mDownloadDao.getItem(
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

    private fun canCompleteDownload(task: DownloadItem): Boolean {
        return task.status == DownloadStatus.completed
    }

    private suspend fun startIteratorProcessor() {
        if (iteratorProcessor.isStopped) {
            iteratorProcessor.start()
        }
    }
}
