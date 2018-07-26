package com.san.kir.manger.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.support.annotation.WorkerThread
import android.support.v4.app.NotificationCompat
import com.san.kir.manger.R
import com.san.kir.manger.components.downloadManager.DownloadService
import com.san.kir.manger.components.latestChapters.LatestChapterActivity
import com.san.kir.manger.components.listChapters.SearchDuplicate
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.room.dao.downloadNewChapters
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.LatestChapter
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.toDownloadItem
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.startService

class MangaUpdaterService : Service() {
    companion object {
        const val ACTION_CANCEL_ALL = "kir.san.manger.MangaUpdaterService.CANCEL_ALL"
        const val ACTION_DOWNLOAD_NEW = "kir.san.manger.MangaUpdaterService.DOWNLOAD_NEW"
        const val actionGet = "MangaUpdaterActionGet"
        const val actionSend = "MangaUpdaterActionSend"

        const val ITEM_NAME = "mangaName"
        const val IS_FOUND_NEW = "isFoundNew"
        const val COUNT_NEW = "countNew"

        private const val TAG = "MangaUpdaterService"
        private const val channelId = "MangaUpdaterId"
        fun contains(manga: Manga) =
            taskCounter.any { it.unic == manga.unic }

        private var taskCounter = listOf<Manga>()
    }

    private var notificationId = ID.generate()

    private val chapters = Main.db.chapterDao
    private val latestChapters = Main.db.latestChapterDao

    @Volatile
    private lateinit var mServiceLopper: Looper
    @Volatile
    private lateinit var mServiceHandler: ServiceHandler

    private val actionGoToLatest by lazy {
        val intent = intentFor<LatestChapterActivity>()
        PendingIntent.getActivity(this, 0, intent, 0)
    }
    private val actionCancelAll by lazy {
        val intent = intentFor<MangaUpdaterService>().setAction(ACTION_CANCEL_ALL)
        val cancelAll = PendingIntent.getService(this, 0, intent, 0)
        NotificationCompat
            .Action
            .Builder(
                R.drawable.ic_notification_cancel,
                getString(R.string.manga_update_action_cancel_all),
                cancelAll
            )
            .build()
    }
    private val actionDownloadNew by lazy {
        val intent = intentFor<MangaUpdaterService>().setAction(ACTION_DOWNLOAD_NEW)
        val downloadNew = PendingIntent.getService(this, 0, intent, 0)
        NotificationCompat
            .Action
            .Builder(
                R.drawable.ic_notification_download,
                getString(R.string.manga_update_action_download_new),
                downloadNew
            )
            .build()
    }

    private var progress = 0 // Прогресс проверенных манг
    private var error = 0 // Счетчик закончившихся с ошибкой
    private var fullCountNew = 0 // Количество новых глав
    private var mangaName = ""

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("InlinedApi")
    override fun onCreate() {
        super.onCreate()

        notificationId = ID.generate()

        progress = 0
        fullCountNew = 0
        error = 0

        val thread = HandlerThread(TAG)
        thread.start()

        mServiceLopper = thread.looper
        mServiceHandler = ServiceHandler(mServiceLopper, this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val importance = NotificationManager.IMPORTANCE_DEFAULT

                NotificationChannel(channelId, TAG, importance).apply {
                    description = "MangaUpdateServiceDescription"
                    enableLights(false)
                    enableVibration(false)
                    notificationManager.createNotificationChannel(this)
                }
            }
            startForeground(notificationId, Notification())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (intent.action) {
                ACTION_CANCEL_ALL -> stopSelf()
                ACTION_DOWNLOAD_NEW -> downloadNew()
                else -> {
                    val task = intent.getParcelableExtra<Manga>("manga")

                    if (task.isUpdate) {
                        taskCounter += task

                        val intentSend = Intent(actionSend)
                        intentSend.putExtra(ITEM_NAME, task.unic)
                        sendBroadcast(intentSend)

                        val msg = mServiceHandler.obtainMessage()
                        msg.arg1 = startId
                        msg.obj = task
                        mServiceHandler.sendMessage(msg)
                    } else {

                    }
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun downloadNew() = async {
        Main.db.latestChapterDao.downloadNewChapters().await().onEach { chapter ->
            startService<DownloadService>("item" to chapter.toDownloadItem())
        }
    }

    override fun onDestroy() {
        mServiceLopper.quit()

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.manga_update_notify_update_ready))
            .setSmallIcon(R.drawable.ic_notification_update)
            .setContentIntent(actionGoToLatest)

        if (fullCountNew != 0) {
            builder.addAction(actionDownloadNew)
        }

        val notify = NotificationCompat.InboxStyle(builder)
            .addLine(
                if (progress > 1)
                    getString(R.string.manga_update_notify_checked_manga, progress)
                else
                    getString(R.string.manga_update_notify_checked_one_manga, mangaName)
            )
            .addLine(getString(R.string.manga_update_notify_new_founded, fullCountNew))
            .addLine(getString(R.string.manga_update_notify_founded_with_error, error))
            .build()
        notificationManager.notify(notificationId, notify)

        stopSelf()
    }

    @WorkerThread
    fun onHandleIntent(manga: Manga) {
        runBlocking {
            var countNew = 0
            try {
                val notify = NotificationCompat.InboxStyle(
                    NotificationCompat.Builder(this@MangaUpdaterService, channelId)
                        .setContentTitle(getString(R.string.manga_update_notify_searching))
                        .setSmallIcon(R.drawable.ic_notification_update)
                        .addAction(actionCancelAll)
                        .setContentText(manga.name)
                )
                    .addLine(manga.name)
                    .addLine(getString(R.string.manga_update_notify_remained, taskCounter.size))
                    .build()

                notificationManager.notify(notificationId, notify)

                mangaName = manga.name

                val oldChapters = chapters.loadChapters(manga.unic)
                var newChapters = listOf<Chapter>()

                ManageSites.chapters(manga)?.let { new ->
                    if (oldChapters.isEmpty()) {
                        newChapters = new
                    } else {
                        new.forEach { chapter ->
                            if (oldChapters.none { oldChapter -> chapter.site == oldChapter.site }) {
                                newChapters += chapter
                            } else {
                                val tempChapter = oldChapters
                                    .first { oldChapter -> chapter.site == oldChapter.site }
                                tempChapter.path = chapter.path
                                chapters.update(tempChapter)
                            }
                        }
                    }
                }

                if (newChapters.isNotEmpty()) {
                    newChapters.reversed().forEach {
                        chapters.insert(it)
                        latestChapters.insert(LatestChapter(it))
                    }
                    val oldSize = oldChapters.size

                    SearchDuplicate.silentRemoveDuplicate(manga).await()

                    val newSize = chapters.loadChapters(manga.unic).size

                    countNew = newSize - oldSize
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                error++
                countNew = 0
            } finally {
                progress++
                fullCountNew += countNew
                taskCounter -= manga

                val intent = Intent(actionGet)
                intent.putExtra(ITEM_NAME, manga.unic)
                intent.putExtra(IS_FOUND_NEW, countNew > 0)
                intent.putExtra(COUNT_NEW, countNew)
                sendBroadcast(intent)
            }
        }
    }

    private open class ServiceHandler(
        looper: Looper,
        val service: MangaUpdaterService
    ) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            service.onHandleIntent(msg.obj as Manga)
            service.stopSelf(msg.arg1)
        }
    }
}
