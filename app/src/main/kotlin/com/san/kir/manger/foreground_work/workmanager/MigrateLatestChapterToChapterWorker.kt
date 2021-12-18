package com.san.kir.manger.foreground_work.workmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.san.kir.core.utils.log
import com.san.kir.manger.R
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.LatestChapterRepository
import com.san.kir.manger.utils.ID

class MigrateLatestChapterToChapterWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    private val notificationManager by lazy {
        NotificationManagerCompat.from(context)
    }
    private var notificationId = ID.generate()
    private val channelId = "MangaMigrateChannelId"
    private val channelTitle = "MigrateLatestChapterToChapterService"

    private val mLatestChapterRepository = LatestChapterRepository(applicationContext)
    private val mChapterRepository = ChapterRepository(applicationContext)

    override suspend fun doWork(): Result {
        val title = applicationContext.getString(R.string.migrate_service_title_update)
        log("doWork")
        setForeground(createForegroundInfo(title))
        log("migrate")
        migrate()
        return Result.success()
    }

    private suspend fun migrate() {
        if (mLatestChapterRepository.getItems().isNotEmpty()) {
            val items = mChapterRepository.getItems()

            kotlin.runCatching {
                items.forEachIndexed { _, chapter ->
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
                    log("success")
                    mLatestChapterRepository.deleteAll()
                    val successText =
                        applicationContext.getString(R.string.migrate_service_title_finish)
                    setForeground(createForegroundInfo(successText, false))
                    notificationManager.cancel(notificationId)
                },
                onFailure = {
                    log("failure")
                    val failureText =
                        applicationContext.getString(R.string.migrate_service_title_error)
                    setForeground(createForegroundInfo(failureText, false))
                }
            )
        }
    }

    private fun createForegroundInfo(title: String, isOngoing: Boolean = true): ForegroundInfo {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setTicker(title)
            .setSmallIcon(R.drawable.ic_notification_update)
            .setOngoing(isOngoing)
            .build()
        return ForegroundInfo(notificationId, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val importance = NotificationManager.IMPORTANCE_MIN
        var mChannel = notificationManager.getNotificationChannel(channelId)
        if (mChannel == null) {
            mChannel = NotificationChannel(channelId, channelTitle, importance)
            mChannel.description =
                "Copy information from LatestChapter to Chapter and delete LatestChapter from database"
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    companion object {
        const val tag = "migrate latest chapters"

        fun addTask(ctx: Context): Operation {
            val task =
                OneTimeWorkRequestBuilder<MigrateLatestChapterToChapterWorker>()
                    .addTag(tag)
                    .build()
            return WorkManager.getInstance(ctx).enqueue(task)
        }
    }
}
