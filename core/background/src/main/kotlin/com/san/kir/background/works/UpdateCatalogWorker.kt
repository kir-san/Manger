package com.san.kir.background.works

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.san.kir.background.R
import com.san.kir.background.logic.repo.CatalogRepository
import com.san.kir.background.logic.repo.WorkersRepository
import com.san.kir.background.util.cancelAction
import com.san.kir.background.util.tryCreateNotificationChannel
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.ID
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.models.base.CatalogTask
import com.san.kir.data.models.base.SiteCatalogElement
import com.san.kir.data.parsing.SiteCatalogsManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.concurrent.CancellationException

@HiltWorker
class UpdateCatalogWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val manager: SiteCatalogsManager,
    private val workersRepository: WorkersRepository,
    private val catalogRepository: CatalogRepository,
) : CoroutineWorker(context, params) {
    private val notificationManager = NotificationManagerCompat.from(applicationContext)
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var mainJob: Job? = null

    private var queue = listOf<CatalogTask>()
    private var errored = listOf<CatalogTask>()
    private val lock = Mutex()

    private var currentJob: Job? = null
    private var currentTask: CatalogTask? = null

    override suspend fun doWork(): Result {
        applicationContext.tryCreateNotificationChannel(channelId, TAG)
        notify()

        coroutineScope {
            mainJob = launch {
                workersRepository.catalog.collect(::control)
            }

            mainJob?.invokeOnCompletion { ex ->
                scope.launch {
                    removeTask()
                    finishedNotify(ex)
                    scope.cancel()
                }
            }

            mainJob?.join()
        }

        return Result.success()
    }

    /* Возможные состояния
    * hasRunningTask = false and new.isEmpty -> DESTROY
    * hasRunningTask = false and new.isNotEmpty -> START
    * hasRunningTask = true and new.isEmpty -> STOP
    * hasRunningTask = true and new.isNotEmpty and currentTask in new -> NONE
    * hasRunningTask = true and new.isNotEmpty and currentTask in new and new.size != queue.size -> UPDATE
    * hasRunningTask = true and new.isNotEmpty and currentTask not in new -> STOP
    * */
    private suspend fun findNewCommand(new: List<CatalogTask>): Command {
        val newIds = new.map { it.id }

        return lock.withLock {
            val task = currentTask

            when {
                hasRunningTask() -> when {
                    new.isEmpty() || task == null || !newIds.contains(task.id) -> Command.Stop
                    queue.size != new.size -> Command.Update
                    else -> Command.None
                }

                new.isNotEmpty() -> Command.Start
                else -> Command.Destroy
            }
        }.apply { queue = new }
    }

    private suspend fun control(new: List<CatalogTask> = queue) {
        val command = findNewCommand(new)
        Timber.v("control -> $command -> $new")

        when (command) {
            Command.Destroy -> mainJob?.cancel(WorkComplete)
            Command.Stop -> {
                stopTask()
                control()
            }

            Command.Start -> start()
            Command.Update -> notify()
            else -> {}
        }
    }

    private suspend fun start() {
        Timber.i("start")
        lock.withLock {
            if (currentJob != null && currentTask != null && currentJob?.isActive == true) return

            currentJob = scope.launch {
                updateCatalog(queue.first())
                removeTask()
                control()
            }
        }
    }

    private suspend fun stopTask() {
        Timber.i("stop -> $currentTask")

        lock.withLock {
            currentTask?.let {
                currentTask = it.copy(state = DownloadState.PAUSED)
            }
        }

        notify()

        if (currentJob == null) return

        currentJob?.cancelAndJoin()

        removeTask()

        Timber.i("stopped -> $currentTask")
    }

    private suspend fun removeTask() {
        Timber.i("removeTask -> $currentTask")
        lock.withLock {
            val task = currentTask ?: return
            queue.firstOrNull { it.id == task.id }?.let { queue = queue - it }
            workersRepository.remove(task)

            currentJob = null
            currentTask = null
        }
    }

    private fun hasRunningTask() = currentJob != null && currentJob?.isActive == true

    private suspend fun updateCatalog(task: CatalogTask) {
        Timber.i("createJob -> $task")

        lock.withLock {
            currentTask = task.copy(progress = 0f, state = DownloadState.QUEUED)
        }
        notify()

        val site = manager.catalog.first { it.name == task.name }
        site.init()

        val tempList = mutableListOf<SiteCatalogElement>()
        kotlin.runCatching {
            var retry = 3
            while (retry != 0) {
                retry--

                tempList.clear()

                lock.withLock {
                    currentTask = task.copy(progress = 0f, state = DownloadState.LOADING)
                }
                notify()

                site.catalog()
                    .collectIndexed { index, value ->
                        val new = index / site.volume.toFloat()

                        currentTask?.let { t ->
                            if ((new * 100).toInt() > (t.progress * 100).toInt()) {
                                lock.withLock {
                                    currentTask = t.copy(progress = new)
                                }
                                notify()
                                Timber.v("task -> ${task.name} / $index / ${site.volume}")
                            }
                        }

                        tempList.add(value)
                    }
                if (tempList.size >= site.volume - 10) break
            }

            Timber.v("update finish. elements getting ${tempList.size}")

            currentTask?.let { t ->
                lock.withLock {
                    currentTask = t.copy(state = DownloadState.COMPLETED)
                }
                notify()
            }

            catalogRepository.save(task.name, tempList)

            Timber.v("save items in db")
        }.onFailure {
            errored = errored + task
            Timber.e(it)
        }
    }

    private suspend fun notify() {
        Timber.i("notify")

        with(NotificationCompat.Builder(applicationContext, channelId)) {
            setSmallIcon(R.drawable.ic_notification_update)

            setContentTitle(
                applicationContext.getString(R.string.catalog_fos_service_notify_title, queue.size)
            )

            currentTask?.let { task ->
                when (task.state) {
                    DownloadState.LOADING -> {
                        val percent = (task.progress * 100).toInt()
                        setContentText("${task.name}  ${percent}%")
                        setProgress(100, percent, false)
                    }

                    DownloadState.QUEUED -> {
                        setContentText(
                            applicationContext.getString(
                                R.string.catalog_loading_text, task.name
                            )
                        )
                        setProgress(0, 0, true)
                    }

                    DownloadState.PAUSED -> {
                        setContentText(
                            applicationContext.getString(
                                R.string.catalog_paused_text, task.name
                            )
                        )
                        setProgress(0, 0, true)
                    }

                    DownloadState.COMPLETED -> {
                        setContentText(
                            applicationContext.getString(
                                R.string.catalog_saving_text, task.name
                            )
                        )
                        setProgress(0, 0, true)
                    }

                    DownloadState.UNKNOWN -> {}
                }

                withIoContext {
                    workersRepository.update(task)
                }

                setSubText(messageToGo)
            } ?: kotlin.run {
                setContentText(messageToGo)
            }

            actionGoToCatalogs?.let {
                setContentIntent(it)
            }

            priority = NotificationCompat.PRIORITY_DEFAULT

            addAction(applicationContext.cancelAction(id))

            kotlin.runCatching {
                setForeground(ForegroundInfo(notifyId, build()))
            }
        }
    }

    private fun finishedNotify(ex: Throwable?) {
        Timber.i("finishedNotify -> $ex")
        with(NotificationCompat.Builder(applicationContext, channelId)) {
            setSmallIcon(R.drawable.ic_notification_update)

            if (ex is WorkComplete) {
                if (errored.isEmpty()) {
                    setContentTitle(applicationContext.getString(R.string.catalog_complete))
                } else {
                    setContentTitle(applicationContext.getString(R.string.catalog_fos_service_notify_error_title))
                    setStyle(
                        NotificationCompat.BigTextStyle().bigText(errored.joinToString { it.name })
                    )
                }
            } else {
                setContentTitle(applicationContext.getString(R.string.catalog_fos_service_notify_manual_stop_title))
            }

            actionGoToCatalogs?.let {
                setContentIntent(it)
            }

            notificationManager.cancel(notifyId)
            notificationManager.notify(notifyId + 1, build())
        }
    }

    private val messageToGo by lazy {
        applicationContext.getString(R.string.catalog_fos_service_message)
    }

    companion object {
        private val channelId = "${UpdateCatalogWorker::class.simpleName}Id"
        private val TAG = "${UpdateCatalogWorker::class.simpleName}Tag"

        private val notifyId = ID.generate()

        private var actionGoToCatalogs: PendingIntent? = null

        fun setLatestDeepLink(ctx: Context, deepLinkIntent: Intent) {
            actionGoToCatalogs = TaskStackBuilder.create(ctx).run {
                addNextIntentWithParentStack(deepLinkIntent)
                getPendingIntent(
                    0,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        }
    }
}

private data object WorkComplete : CancellationException()

private sealed interface Command {
    data object None : Command
    data object Stop : Command
    data object Start : Command
    data object Destroy : Command
    data object Update : Command
}
