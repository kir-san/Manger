package com.san.kir.manger.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.san.kir.ankofork.sdk28.notificationManager
import com.san.kir.manger.R
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.LatestChapterRepository
import com.san.kir.manger.utils.ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MigrateLatestChapterToChapterService : Service(), CoroutineScope {
    override fun onBind(intent: Intent?) = null

    lateinit var job: Job

    private val mLatestChapterRepository = LatestChapterRepository(this)
    private val mChapterRepository = ChapterRepository(this)

    private var channelId = "migrateChannel"
    private var notificationId = ID.generate()

    override fun onCreate() {
        super.onCreate()

        job = Job()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()

            with(NotificationCompat.Builder(this, channelId)) {
                setSmallIcon(R.mipmap.icon_launcher)
                startForeground(notificationId, build())
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        launch(coroutineContext) {
            if (mLatestChapterRepository.getItems().isNotEmpty()) {

                with(
                    NotificationCompat.Builder(
                        this@MigrateLatestChapterToChapterService, channelId
                    )
                ) {
                    setContentTitle(getString(R.string.migrate_service_title_update))
                    setSmallIcon(R.drawable.ic_notification_update)
                    setProgress(0, 0, true)
                    startForeground(notificationId, build())
                }

                val items = mChapterRepository.getItems()

                kotlin.runCatching {
                    items.forEachIndexed { index, chapter ->
                        val tempList = mLatestChapterRepository
                            .getItems()
                            .filter { it.manga == chapter.manga }
                            .filter { it.name == chapter.name }

                        when {
                            tempList.isEmpty() -> {
                                if (!chapter.isInUpdate) {
                                    chapter.isInUpdate = false
                                    mChapterRepository.update(chapter)
                                }
                            }
                            tempList.size == 1 -> {
                                val latestChapter = tempList.first()
                                chapter.isInUpdate = true
                                mChapterRepository.update(chapter)
                                mLatestChapterRepository.delete(latestChapter)
                            }
                            else -> throw Exception("For ${chapter.manga} - ${chapter.name} founded more than one item")
                        }

                        with(
                            NotificationCompat.Builder(
                                this@MigrateLatestChapterToChapterService, channelId
                            )
                        ) {
                            setContentTitle(getString(R.string.migrate_service_title_update))
                            setSmallIcon(R.drawable.ic_notification_update)
                            setProgress(items.size, index, false)
                            startForeground(notificationId, build())
                        }
                    }
                }.onFailure {
                    stopForeground(false)
                    notificationManager.cancel(notificationId)

                    with(
                        NotificationCompat.Builder(
                            this@MigrateLatestChapterToChapterService, channelId
                        )
                    ) {
                        setContentTitle(getString(R.string.migrate_service_title_error))
                        setSmallIcon(R.drawable.ic_notification_update)
                        notificationManager.notify(notificationId, build())
                    }
                }

                mLatestChapterRepository.deleteAll()

                stopForeground(false)
                notificationManager.cancel(notificationId)

                with(
                    NotificationCompat.Builder(
                        this@MigrateLatestChapterToChapterService, channelId
                    )
                ) {
                    setContentTitle(getString(R.string.migrate_service_title_finish))
                    setSmallIcon(R.drawable.ic_notification_update)
                    notificationManager.notify(notificationId, build())
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
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

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        private const val TAG = "MigrateLatestChapterToChapterService"
    }
}
