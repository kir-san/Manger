package com.san.kir.manger.components.DownloadManager

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.san.kir.manger.R
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.bytesToMbytes
import com.san.kir.manger.utils.formatDouble
import com.san.kir.manger.utils.log
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.notificationManager


class DownloadService : Service(), DownloadListener {
    companion object {
        private const val ACTION_PAUSE_ALL = "kir.san.manger.DownloadService.PAUSE_ALL"
        private const val channelId = "DownloadServiceId"
    }

    private val downloadManager = ChapterLoader().also {
        it.addListener(this, this)
    }

    private val notificationId = ID.generate()

    private val actionGoToDownloads by lazy {
        val intent = intentFor<DownloadManagerActivity>()
        PendingIntent.getActivity(this, 0, intent, 0)
    }
    private val actionPauseAll by lazy {
        val intent = intentFor<DownloadService>().setAction(ACTION_PAUSE_ALL)
        val pauseAll = PendingIntent.getService(this, 0, intent, 0)
        NotificationCompat
            .Action
            .Builder(
                R.drawable.ic_stop_white,
                getString(R.string.download_service_pause_all),
                pauseAll
            )
            .build()
    }

    private var totalCount = 0
        set(value) {
            field = if (value < 0) 0 else value
        }
    private var totalSize = 0L
        set(value) {
            field = if (value < 0) 0 else value
        }
    private var totalTime = 0L
        set(value) {
            field = if (value < 0) 0 else value
        }
    private var queueCount = 0
        set(value) {
            field = if (value < 0) 0 else value
        }
    private var errorCount = 0
        set(value) {
            field = if (value < 0) 0 else value
        }

    override fun onBind(intent: Intent?): IBinder {
        return LocalBinder(this.downloadManager)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (ACTION_PAUSE_ALL) {
            intent?.action -> {
                downloadManager.pauseAll()
            }
            else -> {
                val item = intent?.getParcelableExtra<DownloadItem>("item")
                item?.let {
                    if (!downloadManager.hasTask(it)) {
                        downloadManager.add(it)
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(false)
        downloadManager.stop()
        stopSelf()
    }

    override fun onQueued(item: DownloadItem) {
        totalCount++
        queueCount++
        log("onQueued total=$totalCount queue=$queueCount")

        sendStartNotification(item)
    }

    override fun onProgress(item: DownloadItem) {
        log("onProgress")
        sendProgressNotification(item)
    }

    override fun onPaused(item: DownloadItem) {
        queueCount--
        totalCount--
        log("onPaused total=$totalCount queue=$queueCount")
        sendCompleteNotification()
    }

    override fun onError(item: DownloadItem, cause: Throwable?) {
        errorCount++
        queueCount--
        log("onError error=$errorCount queue=$queueCount")
        sendCompleteNotification()
    }

    override fun onCompleted(item: DownloadItem) {
        queueCount--
        totalSize += item.downloadSize
        totalTime += item.totalTime
        log("onCompleted total=$totalCount queue=$queueCount")
        sendCompleteNotification()
    }

    private fun sendStartNotification(item: DownloadItem) {
        if (queueCount == 1) {
            with(NotificationCompat.Builder(this, channelId)) {
                setSmallIcon(R.drawable.ic_action_download_white)
                setContentTitle(getString(R.string.download_service_queue, queueCount))
                setContentText(item.name)
                setProgress(0, 0, true)
                setContentIntent(actionGoToDownloads)
                addAction(actionPauseAll)
                startForeground(notificationId, build())
            }
        }
    }

    private fun sendProgressNotification(item: DownloadItem) {
        with(NotificationCompat.Builder(this, channelId)) {
            setSmallIcon(R.drawable.ic_action_download_white)
            setContentTitle(getString(R.string.download_service_queue, queueCount))
            setContentText(item.name)
            setProgress(item.totalPages, item.downloadPages, false)
            setContentIntent(actionGoToDownloads)
            addAction(actionPauseAll)
            startForeground(notificationId, build())
        }
    }

    private fun sendCompleteNotification() {
        when {
            errorCount > 0 &&
                    errorCount == totalCount -> NotificationCompat.Builder(this, channelId).apply {
                setSmallIcon(R.drawable.ic_action_download_white)
                setContentTitle(getString(R.string.download_service_complete_title_error))
                setContentText(getString(R.string.download_service_complete_text_error))
                setContentIntent(actionGoToDownloads)

                stopForeground(false)
                notificationManager.notify(notificationId, build())
                clearCounters()
            }
            queueCount == 0 &&
                    totalCount > 0 -> {
                val builder = NotificationCompat.Builder(this, channelId).apply {
                    setSmallIcon(R.drawable.ic_action_download_white)
                    setContentTitle(getString(R.string.download_service_complete_title))
                    setContentText(getString(R.string.download_service_complete_text))
                    setContentIntent(actionGoToDownloads)
                }

                val notify = NotificationCompat.InboxStyle(builder)
                addTotalAndErrorCountLine(notify)
                addSizeAndTimeLine(notify)

                stopForeground(false)
                notificationManager.notify(notificationId, notify.build())
                clearCounters()
            }
            queueCount == 0 &&
                    totalCount == 0 -> NotificationCompat.Builder(this, channelId).apply {
                setSmallIcon(R.drawable.ic_action_download_white)
                setContentTitle(getString(R.string.download_service_complete_title_paused))
                setContentText(getString(R.string.download_service_complete_text_paused))
                setContentIntent(actionGoToDownloads)

                stopForeground(false)
                notificationManager.notify(notificationId, build())
                clearCounters()
            }

        }
    }

    private fun addSizeAndTimeLine(notify: NotificationCompat.InboxStyle) {
        val totalTime = totalTime / 1000
        if (totalTime < 60) {
            notify.addLine(
                getString(
                    R.string.download_service_complete_time_sec,
                    formatDouble(bytesToMbytes(totalSize)),
                    totalTime
                )
            )
        } else {
            val mins = totalTime / 60
            val secs = totalTime % 60
            notify.addLine(
                getString(
                    R.string.download_service_complete_time_min_sec,
                    formatDouble(bytesToMbytes(totalSize)),
                    mins,
                    secs
                )
            )
        }
    }

    private fun addTotalAndErrorCountLine(notify: NotificationCompat.InboxStyle) {
        if (errorCount == 0) {
            notify.addLine(
                resources.getQuantityString(
                    R.plurals.download_service_complete_without_error,
                    totalCount,
                    totalCount
                )
            )
        } else {
            log("total count = ${totalCount}")
            notify.addLine(
                resources.getQuantityString(
                    R.plurals.download_service_complete_with_error_start,
                    totalCount,
                    totalCount
                ) + " " +
                        resources.getQuantityString(
                            R.plurals.download_service_complete_with_error_end,
                            errorCount,
                            errorCount
                        )
            )
        }
    }

    private fun clearCounters() {
        totalCount = 0
        totalSize = 0
        totalTime = 0
        queueCount = 0
        errorCount = 0

    }

    class LocalBinder(val chapterLoader: ChapterLoader) : Binder()
}
