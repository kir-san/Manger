package com.san.kir.manger.components.downloadManager

import android.os.Handler
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus

class DownloadManagerDelegateImpl(
    private val uiHandler: Handler,
    private val handler: Handler,
    private val listener: DownloadListener,
    private val iteratorProcessor: IteratorProcessor
) : DownloadManager.Delegate {
    private val dbManager = Main.db.downloadDao

    override fun onDownloadRemovedFromManager(item: DownloadItem) {
        try {
            handler.post {
                if (iteratorProcessor.isStopped) {
                    iteratorProcessor.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStarted(item: DownloadItem) {
        try {
            item.status = DownloadStatus.loading
            dbManager.update(item)
            uiHandler.post {
                listener.onProgress(item)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onProgress(item: DownloadItem) {
        try {
            dbManager.update(item)
            uiHandler.post {
                listener.onProgress(item)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onError(item: DownloadItem, cause: Throwable?) {
        try {
            item.status = DownloadStatus.error
            dbManager.update(item)
            uiHandler.post {
                listener.onError(item, cause)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onComplete(item: DownloadItem) {
        try {
            item.status = DownloadStatus.completed
            dbManager.update(item)

            val stat = Main.db.statisticDao.loadItem(item.manga)
            stat.downloadSize += item.totalSize
            stat.downloadTime += item.totalTime
            Main.db.statisticDao.update(stat)

            uiHandler.post {
                listener.onCompleted(item)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
