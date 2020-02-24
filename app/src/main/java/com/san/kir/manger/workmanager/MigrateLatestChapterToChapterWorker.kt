package com.san.kir.manger.workmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.san.kir.ankofork.doFromSdk
import com.san.kir.ankofork.sdk28.notificationManager
import com.san.kir.manger.R
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.LatestChapterRepository
import com.san.kir.manger.utils.ID

class MigrateLatestChapterToChapterWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    private val notificationManager = context.notificationManager
    private var notificationId = ID.generate()

    private val mLatestChapterRepository = LatestChapterRepository(applicationContext)
    private val mChapterRepository = ChapterRepository(applicationContext)

    override suspend fun doWork(): Result {
        val title = applicationContext.getString(R.string.migrate_service_title_update)
        setForeground(createForegroundInfo(title))
        migrate()
        return Result.success()
    }

    private suspend fun migrate() {
        if (mLatestChapterRepository.getItems().isNotEmpty()) {
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
                }
            }.fold(
                onSuccess = {
                    mLatestChapterRepository.deleteAll()
                    val successText =
                        applicationContext.getString(R.string.migrate_service_title_finish)
                    setForeground(createForegroundInfo(successText, false))
                },
                onFailure = {
                    val failureText =
                        applicationContext.getString(R.string.migrate_service_title_error)
                    setForeground(createForegroundInfo(failureText, false))
                }
            )


        }
    }

    private fun createForegroundInfo(title: String, isOngoing: Boolean = true): ForegroundInfo {
        val id = "migrateChannel"

        doFromSdk(Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setSmallIcon(R.drawable.ic_notification_update)
            .setOngoing(isOngoing)
            .build()
        return ForegroundInfo(notificationId, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channelId = "MangaMigrateChannelId"
        val channelName = "MigrateLatestChapterToChapterService"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val mChannel = NotificationChannel(channelId, channelName, importance)
        mChannel.description =
            "Copy information from LatestChapter to Chapter and delete LatestChapter from database"
        notificationManager.createNotificationChannel(mChannel)
    }
}
