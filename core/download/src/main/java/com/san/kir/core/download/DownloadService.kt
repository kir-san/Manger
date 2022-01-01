package com.san.kir.core.download

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.san.kir.core.internet.WifiNetwork
import com.san.kir.core.utils.ID
import com.san.kir.core.utils.bytesToMb
import com.san.kir.core.utils.formatDouble
import com.san.kir.core.utils.intentFor
import com.san.kir.core.utils.log
import com.san.kir.core.utils.startService
import com.san.kir.data.models.Chapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@SuppressLint("UnspecifiedImmutableFlag")
@AndroidEntryPoint
class DownloadService : LifecycleService(), DownloadListener {
    companion object {
        private const val ACTION_PAUSE_ALL = "kir.san.manger.DownloadService.PAUSE_ALL"
        private const val ACTION_PAUSE = "kir.san.manger.DownloadService.PAUSE"

        private const val ACTION_START_ALL = "kir.san.manger.DownloadService.START_ALL"
        private const val ACTION_START = "kir.san.manger.DownloadService.START"

        private const val TAG = "DownloadService"

        fun startAll(ctx: Context) = startService<DownloadService>(ctx, ACTION_START_ALL)

        fun start(ctx: Context, item: Chapter) =
            startService<DownloadService>(ctx, ACTION_START, "item" to item)

        fun pauseAll(ctx: Context) = startService<DownloadService>(ctx, ACTION_PAUSE_ALL)

        fun pause(ctx: Context, item: Chapter) =
            startService<DownloadService>(ctx, ACTION_PAUSE, "item" to item)

        //        MainNavTarget.Downloader.deepLink.toUri(),
        var actionGoToDownloads: PendingIntent? = null
        inline fun <reified T : Any> setActionGoToDownloads(ctx: Context, uri: Uri) {
            val deepLinkIntent = Intent(Intent.ACTION_VIEW, uri, ctx, T::class.java)
            actionGoToDownloads = TaskStackBuilder.create(ctx).run {
                addNextIntentWithParentStack(deepLinkIntent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }
    }

    @Inject
    lateinit var downloadManager: ChapterLoader

    @Inject
    lateinit var downloadStore: com.san.kir.data.store.DownloadStore

    @Inject
    lateinit var wifiNetwork: WifiNetwork

    private val notificationManager by lazy { NotificationManagerCompat.from(this) }

    private var channelId = ""
    private var notificationId = ID.generate()

    private val actionPauseAll by lazy {
        val intent = intentFor<DownloadService>(this).setAction(ACTION_PAUSE_ALL)
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

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        setForeground()

        downloadManager.addListener(this, this)

        lifecycleScope.launchWhenCreated {
            combine(
                downloadStore.data,
                wifiNetwork.state
            ) { data, wifi ->
                downloadManager.setConcurrentPages(if (data.concurrent) 4 else 1)
                downloadManager.setRetryOnError(data.retry)
                downloadManager.isWifiOnly(data.wifi)

                if (data.wifi && wifi.not()) {
                    setNoWifiForeground()
                } else {
                    setForeground()
                }
            }
        }
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

    private fun setNoWifiForeground() {
        with(NotificationCompat.Builder(this, channelId)) {
            setSmallIcon(R.drawable.ic_notification_download)
            setContentTitle(getString(R.string.download_service_title))
            setContentText(getString(R.string.download_service_wifi_message))
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


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE_ALL -> downloadManager.pauseAll()
            ACTION_PAUSE -> {
                val item = intent.getParcelableExtra<Chapter>("item")
                item?.let {
                    downloadManager.pause(it)
                }
            }

            ACTION_START_ALL -> downloadManager.startAll()
            ACTION_START -> {
                val item = intent.getParcelableExtra<Chapter>("item")
                item?.let {
                    downloadManager.start(it)
                }
            }
            else -> super.onStartCommand(intent, flags, startId)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        clearCounters()
        downloadManager.pauseAll()
        downloadManager.clearListeners()
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
