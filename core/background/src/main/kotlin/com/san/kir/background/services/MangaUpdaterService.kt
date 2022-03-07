package com.san.kir.background.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
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
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.san.kir.background.R
import com.san.kir.background.util.SearchDuplicate
import com.san.kir.core.utils.ID
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.core.utils.intentFor
import com.san.kir.core.utils.log
import com.san.kir.core.utils.startService
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.extend.SimplifiedManga
import com.san.kir.data.parsing.SiteCatalogsManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SuppressLint("UnspecifiedImmutableFlag")
@AndroidEntryPoint
class MangaUpdaterService : Service() {
    companion object {
        const val ACTION_CANCEL_ALL = "kir.san.manger.MangaUpdaterService.CANCEL_ALL"
        const val actionGet = "MangaUpdaterActionGet"
        const val actionSend = "MangaUpdaterActionSend"

        const val ITEM_NAME = "mangaName"
        const val COUNT_NEW = "countNew"

        private const val TAG = "MangaUpdaterService"

        fun contains(manga: Manga) =
            taskCounter.any { it == manga.id }

        fun add(ctx: Context, manga: SimplifiedManga) = add(ctx, manga.id)

        fun add(ctx: Context, manga: Manga) = add(ctx, manga.id)

        private fun add(ctx: Context, mangaId: Long) {
            startService<MangaUpdaterService>(ctx, Manga.tableName to mangaId)
        }

        private var taskCounter = listOf<Long>()

        private val actionToLatest: PendingIntent? = null
        fun setLatestDeepLink(ctx: Context, deepLinkIntent: Intent) {
            TaskStackBuilder.create(ctx).run {
                addNextIntentWithParentStack(deepLinkIntent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }
    }

    private var notificationId = ID.generate()
    private var channelId = ""

    @Volatile
    private lateinit var mServiceLopper: Looper

    @Volatile
    private lateinit var mServiceHandler: ServiceHandler

    private val actionCancelAll by lazy {
        val intent = intentFor<MangaUpdaterService>(this).setAction(ACTION_CANCEL_ALL)
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

    @Inject
    lateinit var chapterDao: ChapterDao

    @Inject
    lateinit var mangaDao: MangaDao

    @Inject
    lateinit var searchDuplicate: SearchDuplicate

    private val default = Dispatchers.Default

    @Inject
    lateinit var manager: SiteCatalogsManager
    private var progress = 0 // Прогресс проверенных манг
    private var error = 0 // Счетчик закончившихся с ошибкой
    private var fullCountNew = 0 // Количество новых глав
    private var mangaName = ""

    private val notificationManager by lazy {
        NotificationManagerCompat.from(this)
    }

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
        mServiceHandler = ServiceHandler(mServiceLopper, this)

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
                // TODO не реагирует на действие
                ACTION_CANCEL_ALL -> {
                    val tempIntent = Intent(actionGet)
                    tempIntent.putExtra(ITEM_NAME, mangaName)
                    tempIntent.putExtra(COUNT_NEW, -1)
                    sendBroadcast(tempIntent)
                    stopSelf()
                }
                else -> {
                    intent.getLongExtra(Manga.tableName, -1).let { task ->
                        if (task != -1L) {
                            taskCounter = taskCounter + task

                            val intentSend = Intent(actionSend)
                            intentSend.putExtra(ITEM_NAME, task)
                            sendBroadcast(intentSend)

                            val msg = mServiceHandler.obtainMessage()
                            msg.arg1 = startId
                            msg.obj = task
                            mServiceHandler.sendMessage(msg)
                        }
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

        actionToLatest?.let { builder.setContentIntent(it) }

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
        if (notify != null) {
            notificationManager.notify(notificationId, notify)
        }

        notificationId = ID.generate()
        stopSelf()
    }

    @WorkerThread
    fun onHandleIntent(mangaId: Long) = runBlocking {
        var countNew = 0
        try {
            // Получение данных из базы данных
            val mangaDB = mangaDao.itemById(mangaId)

            mangaName = mangaDB.name

            showStartNotification(mangaName)

            if (mangaDB.isUpdate.not()) return@runBlocking

//            checkLinkInManga(mangaDB)

            // Получаем список глав из БД
            val oldChapters = withDefaultContext {
                chapterDao.getItemsWhereManga(mangaDB.name)
            }

            /* Обновляем страницы в главах */
            oldChapters.updatePagesInChapters(mangaDB)

            var newChapters = listOf<Chapter>()

            // Получаем список глав из сети
            manager.chapters(mangaDB).let { new ->
                if (oldChapters.isEmpty()) { // Если глав не было до обновления
                    newChapters = new
                } else {
                    new.forEach { chapter ->
                        // Если глава отсутствует в базе данных то добавить
                        if (oldChapters.none { oldChapter -> chapter.link == oldChapter.link }) {
                            newChapters = newChapters + chapter
                        } else {
                            // Иначе обновляем путь
                            val tempChapter =
                                oldChapters.first { oldChapter -> chapter.link == oldChapter.link }
                            tempChapter.path = chapter.path
                            chapterDao.update(tempChapter)
                        }
                    }
                }
            }

            // Если новые главы есть
            if (newChapters.isNotEmpty()) {
                // Разворачиваем список
                newChapters.reversed().forEach { chapter ->
                    // Обновляем страницы и сохраняем
                    if (chapter.pages.isEmpty())
                        chapter.pages = manager.pages(chapter)
                    chapter.isInUpdate = true
                    chapterDao.insert(chapter)
                }
                val oldSize = oldChapters.size

                // Производим поиск дублирующихся глав и очищаем от лишних
                withDefaultContext {
                    searchDuplicate.silentRemoveDuplicate(mangaDB)
                }

                val newSize = chapterDao.getItemsWhereManga(mangaDB.name).size

                // Узнаем сколько было добавленно
                countNew = newSize - oldSize
            } else {
                withDefaultContext {
                    searchDuplicate.silentRemoveDuplicate(mangaDB)
                }
            }
        } catch (ex: Exception) {
            log("manga = $mangaName")
            ex.printStackTrace()
            error++
            countNew = 0
        } finally {
            progress++
            fullCountNew += countNew
            taskCounter = taskCounter - mangaId

            Intent().apply {
                action = actionGet
                putExtra(ITEM_NAME, mangaName)
                putExtra(COUNT_NEW, countNew)

                sendBroadcast(this)
            }
        }
    }

    private suspend fun List<Chapter>.updatePagesInChapters(mangaDB: Manga) =
        withDefaultContext {
            kotlin.runCatching {
                // Отфильтровываем те в которых, либо нет страниц, либо не все страницы
                // либо это альтернативный сайт
                filter {
                    mangaDB.isAlternativeSite
                            || it.pages.isNullOrEmpty()
                            || it.pages.any { chap -> chap.isBlank() }
                }
                    // Получаем список страниц и сохраняем
                    .onEach {
                        launch(default) {
                            it.pages = manager.pages(it)
                        }.join()
                    }
                    .apply {
                        chapterDao.update(*toTypedArray())
                    }
            }.onFailure { it.printStackTrace() }
        }

    private suspend fun checkLinkInManga(mangaDB: Manga) {
        if (mangaDB.shortLink.isEmpty()) {
            val site = manager.getSite(mangaDB.host)

            mangaDB.host = site.host
            mangaDB.shortLink = mangaDB.shortLink

            mangaDao.update(mangaDB)
        } else {
            val site = manager.getSite(mangaDB.host)

            mangaDB.host = site.host

            mangaDao.update(mangaDB)
        }
    }

    private fun showStartNotification(mangaName: String) {
        NotificationCompat.InboxStyle(
            NotificationCompat.Builder(this@MangaUpdaterService, channelId)
                .setContentTitle(getString(R.string.manga_update_notify_searching))
                .setSmallIcon(R.drawable.ic_notification_update)
                .addAction(actionCancelAll)
                .setContentText(mangaName)
        ).run {
            addLine(mangaName)
            addLine(getString(R.string.manga_update_notify_remained, taskCounter.size))
            startForeground(notificationId, build())
        }
    }


    private open class ServiceHandler(
        looper: Looper,
        val service: MangaUpdaterService,
    ) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            service.onHandleIntent(msg.obj as Long)
            service.stopSelf(msg.arg1)
        }
    }
}
