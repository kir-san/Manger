package com.san.kir.manger.utils

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.support.annotation.WorkerThread
import android.support.v4.app.NotificationCompat
import com.san.kir.manger.R
import com.san.kir.manger.components.DownloadManager.DownloadService
import com.san.kir.manger.components.LatestChapters.LatestChapterActivity
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.room.DAO.downloadNewChapters
import com.san.kir.manger.room.DAO.insert
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.LatestChapter
import com.san.kir.manger.room.models.Manga
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.startService

class MangaUpdaterService : Service() {
    companion object {
        const val ACTION_CANCELALL = "kir.san.manger.MangaUpdaterService.CANCELALL"
        const val ACTION_DOWNLOADNEW = "kir.san.manger.MangaUpdaterService.DOWNLOADNEW"
        const val actionGet = "MangaUpdaterActionGet"
        const val actionSend = "MangaUpdaterActionSend"

        const val ITEM_NAME = "unic"
        const val IS_FOUND_NEW = "isFoundNew"
        const val COUNT_NEW = "countNew"

        private const val TAG = "MangaUpdaterService"
        private const val channelId = "MangaUpdaterId"
        fun contains(manga: Manga) =
            taskCounter.any { it.unic == manga.unic }

        private var taskCounter = listOf<Manga>()
        private val notificationId = ID.generate()
    }

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
        val intent = intentFor<MangaUpdaterService>().setAction(ACTION_CANCELALL)
        val cancellAll = PendingIntent.getService(this, 0, intent, 0)
        NotificationCompat
            .Action
            .Builder(R.drawable.ic_cancel, "Отменить все", cancellAll)
            .build()
    }
    private val actionDownloadNew by lazy {
        val intent = intentFor<MangaUpdaterService>().setAction(ACTION_DOWNLOADNEW)
        val downloadNew = PendingIntent.getService(this, 0, intent, 0)
        NotificationCompat
            .Action
            .Builder(R.drawable.ic_action_download_white, "Скачать новое", downloadNew)
            .build()
    }

    private var progress = 0 // Прогресс проверенных манг
    private var error = 0 // Счетчик закончившихся с ошибкой
    private var fullCountNew = 0 // Количество новых глав

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        progress = 0
        fullCountNew = 0
        error = 0

        val thread = HandlerThread(TAG)
        thread.start()

        mServiceLopper = thread.looper
        mServiceHandler = ServiceHandler(mServiceLopper, this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (intent.action) {
                ACTION_CANCELALL -> stopSelf()
                ACTION_DOWNLOADNEW -> downloadNew()
                else -> {
                    val task = intent.getParcelableExtra<Manga>("manga")
                    taskCounter += task

                    val intentSend = Intent(actionSend)
                    intentSend.putExtra(ITEM_NAME, task.unic)
                    sendBroadcast(intentSend)

                    val msg = mServiceHandler.obtainMessage()
                    msg.arg1 = startId
                    msg.obj = task
                    mServiceHandler.sendMessage(msg)
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun downloadNew() = async {
        Main.db.latestChapterDao.downloadNewChapters().await().onEach { chapter ->
            val item = DownloadItem(
                name = chapter.manga + " " + chapter.name,
                link = chapter.site,
                path = chapter.path
            )
            startService<DownloadService>("item" to item)
        }
    }

    override fun onDestroy() {
        mServiceLopper.quit()

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Обновление завершено")
            .setSmallIcon(R.drawable.ic_notify_updater)
            .setContentIntent(actionGoToLatest)

        if (fullCountNew != 0) {
            builder.addAction(actionDownloadNew)
        }


        val notify = NotificationCompat.InboxStyle(builder)
            .addLine("Проверенно манг было: $progress")
            .addLine("Найдено новых глав: $fullCountNew")
            .addLine("С ошибкой проверенно: $error")
            .build()
        notificationManager.notify(notificationId, notify)
    }

    @WorkerThread
    fun onHandleIntent(manga: Manga) {
        runBlocking {
            var countNew = 0
            try {
                val notify = NotificationCompat.InboxStyle(
                    NotificationCompat.Builder(this@MangaUpdaterService, channelId)
                        .setContentTitle("Поиск новых глав")
                        .setSmallIcon(R.drawable.ic_notify_updater)
                        .addAction(actionCancelAll)
                        .setContentText(manga.name)
                )
                    .addLine(manga.name)
                    .addLine("Осталось: ${taskCounter.size}")
                    .build()

                notificationManager.notify(notificationId, notify)

                val oldChapters = chapters.loadChapters(manga.unic)
                var newChapters = listOf<Chapter>()

                ManageSites.chapters(manga)?.let { new ->
                    new.forEach { chapter ->
                        if (oldChapters.isNotEmpty()) {
                            if (oldChapters.none { oldChapter ->
                                    chapter.site == oldChapter.site
                                }) {
                                newChapters += chapter
                            }
                        }
                    }

                }

                if (newChapters.isNotEmpty()) {
                    newChapters.reversed().forEach {
                        chapters.insert(it)
                        latestChapters.insert(LatestChapter(it))
                    }

                    countNew = newChapters.size
                }
            } catch (ex: Exception) {
                error++
                countNew = -1
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
