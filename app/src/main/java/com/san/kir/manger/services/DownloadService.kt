package com.san.kir.manger.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.san.kir.ankofork.intentFor
import com.san.kir.manger.R
import com.san.kir.manger.components.download_manager.ChapterLoader
import com.san.kir.manger.components.download_manager.DownloadListener
import com.san.kir.manger.components.download_manager.DownloadManagerActivity
import com.san.kir.manger.data.datastore.DownloadRepository
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.extensions.bytesToMb
import com.san.kir.manger.utils.extensions.formatDouble
import com.san.kir.manger.utils.extensions.log
import com.san.kir.manger.utils.extensions.startForegroundServiceIntent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject

@SuppressLint("UnspecifiedImmutableFlag")
@AndroidEntryPoint
class DownloadService : Service(), DownloadListener, CoroutineScope {
    companion object {
        private const val ACTION_PAUSE_ALL = "kir.san.manger.DownloadService.PAUSE_ALL"
        private const val ACTION_PAUSE = "kir.san.manger.DownloadService.PAUSE"

        private const val ACTION_START_ALL = "kir.san.manger.DownloadService.START_ALL"
        private const val ACTION_START = "kir.san.manger.DownloadService.START"

        private const val TAG = "DownloadService"

        fun startAll(ctx: Context) = with(ctx) {
            startForegroundServiceIntent(
                intentFor<DownloadService>().setAction(ACTION_START_ALL)
            )
        }

        fun start(ctx: Context, item: Chapter) = with(ctx) {
            startForegroundServiceIntent(
                intentFor<DownloadService>("item" to item).setAction(ACTION_START)
            )
        }

        fun pauseAll(ctx: Context) {
            with(ctx) {
                startService(
                    intentFor<DownloadService>().setAction(ACTION_PAUSE_ALL)
                )
            }
        }

        fun pause(ctx: Context, item: Chapter) {
            with(ctx) {
                startService(
                    intentFor<DownloadService>("item" to item).setAction(
                        ACTION_PAUSE
                    )
                )
            }
        }
    }

    @Inject
    lateinit var downloadManager: ChapterLoader

    @Inject
    lateinit var downloadStore: DownloadRepository

    private val notificationManager by lazy { NotificationManagerCompat.from(this) }

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

    private val job = Job()
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override val coroutineContext = job + dispatcher

    override fun onCreate() {
        super.onCreate()

        downloadManager.addListener(this, this)

        launch {
            downloadStore.data.collect { data ->
                downloadManager.setConcurrentPages(if (data.concurrent) 4 else 1)
                downloadManager.setRetryOnError(data.retry)
                downloadManager.isWifiOnly(data.wifi)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        setForeground()
    }

    private fun setForeground() {
        with(NotificationCompat.Builder(this, channelId)) {
            setSmallIcon(R.drawable.ic_notification_download)
            setContentTitle(getString(R.string.download_service_title))
            setContentText(getString(R.string.download_service_message))
            setContentIntent(actionGoToDownloads)
            priority = NotificationCompat.PRIORITY_MIN
            setAutoCancel(true)
            startForeground(ID.generate(), build())
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

    override fun onBind(intent: Intent?): Nothing? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_PAUSE_ALL -> downloadManager.pauseAll()
            ACTION_PAUSE -> launch {
                val item = intent.getParcelableExtra<Chapter>("item")
                item?.let {
                    downloadManager.pause(it)
                }
            }

            ACTION_START_ALL -> downloadManager.startAll()
            ACTION_START -> launch {
                val item = intent.getParcelableExtra<Chapter>("item")
                item?.let {
                    downloadManager.start(it)
                }
            }

        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        clearCounters()
        downloadManager.pauseAll()
        downloadManager.clearListeners()
//        downloadManager.stop()
        job.cancel()
    }

    override fun onQueued(item: Chapter) {
        log("onQueued item = $item")
        totalCount++
        queueCount++
        if (queueCount == 1) {
            sendStartNotification(item)
        }
    }

    override fun onProgress(item: Chapter) {
        log("onProgress item = $item")
        sendProgressNotification(item)
    }

    override fun onPaused(item: Chapter) {
        log("onPaused item = $item")
        queueCount--
        totalCount--
        if (queueCount == 0)
            sendCompleteNotification()
        else if (queueCount < 0)
            clearCounters()

    }

    override fun onError(item: Chapter, cause: Throwable?) {
        errorCount++
        queueCount--
        if (queueCount == 0)
            sendCompleteNotification()
        else if (queueCount < 0)
            clearCounters()
    }

    override fun onCompleted(item: Chapter) {
        queueCount--
        totalSize += item.downloadSize
        totalTime += item.totalTime
        if (queueCount <= 0)
            sendCompleteNotification()
    }

    private fun sendStartNotification(item: Chapter) {
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

    private fun sendProgressNotification(item: Chapter) {
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
            errorCount > 0 && errorCount == totalCount ->
                NotificationCompat.Builder(this, channelId).apply {
                    setSmallIcon(R.drawable.ic_notification_download)
                    setContentTitle(getString(R.string.download_service_complete_title_error))
                    setContentText(getString(R.string.download_service_complete_text_error))
                    setContentIntent(actionGoToDownloads)
                }.build()
            queueCount == 0 && totalCount > 0 -> {
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
            queueCount == 0 && totalCount == 0 ->
                NotificationCompat.Builder(this, channelId).apply {
                    setSmallIcon(R.drawable.ic_notification_download)
                    setContentTitle(getString(R.string.download_service_complete_title_paused))
                    setContentText(getString(R.string.download_service_complete_text_paused))
                    setContentIntent(actionGoToDownloads)
                }.build()
            else -> null
        }

        if (notify != null) {
            notificationManager.notify(notificationId++, notify)
            setForeground()
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
        notificationId = ID.generate()
    }
}
