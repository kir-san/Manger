package com.san.kir.manger.components.downloadManager

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus

class ChapterLoader(context: Context) {
    private val lock = Object()

    private val uiHandler = Handler(Looper.getMainLooper())
    private val handler by lazy {
        val handlerThread = HandlerThread("chapter downloader")
        handlerThread.start()
        Handler(handlerThread.looper)
    }

    private val listeners = ListenerProvider()
    private val networkManager = NetworkManager(context)
    private val downloadManager = DownloadManager(1)
    private val iteratorProcessor = IteratorProcessor(handler, downloadManager, networkManager)
    private val dbManager = Main.db.downloadDao

    init {
        downloadManager.delegate = DownloadManagerDelegateImpl(
            uiHandler,
            handler,
            listeners.mainListener,
            iteratorProcessor
        )
    }


    fun add(task: DownloadItem) {
        synchronized(lock) {
            handler.post {
                try {
                    task.order = System.currentTimeMillis()
                    task.status = DownloadStatus.queued
                    dbManager.insert(task)

                    startIteratorProcessor()

                    uiHandler.post {
                        listeners.mainListener.onQueued(task)
                    }

                    if (!networkManager.isAvailable()) {
                        pause(task)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
        synchronized(lock) {
            handler.post {
                try {
                    task.status = DownloadStatus.queued
                    dbManager.update(task)

                    if (isDownloading(task.id)) {
                        cancelDownload(task.id)
                    }
                    if (canPauseDownload(task)) {
                        task.status = DownloadStatus.pause
                    }
                    dbManager.update(task)

                    uiHandler.post {
                        listeners.mainListener.onPaused(task)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun start(task: DownloadItem) {
        synchronized(lock) {
            handler.post {
                try {
                    if (isDownloading(task.id)) {
                        cancelDownload(task.id)
                    }
                    if (!isDownloading(task.id) && canResumeDownload(task)) {
                        task.order = System.currentTimeMillis()
                        task.status = DownloadStatus.queued
                    }

                    dbManager.update(task)

                    startIteratorProcessor()

                    uiHandler.post {
                        listeners.mainListener.onQueued(task)
                    }

                    if (!networkManager.isAvailable()) {
                        pause(task)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun retry(task: DownloadItem) {
        synchronized(lock) {
            handler.post {
                try {
                    if (canRetryDownload(task)) {
                        task.order = System.currentTimeMillis()
                        task.status = DownloadStatus.queued
                    }

                    dbManager.update(task)

                    startIteratorProcessor()

                    uiHandler.post {
                        listeners.mainListener.onQueued(task)
                    }
                } catch (e: Exception) {

                }
            }
        }
    }

    fun pauseAll() {
        synchronized(lock) {
            handler.post {
                try {
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

                        uiHandler.post {
                            downloads.forEach {
                                listeners.mainListener.onPaused(it)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun startAll() {
        synchronized(lock) {
            handler.post {
                try {
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

                    uiHandler.post {
                        downloads.forEach {
                            listeners.mainListener.onQueued(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun retryAll() {
        synchronized(lock) {
            handler.post {
                try {
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

                    uiHandler.post {
                        downloads.forEach {
                            listeners.mainListener.onQueued(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun stop() {
        listeners.clear()
        iteratorProcessor.stop()
        downloadManager.close()
        handler.looper.quitSafely()
    }

    fun <T : Any> addListener(tag: T, listener: DownloadListener) {
        synchronized(lock) {
            listeners.addListener(tag, listener)
        }
    }

    fun <T : Any> removeListeners(tag: T) {
        synchronized(lock) {
            listeners.removeListeners(tag)
        }
    }

    fun hasTask(task: DownloadItem): Boolean {
        synchronized(lock) {
            val containedItem = dbManager.loadItem(task.link)
            return containedItem != null
        }
    }

    fun setConcurrentPages(concurrent: Int) {
        synchronized(lock) {
            downloadManager.changeConcurrentPages(concurrent)
        }
    }

    fun setRetryOnError(isRetry: Boolean) {
        synchronized(lock) {
            iteratorProcessor.setRetry(isRetry)
        }
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

    private fun startIteratorProcessor() {
        if (iteratorProcessor.isStopped) {
            iteratorProcessor.start()
        }
    }


}
