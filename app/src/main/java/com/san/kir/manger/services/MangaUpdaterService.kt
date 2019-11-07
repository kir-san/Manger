package com.san.kir.manger.services

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
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import com.san.kir.ankofork.intentFor
import com.san.kir.ankofork.sdk28.notificationManager
import com.san.kir.manger.R
import com.san.kir.manger.components.latest_chapters.LatestChapterActivity
import com.san.kir.manger.components.list_chapters.SearchDuplicate
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.getShortLink
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.utils.ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MangaUpdaterService : Service() {
    companion object {
        const val ACTION_CANCEL_ALL = "kir.san.manger.MangaUpdaterService.CANCEL_ALL"
        const val actionGet = "MangaUpdaterActionGet"
        const val actionSend = "MangaUpdaterActionSend"

        const val ITEM_NAME = "mangaName"
        const val IS_FOUND_NEW = "isFoundNew"
        const val COUNT_NEW = "countNew"

        private const val TAG = "MangaUpdaterService"

        fun contains(manga: Manga) =
            taskCounter.any { it.unic == manga.unic }

        private var taskCounter = listOf<Manga>()
    }

    private var notificationId = ID.generate()
    private var channelId = ""
    private val mChapterRepository = ChapterRepository(this)
    private val mMangaRepository = MangaRepository(this)

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
        progress = 0
        fullCountNew = 0
        error = 0

        val thread = HandlerThread(TAG)
        thread.start()

        mServiceLopper = thread.looper
        mServiceHandler =
            ServiceHandler(mServiceLopper, this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()

            with(NotificationCompat.Builder(this, channelId)) {
                setSmallIcon(R.mipmap.icon_launcher)
                startForeground(notificationId, build())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channelId = "MangaUpdaterChannelId"
        val channelName = TAG
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        notificationManager.createNotificationChannel(chan)
        this.channelId = channelId
    }

    @Suppress("ControlFlowWithEmptyBody")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (intent.action) {
                ACTION_CANCEL_ALL -> {
                    val tempIntent = Intent(actionGet)
                    tempIntent.putExtra(ITEM_NAME, mangaName)
                    tempIntent.putExtra(IS_FOUND_NEW, false)
                    tempIntent.putExtra(COUNT_NEW, -1)
                    sendBroadcast(tempIntent)
                    stopSelf()
                }
                else -> {
                    val task = intent.getParcelableExtra<Manga>("manga")

                    if (task.isUpdate) {
                        taskCounter = taskCounter + task

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

    override fun onDestroy() {
        mServiceLopper.quit()

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.manga_update_notify_update_ready))
            .setSmallIcon(R.drawable.ic_notification_update)
            .setContentIntent(actionGoToLatest)

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

        stopForeground(false)
        notificationManager.cancel(notificationId)
        notificationManager.notify(notificationId, notify)

        notificationId = ID.generate()
        stopSelf()
    }

    @WorkerThread
    fun onHandleIntent(manga: Manga) = runBlocking {
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
            startForeground(notificationId, notify)

            val mangaDB = mMangaRepository.getItem(manga.unic)

            if (mangaDB.shortLink.isEmpty()) {
                val site = ManageSites.getSite(mangaDB.host)

                mangaDB.host = site.host
                mangaDB.shortLink = site.getShortLink(mangaDB.site)

                mMangaRepository.update(mangaDB)
            } else {
                val site = ManageSites.getSite(mangaDB.host)

                mangaDB.host = site.host

                mMangaRepository.update(mangaDB)
            }

            mangaName = mangaDB.name

            val oldChapters =
                withContext(Dispatchers.IO) {
                    mChapterRepository
                        .getItems(mangaDB.unic)
                        .onEach {
                            launch(Dispatchers.Default) {
                                if (
                                    it.pages.isNullOrEmpty()
                                    ||
                                    it.pages.any { chap -> chap.isBlank() }
                                    ||
                                    mangaDB.isAlternativeSite
                                ) {
                                    it.pages = ManageSites.pages(it)
                                    mChapterRepository.update(it)
                                }
                            }.join()
                        }
                }

            var newChapters = listOf<Chapter>()

            ManageSites.chapters(mangaDB).let { new ->
                if (oldChapters.isEmpty()) { // Если глав не было до обновления
                    newChapters = new
                } else {
                    new.forEach { chapter ->
                        // Если глава отсутствует в базе данных то добавить
                        if (oldChapters.none { oldChapter -> chapter.site == oldChapter.site }) {
                            newChapters = newChapters + chapter
                        } else {
                            val tempChapter = oldChapters
                                .first { oldChapter -> chapter.site == oldChapter.site }
                            tempChapter.path = chapter.path
                            mChapterRepository.update(tempChapter)
                        }
                    }
                }
            }

            if (newChapters.isNotEmpty()) {
                newChapters.reversed().forEach {
                    it.pages = ManageSites.pages(it)
                    it.isInUpdate = true
                    mChapterRepository.insert(it)
                }
                val oldSize = oldChapters.size

                withContext(Dispatchers.IO) {
                    SearchDuplicate(this@MangaUpdaterService).silentRemoveDuplicate(mangaDB)
                }

                val newSize = mChapterRepository.getItems(mangaDB.unic).size

                countNew = newSize - oldSize
            } else {
                withContext(Dispatchers.IO) {
                    SearchDuplicate(this@MangaUpdaterService).silentRemoveDuplicate(mangaDB)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            error++
            countNew = 0
        } finally {
            progress++
            fullCountNew += countNew
            taskCounter = taskCounter - manga

            val intent = Intent(actionGet)
            intent.putExtra(ITEM_NAME, manga.unic)
            intent.putExtra(IS_FOUND_NEW, countNew > 0)
            intent.putExtra(COUNT_NEW, countNew)
            sendBroadcast(intent)
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
