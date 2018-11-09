package com.san.kir.manger.components.downloadManager

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.san.kir.manger.R
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.bytesToMb
import com.san.kir.manger.utils.formatDouble
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.notificationManager


class DownloadService : Service(), DownloadListener {
    companion object {
        private const val ACTION_PAUSE_ALL = "kir.san.manger.DownloadService.PAUSE_ALL"
        private const val channelId = "DownloadServiceId"
        private const val name = "DownloadServiceName"
        private const val description = "DownloadServiceDescription"
    }

    private val downloadManager by lazy {
        ChapterLoaderC(applicationContext).also {
            it.addListener(this, this)
            defaultSharedPreferences.apply {
                val concurrentKey = getString(R.string.settings_downloader_parallel_key)
                val concurrentDefault =
                    getString(R.string.settings_downloader_parallel_default) == "true"
                val isParallel = getBoolean(concurrentKey, concurrentDefault)
                it.setConcurrentPages(if (isParallel) 4 else 1)

                val retryKey = getString(R.string.settings_downloader_retry_key)
                val retryDefault = getString(R.string.settings_downloader_retry_default) == "true"
                val isRetry = getBoolean(retryKey, retryDefault)
                it.setRetryOnError(isRetry)

                val wifiKey = getString(R.string.settings_downloader_wifi_only_key)
                val wifiDefault = getString(R.string.settings_downloader_wifi_only_default) == "true"
                val isWifi = getBoolean(wifiKey, wifiDefault)
                it.isWifiOnly(isWifi)
            }
        }
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
                R.drawable.ic_notification_stop,
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

    @SuppressLint("InlinedApi")
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val importance = NotificationManager.IMPORTANCE_DEFAULT

                NotificationChannel(channelId, name, importance).apply {
                    description = DownloadService.description
                    notificationManager.createNotificationChannel(this)
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return LocalBinderC(this.downloadManager)
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
        sendStartNotification(item)
    }

    override fun onProgress(item: DownloadItem) {
        sendProgressNotification(item)
    }

    override fun onPaused(item: DownloadItem) {
        queueCount--
        totalCount--
        sendCompleteNotification()
    }

    override fun onError(item: DownloadItem, cause: Throwable?) {
        errorCount++
        queueCount--
        sendCompleteNotification()
    }

    override fun onCompleted(item: DownloadItem) {
        queueCount--
        totalSize += item.downloadSize
        totalTime += item.totalTime
        sendCompleteNotification()
    }

    private fun sendStartNotification(item: DownloadItem) {
        if (queueCount == 1) {
            with(NotificationCompat.Builder(this, channelId)) {
                setSmallIcon(R.drawable.ic_notification_download)
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
            setSmallIcon(R.drawable.ic_notification_download)
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
                setSmallIcon(R.drawable.ic_notification_download)
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
                    setSmallIcon(R.drawable.ic_notification_download)
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
                setSmallIcon(R.drawable.ic_notification_download)
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
                    formatDouble(bytesToMb(totalSize)),
                    totalTime
                )
            )
        } else {
            val mins = totalTime / 60
            val secs = totalTime % 60
            notify.addLine(
                getString(
                    R.string.download_service_complete_time_min_sec,
                    formatDouble(bytesToMb(totalSize)),
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
    class LocalBinderC(val chapterLoader: ChapterLoaderC) : Binder()
}
