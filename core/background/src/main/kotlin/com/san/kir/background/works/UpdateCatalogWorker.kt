package com.san.kir.background.works

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.work.WorkerParameters
import com.san.kir.background.R
import com.san.kir.background.logic.WorkComplete
import com.san.kir.background.util.cancelAction
import com.san.kir.core.utils.ID
import com.san.kir.core.utils.ManualDI
import com.san.kir.data.catalogWorkerRepository
import com.san.kir.data.catalogsRepository
import com.san.kir.data.models.catalog.SiteCatalogElement
import com.san.kir.data.models.utils.DownloadState
import com.san.kir.data.models.workers.CatalogTask
import com.san.kir.data.parsing.siteCatalogsManager
import kotlinx.coroutines.flow.collectIndexed
import timber.log.Timber

internal class UpdateCatalogWorker(
    context: Context,
    params: WorkerParameters,
) : BaseUpdateWorker<CatalogTask>(context, params) {

    private val manager = ManualDI.siteCatalogsManager()
    private val catalogsRepository = ManualDI.catalogsRepository()

    override val workerRepository = ManualDI.catalogWorkerRepository()
    override val TAG = "Catalogs Updater"

    override suspend fun work(task: CatalogTask) {
        updateCurrentTask { copy(progress = 0f, state = DownloadState.QUEUED) }
        notify()

        val buffer = mutableListOf<SiteCatalogElement>()
        var fullCount = 0
        var lastSavedPercent = 0
        kotlin.runCatching {
            val site = manager.catalog(task.name)
            val catalogName = manager.catalogName(site.name)
            site.init()

            var retry = 3
            while (retry != 0) {
                retry--

                buffer.clear()

                updateCurrentTask { copy(progress = 0f, state = DownloadState.LOADING) }
                notify()

                site.catalog()
                    .collectIndexed { index, value ->
                        val new = index / site.volume.toFloat()
                        val percents = (new * 100).toInt()

                        buffer.add(value)
                        fullCount++
                        withCurrentTask { t ->
                            if (percents > (t.progress * 100).toInt()) {
                                updateCurrentTask { copy(progress = new) }
                                notify()
                                Timber.v("task -> ${task.name} / $index / ${site.volume}")
                            }
                        }

                        if (percents % 5 == 0 && percents > lastSavedPercent) {
                            catalogsRepository.save(catalogName, buffer)
                            Timber.v("$percents save items(${buffer.size}) in db")
                            buffer.clear()
                            lastSavedPercent = percents
                        }
                    }
                if (buffer.isNotEmpty()) {
                    catalogsRepository.save(catalogName, buffer)
                }
                if (fullCount >= site.volume - 10) break
            }

            Timber.v("update finish. elements getting $fullCount")

            withCurrentTask { t ->
                updateCurrentTask { copy(state = DownloadState.COMPLETED) }
                notify()
            }
        }.onFailure {
            errored = errored + task
            Timber.e(it)
        }
    }

    override suspend fun onNotify(task: CatalogTask?) {
        with(NotificationCompat.Builder(applicationContext, channelId)) {
            setSmallIcon(R.drawable.ic_notification_update)

            setContentTitle(applicationContext.getString(R.string.catalogs_updating_format, queue.size))

            task?.let { task ->
                when (task.state) {
                    DownloadState.LOADING -> {
                        val percent = (task.progress * 100).toInt()
                        setContentText("${task.name}  ${percent}%")
                        setProgress(100, percent, false)
                    }

                    DownloadState.QUEUED -> {
                        setContentText(
                            applicationContext.getString(R.string.prepare_catalog_for_loading_format, task.name)
                        )
                        setProgress(0, 0, true)
                    }

                    DownloadState.PAUSED -> {
                        setContentText(
                            applicationContext.getString(R.string.cancel_loading_catalog_format, task.name)
                        )
                        setProgress(0, 0, true)
                    }

                    DownloadState.COMPLETED -> {
                        setContentText(applicationContext.getString(R.string.saving_catalog_format, task.name))
                        setProgress(0, 0, true)
                    }

                    else -> {}
                }

                workerRepository.save(task)

                setSubText(messageToGo)
            } ?: kotlin.run {
                setContentText(messageToGo)
            }

            actionGoToCatalogs?.let {
                setContentIntent(it)
            }

            priority = NotificationCompat.PRIORITY_DEFAULT

            addAction(applicationContext.cancelAction(id))

            kotlin.runCatching { setForeground(notifyId, build()) }
        }
    }

    override fun finishedNotify(ex: Throwable?) {
        with(NotificationCompat.Builder(applicationContext, channelId)) {
            setSmallIcon(R.drawable.ic_notification_update)

            if (ex is WorkComplete) {
                if (errored.isEmpty()) {
                    setContentTitle(applicationContext.getString(R.string.complete_catalog_update))
                } else {
                    setContentTitle(applicationContext.getString(R.string.updating_catalogs_completed_with_error))
                    setStyle(NotificationCompat.BigTextStyle().bigText(errored.joinToString { it.name }))
                }
            } else {
                setContentTitle(applicationContext.getString(R.string.updating_catalogs_canceled))
            }

            actionGoToCatalogs?.let {
                setContentIntent(it)
            }

            notificationManager.cancel(notifyId)
            notificationManager.notify(notifyId + 1, build())
        }
    }

    private val messageToGo by lazy {
        applicationContext.getString(R.string.press_notify_for_go_to_catalogs)
    }

    companion object {
        private val notifyId = ID.generate()

        private var actionGoToCatalogs: PendingIntent? = null

        fun setLatestDeepLink(deepLinkIntent: Intent) {
            actionGoToCatalogs = TaskStackBuilder.create(ManualDI.application).run {
                addNextIntentWithParentStack(deepLinkIntent)
                getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }
    }
}
