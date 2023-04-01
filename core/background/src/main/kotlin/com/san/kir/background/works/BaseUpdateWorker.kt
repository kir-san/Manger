package com.san.kir.background.works

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.san.kir.background.logic.Command
import com.san.kir.background.logic.WorkComplete
import com.san.kir.background.logic.repo.BaseWorkerRepository
import com.san.kir.background.util.tryCreateNotificationChannel
import com.san.kir.data.models.base.BaseTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

open class BaseUpdateWorker<T : BaseTask<T>>(
    context: Context,
    params: WorkerParameters,
    private val workerRepository: BaseWorkerRepository<T>,
) : CoroutineWorker(context, params) {
    protected val notificationManager = NotificationManagerCompat.from(applicationContext)
    protected val channelId = "${this::class.java.simpleName}Id"
    protected open val TAG = "${this::class.java.simpleName}Tag"

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var mainJob: Job? = null

    protected var queue = listOf<T>()
        private set

    protected var errored = listOf<T>()
    private val lock = Mutex()

    private var currentJob: Job? = null
    private var currentTask: T? = null

    override suspend fun doWork(): Result {
        applicationContext.tryCreateNotificationChannel(channelId, TAG)
        notify()

        coroutineScope {
            mainJob = launch {
                workerRepository.catalog.collect(::control)
            }

            mainJob?.invokeOnCompletion { ex ->
                scope.launch {
                    removeTasks()
                    Timber.i("finishedNotify -> $ex")
                    finishedNotify(ex)
                    scope.cancel()
                }
            }

            mainJob?.join()
        }

        return Result.success()
    }

    private suspend fun control(new: List<T> = queue) {
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

    /* Возможные состояния
    * hasRunningTask = false and new.isEmpty -> DESTROY
    * hasRunningTask = false and new.isNotEmpty -> START
    * hasRunningTask = true and new.isEmpty -> STOP
    * hasRunningTask = true and new.isNotEmpty and currentTask in new -> NONE
    * hasRunningTask = true and new.isNotEmpty and currentTask in new and new.size != queue.size -> UPDATE
    * hasRunningTask = true and new.isNotEmpty and currentTask not in new -> STOP
    * */
    private suspend fun findNewCommand(new: List<T>): Command {
        val newIds = prepareTasks(new)

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

    private suspend fun start() {
        Timber.i("start")

        if (currentJob != null
            && currentTask != null
            && currentJob?.isActive == true
            && queue.isNotEmpty()) return

        currentJob = scope.launch {
            currentTask = queue.first()
            Timber.i("createJob -> ${queue.first()}")
            work(queue.first())
            removeTask()
            control()
        }
    }

    private suspend fun stopTask() {
        Timber.i("stop -> $currentTask")

        updateCurrentTask { setPaused() }

        notify()

        if (currentJob == null) return
        currentJob?.cancelAndJoin()

        removeTask()

        Timber.i("stopped -> $currentTask")
    }

    private suspend fun removeTask() {
        Timber.i("removeTask -> $currentTask")

        val task = currentTask ?: return
        queue.firstOrNull { it.id == task.id }?.let { queue = queue - it }
        workerRepository.remove(task)

        currentJob = null
        currentTask = null
    }

    private suspend fun removeTasks() {
        Timber.i("removeTasks")
        val tasks = workerRepository.catalog.first()
        workerRepository.clear()
        currentJob = null
        currentTask = null

        onRemoveAllTasks(tasks)
    }

    private fun hasRunningTask() = currentJob != null && currentJob?.isActive == true

    protected open suspend fun prepareTasks(new: List<T>): List<Long> = new.map { it.id }

    protected fun updateCurrentTask(task: T.() -> T) {
        currentTask = currentTask?.task()
    }

    protected suspend fun withCurrentTask(action: suspend (T) -> Unit) {
        currentTask?.let { action(it) }
    }

    protected suspend fun notify() {
        Timber.i("notify")
        onNotify(currentTask)
    }

    protected open suspend fun onNotify(task: T?) {}
    protected open fun finishedNotify(ex: Throwable?) {}
    protected open suspend fun work(task: T) {}
    protected open suspend fun onRemoveAllTasks(tasks: List<T>) {}

}
