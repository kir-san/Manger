package com.san.kir.background.works

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.hilt.work.HiltWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.san.kir.background.R
import com.san.kir.background.logic.ChapterDownloader
import com.san.kir.background.logic.WorkComplete
import com.san.kir.background.logic.repo.ChapterRepository
import com.san.kir.background.logic.repo.ChapterWorkerRepository
import com.san.kir.background.logic.repo.SettingsRepository
import com.san.kir.background.util.cancelAction
import com.san.kir.core.internet.CellularNetwork
import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.internet.NetworkState
import com.san.kir.core.internet.WifiNetwork
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.ID
import com.san.kir.core.utils.bytesToMb
import com.san.kir.core.utils.formatDouble
import com.san.kir.data.models.base.ChapterTask
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.takeWhile
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@HiltWorker
class DownloadChaptersWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val workerRepository: ChapterWorkerRepository,
    private val chapterRepository: ChapterRepository,
    private val settingsRepository: SettingsRepository,
    private val connectManager: ConnectManager,
    private val cellularNetwork: CellularNetwork,
    private val wifiNetwork: WifiNetwork,
) : BaseUpdateWorker<ChapterTask>(context, params, workerRepository) {

    override val TAG = "Chapter Downloader"

    private var successfuled = listOf<ChapterTask>()
    private var networkState = NetworkState.OK

    override suspend fun work(task: ChapterTask) {
        val loader = ChapterDownloader(
            chapterRepository.chapter(task.chapterId),
            chapterRepository,
            connectManager,
            if (settingsRepository.currentDownload().concurrent) 4 else 1,
            checkNetwork = ::awaitNetwork
        ) { chapter ->
            updateCurrentTask {
                copy(
                    chapterName = chapter.name,
                    max = chapter.pages.size,
                    progress = chapter.downloadPages,
                    size = chapter.downloadSize,
                    time = chapter.downloadTime,
                    state = chapter.status,
                )
            }
            notify()
        }

        awaitNetwork()
        loader.run()
            .onSuccess {
                withCurrentTask { task ->
                    successfuled = successfuled + task
                }
            }
            .onFailure {
                if (settingsRepository.currentDownload().retry) {
                    chapterRepository.addToQueue(task.chapterId)
                } else withCurrentTask { task ->
                    errored = errored + task
                }
            }
    }

    override suspend fun onNotify(task: ChapterTask?) {
        with(NotificationCompat.Builder(applicationContext, channelId)) {
            setSmallIcon(R.drawable.ic_notification_download)

            setContentTitle(applicationContext.getString(R.string.chapters_downloading))

            when (networkState) {
                NetworkState.NOT_WIFI     -> setContentText(applicationContext.getString(R.string.wifi_off))
                NetworkState.NOT_CELLURAR -> setContentText(applicationContext.getString(R.string.internet_off))
                NetworkState.OK           -> task?.let { task ->
                    setContentTitle(
                        applicationContext.getString(R.string.queue_downloading, queue.size)
                    )
                    setContentText(task.chapterName)

                    when (task.state) {
                        DownloadState.LOADING -> {
                            setProgress(task.max, task.progress, false)
                        }

                        else                  -> {
                            setProgress(0, 0, true)
                        }
                    }

                    setSubText(messageToGo)
                } ?: kotlin.run {
                    setContentText(messageToGo)
                }
            }

            actionToDownloads?.let(::setContentIntent)

            priority = NotificationCompat.PRIORITY_DEFAULT

            addAction(applicationContext.cancelAction(id))

            kotlin.runCatching { setForeground(ForegroundInfo(notifyId, build())) }
        }
    }

    override fun finishedNotify(ex: Throwable?) {
        val builder = NotificationCompat.Builder(applicationContext, channelId)
        builder.setSmallIcon(R.drawable.ic_notification_download)

        if (ex is WorkComplete) {
            when {
                successfuled.isEmpty() && errored.isNotEmpty()    -> {
                    builder.setContentTitle(applicationContext.getString(R.string.download_failed))
                    builder.setContentText(applicationContext.getString(R.string.all_chapters_downloaded_with_an_error))
                }

                successfuled.isNotEmpty() && errored.isNotEmpty() -> {
                    builder.setContentTitle(applicationContext.getString(R.string.download_complete_with_error))
                    builder.setContentText(applicationContext.getString(R.string.all_chapters_downloaded_with_an_error))

                    builder.setStyle(
                        NotificationCompat.InboxStyle()
                            .addLine(
                                applicationContext.resources.getQuantityString(
                                    R.plurals.chapters_download_complete_without_errors,
                                    successfuled.size,
                                    successfuled.size
                                )
                            )
                            .addLine(
                                applicationContext.resources.getQuantityString(
                                    R.plurals.chapters_with_errors,
                                    errored.size,
                                    errored.size
                                )
                            )
                            .addLine(sizeAndTime())
                    )
                }

                successfuled.isNotEmpty()                         -> {
                    builder.setContentTitle(applicationContext.getString(R.string.download_complete))
                    builder.setContentText(applicationContext.getString(R.string.enjoy_reading))

                    builder.setStyle(
                        NotificationCompat.InboxStyle()
                            .addLine(
                                applicationContext.resources.getQuantityString(
                                    R.plurals.chapters_download_complete_without_errors,
                                    successfuled.size,
                                    successfuled.size
                                )
                            )
                            .addLine(sizeAndTime())
                    )
                }
            }
        } else {
            builder.setContentTitle(applicationContext.getString(R.string.chapters_downloading_canceled))
            builder.setStyle(
                NotificationCompat.InboxStyle()
                    .addLine(
                        applicationContext.resources.getQuantityString(
                            R.plurals.chapters_download_complete_without_errors,
                            successfuled.size,
                            successfuled.size
                        )
                    )
                    .addLine(sizeAndTime())
            )
        }

        actionToDownloads?.let(builder::setContentIntent)

        notificationManager.cancel(notifyId)
        notificationManager.notify(notifyId + 1, builder.build())
    }

    private fun sizeAndTime(): String {
        val minutes = successfuled.sumOf { it.time } / 1.minutes.inWholeMilliseconds
        return if (minutes < 1)
            applicationContext.getString(
                R.string.download_mb, formatDouble(bytesToMb(successfuled.sumOf { it.size })),
            )
        else
            applicationContext.getString(
                R.string.download_mb_by_min,
                formatDouble(bytesToMb(successfuled.sumOf { it.size })),
                minutes
            )
    }


    private val messageToGo by lazy {
        applicationContext.getString(R.string.press_notify_for_go_to_downloads)
    }

    // Если нет был в наличии, то вернется true
    // Если его включения было необходимо ожидать, то false
    private suspend fun awaitNetwork(): Boolean {
        delay(1.seconds) // Задержка, чтобы успело отработать оповещение от системы, если изменился статус сети
        if (settingsRepository.currentDownload().wifi) {
            if (wifiNetwork.state.value.not()) {
                networkState = NetworkState.NOT_WIFI
                notify()

                wifiNetwork.state.takeWhile { it.not() }.collect()

                networkState = NetworkState.OK
                return false
            }
        } else {
            if (cellularNetwork.state.value.not() || wifiNetwork.state.value.not()) {
                networkState = NetworkState.NOT_CELLURAR
                notify()

                combine(cellularNetwork.state, wifiNetwork.state) { cell, wifi -> cell || wifi }
                    .takeWhile { it.not() }.collect()

                networkState = NetworkState.OK
                return false
            }
        }
        networkState = NetworkState.OK
        return true
    }

    companion object {
        private val notifyId = ID.generate()

        private var actionToDownloads: PendingIntent? = null

        fun setDownloadDeepLink(ctx: Context, deepLinkIntent: Intent) {
            actionToDownloads = TaskStackBuilder.create(ctx).run {
                addNextIntentWithParentStack(deepLinkIntent)
                getPendingIntent(
                    0,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        }
    }
}
