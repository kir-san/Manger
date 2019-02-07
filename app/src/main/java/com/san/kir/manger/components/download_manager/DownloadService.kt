package com.san.kir.manger.components.download_manager

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import com.san.kir.manger.R
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.bytesToMb
import com.san.kir.manger.utils.formatDouble
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.notificationManager


class DownloadService : Service(), DownloadListener {
    companion object {
        private const val ACTION_PAUSE_ALL = "kir.san.manger.DownloadService.PAUSE_ALL"

        private const val TAG = "DownloadService"
    }

    private val downloadManager by lazy {
        ChapterLoader(applicationContext).also {
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
                val wifiDefault =
                    getString(R.string.settings_downloader_wifi_only_default) == "true"
                val isWifi = getBoolean(wifiKey, wifiDefault)
                it.isWifiOnly(isWifi)
            }
        }
    }

    private var channelId = ""
    private var notificationId = ID.generate()

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


    private var totalSize = 0L
        set(value) {
            field = if (value < 0) 0 else value
        }
    private var totalTime = 0L
        set(value) {
            field = if (value < 0) 0 else value
        }
    private var totalCount = 0
    private var queueCount = 0
    private var errorCount = 0

    @SuppressLint("InlinedApi")
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channelId = "DownloadServiceId"
        val channelName = TAG
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        notificationManager.createNotificationChannel(chan)
        this.channelId = channelId
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
                    GlobalScope.launch {
                        if (!downloadManager.hasTask(it)) {
                            downloadManager.add(it)
                        }
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
        if (queueCount == 1) {
            sendStartNotification(item)
        }
    }

    override fun onProgress(item: DownloadItem) {
        sendProgressNotification(item)
    }

    override fun onPaused(item: DownloadItem) {
        queueCount--
        totalCount--
        if (queueCount == 0)
            sendCompleteNotification()
        else if (queueCount < 0)
            clearCounters()

    }

    override fun onError(item: DownloadItem, cause: Throwable?) {
        errorCount++
        queueCount--
        if (queueCount == 0)
            sendCompleteNotification()
        else if (queueCount < 0)
            clearCounters()
    }

    override fun onCompleted(item: DownloadItem) {
        queueCount--
        totalSize += item.downloadSize
        totalTime += item.totalTime
        if (queueCount == 0)
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
        val notify = when {
            errorCount > 0 &&
                    errorCount == totalCount -> NotificationCompat.Builder(this, channelId).apply {
                setSmallIcon(R.drawable.ic_notification_download)
                setContentTitle(getString(R.string.download_service_complete_title_error))
                setContentText(getString(R.string.download_service_complete_text_error))
                setContentIntent(actionGoToDownloads)
            }.build()
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
                notify.build()
            }
            queueCount == 0 &&
                    totalCount == 0 -> NotificationCompat.Builder(this, channelId).apply {
                setSmallIcon(R.drawable.ic_notification_download)
                setContentTitle(getString(R.string.download_service_complete_title_paused))
                setContentText(getString(R.string.download_service_complete_text_paused))
                setContentIntent(actionGoToDownloads)
            }.build()
            else -> null
        }

        stopForeground(false)
        notificationManager.cancel(notificationId)
        notificationManager.notify(notificationId, notify)
        clearCounters()
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
        notificationId = ID.generate()
    }

    //    class LocalBinder(val chapterLoader: ChapterLoader) : Binder()
    class LocalBinderC(val chapterLoader: ChapterLoader) : Binder()
}
