package com.san.kir.manger.components.DownloadManager

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.san.kir.manger.R
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.update
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus
import com.san.kir.manger.utils.ID
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.notificationManager


class DownloadService : Service(), DownloadListener {
    companion object {
        const val ACTION_PAUSE_ALL = "kir.san.manger.DownloadService.PAUSE_ALL"
        private val channelId = "DownloadServiceId"
    }

    lateinit var downloadManager: DownloadManager
    private val downloadControl = DownloadControl()
    private val dao = Main.db.downloadDao

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
                .Builder(R.drawable.ic_stop_white, "Приостановить все", pauseAll)
                .build()
    }

    override fun onBind(intent: Intent?): IBinder {
        return LocalBinder(this)
    }

    override fun onCreate() {
        super.onCreate()
        downloadManager = DownloadManager(this)
        downloadControl.register(this)
        downloadControl.addListener(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (ACTION_PAUSE_ALL) {
            intent?.action -> {
                downloadManager.pauseAllTask()
                sendNotification(false)
            }
            else -> {
                val item = intent?.getParcelableExtra<DownloadItem>("item")
                item?.let {
                    if (!downloadManager.hasTask(it)) {
                        downloadManager.addTask(it)
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadManager.finish()
        downloadControl.removeListeners()
        downloadControl.unregister(this)
    }

    override fun onAdd(item: DownloadItem) {
        item.status = DownloadStatus.loading
        dao.update(item)
        sendNotification()
    }

    override fun onStart(item: DownloadItem) {
        dao.update(item)
    }

    override fun onUpdate(item: DownloadItem) {
        dao.update(item)
    }

    override fun onPause(item: DownloadItem) {
        item.status = DownloadStatus.pause
        dao.update(item)
    }

    override fun onError(item: DownloadItem) {
        item.status = DownloadStatus.error
        dao.update(item)
        sendNotification()
    }

    override fun onComplete(item: DownloadItem) {
        item.status = DownloadStatus.completed
        dao.update(item)
        sendNotification()
    }

    private fun sendNotification(isAction: Boolean = true) {
        with(NotificationCompat.Builder(this, channelId)) {
            setSmallIcon(R.drawable.ic_action_download_white)
            if (downloadManager.getTotalTaskCount() != 0) {
                setContentTitle("Осталось скачать ${downloadManager.getTotalTaskCount()} гл.")
                setProgress(0, 0, true)
            } else {
                setContentTitle("Загрузка завершена")
                setContentText("Приятного чтения")
                setAutoCancel(true)
            }
            setContentIntent(actionGoToDownloads)
            if (isAction || downloadManager.getTotalTaskCount() != 0)
                addAction(actionPauseAll)
            notificationManager.notify(notificationId, build())
        }
    }

    class LocalBinder(val service: DownloadService) : Binder()
}
