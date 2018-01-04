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
import com.san.kir.manger.components.LatestChapters.LatestChapterActivity
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.room.DAO.insert
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.LatestChapter
import com.san.kir.manger.room.models.Manga
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.notificationManager

class MangaUpdaterService : Service() {
    companion object {
        val ACTION_CANCELALL = "kir.san.manger.MangaUpdaterService.CANCELALL"
        val action = "MangaUpdaterAction"

        val ITEM = "manga"
        val IS_FOUND_NEW = "isFoundNew"
        val COUNT_NEW = "countNew"

        private val TAG = "MangaUpdaterService"
        private val channelId = "MangaUpdaterId"
        fun contains(manga: Manga) = taskCounter.contains(manga)
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

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == ACTION_CANCELALL) {
            stopSelf()
        } else {
            val task = intent.getParcelableExtra("manga") as Manga
            taskCounter += task

            val msg = mServiceHandler.obtainMessage()
            msg.arg1 = startId
            msg.obj = task
            mServiceHandler.sendMessage(msg)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mServiceLopper.quit()

        val notify = NotificationCompat.InboxStyle(
                NotificationCompat.Builder(this, channelId)
                        .setContentTitle("Обновление завершено")
                        .setSmallIcon(R.drawable.ic_notify_updater)
                        .setContentIntent(actionGoToLatest)
        )
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
                with(NotificationCompat.Builder(this@MangaUpdaterService, channelId)) {
                    setSmallIcon(R.drawable.ic_notify_updater)
                    setContentTitle("Поиск новых глав")
                    setContentText(manga.name)
                    setProgress(taskCounter.size, progress, false)
                    addAction(actionCancelAll)
                    notificationManager.notify(notificationId, build())
                }

                val oldChapters = chapters.loadChapters(manga.unic)
                var newChapters = listOf<Chapter>()

                ManageSites.chapters(manga)?.let { new ->
                    new.forEach { chapter ->
                        if (oldChapters.isNotEmpty()) {
                            if (oldChapters.none { oldChapter ->
                                log("chapter ${chapter.site} and oldChapter ${oldChapter.site} is ${chapter.site == oldChapter.site}")
                                chapter.site == oldChapter.site }) {
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

                val intent = Intent(action)
                intent.putExtra(ITEM, manga)
                intent.putExtra(IS_FOUND_NEW, countNew > 0)
                intent.putExtra(COUNT_NEW, countNew)
                sendBroadcast(intent)
            }
        }
    }

    private open class ServiceHandler(looper: Looper,
                                      val service: MangaUpdaterService) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            service.onHandleIntent(msg.obj as Manga)
            service.stopSelf(msg.arg1)
        }
    }
}
