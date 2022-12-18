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
import com.san.kir.background.logic.WorkComplete
import com.san.kir.background.logic.repo.MangaRepository
import com.san.kir.background.logic.repo.MangaWorkerRepository
import com.san.kir.background.util.SearchDuplicate
import com.san.kir.background.util.cancelAction
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.ID
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.MangaTask
import com.san.kir.data.parsing.SiteCatalogsManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import timber.log.Timber

@HiltWorker
class UpdateMangaWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val workerRepository: MangaWorkerRepository,
    private val mangaRepository: MangaRepository,
    private val manager: SiteCatalogsManager,
    private val searchDuplicate: SearchDuplicate,
) : BaseUpdateWorker<MangaTask>(context, params, workerRepository) {

    override val TAG = "Chapter Finder"

    private var successfuled = listOf<MangaTask>()

    override suspend fun work(task: MangaTask) {

        kotlin.runCatching {
            val mangaDb = mangaRepository.manga(task.mangaId)
            if (mangaDb.isUpdate.not()) return

            updateCurrentTask { copy(state = DownloadState.LOADING, mangaName = mangaDb.name) }
            notify()

            // Получаем список глав из БД
            // Обновление страниц для не прочитанных глав
            val oldChapters = mangaRepository.chapters(mangaDb)
            var newChapters = listOf<Chapter>()

            // Получаем список глав из сети
            val new = manager.chapters(mangaDb)
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
                        mangaRepository.update(tempChapter.copy(path = chapter.path))
                    }
                }
            }

            Timber.i("load network chapters -> ${new.size} -> ${new.last()}")

            // Если новые главы есть
            if (newChapters.isNotEmpty()) {
                // Разворачиваем список
                newChapters.reversed().forEach { chapter ->
                    // Обновляем страницы и сохраняем
                    mangaRepository.add(
                        chapter.copy(
                            isInUpdate = true,
                            pages = chapter.pages.ifEmpty { manager.pages(chapter) })
                    )
                }

                // Производим поиск дублирующихся глав и очищаем от лишних
                searchDuplicate.silentRemoveDuplicate(mangaDb)

                val newSize = mangaRepository.chapters(mangaDb).size

                // Узнаем сколько было добавленно
                updateCurrentTask { copy(newChapters = newSize - oldChapters.size) }
            } else {
                searchDuplicate.silentRemoveDuplicate(mangaDb)
            }
        }.onFailure { ex ->
            if (ex is CancellationException) return
            ex.printStackTrace()
            Timber.i("failure work")
            withCurrentTask { task ->
                Timber.v("manga = ${task.mangaName}")
                errored = errored + task
                workerRepository.update(task.copy(state = DownloadState.UNKNOWN))
            }
        }.onSuccess {
            withCurrentTask { task ->
                successfuled = successfuled + task
                workerRepository.update(task.copy(state = DownloadState.COMPLETED))
            }
        }
    }

    override suspend fun onNotify(task: MangaTask?) {
        with(NotificationCompat.Builder(applicationContext, channelId)) {
            setSmallIcon(R.drawable.ic_notification_update)

            setContentTitle(applicationContext.getString(R.string.finding_new_chapters))

            task?.let { task ->
                setContentTitle(
                    applicationContext.getString(R.string.left_check_format, queue.size)
                )

                when (task.state) {
                    DownloadState.PAUSED -> setContentText(
                        applicationContext.getString(
                            R.string.cancel_cancel_finding_for_format, task.mangaName
                        )
                    )

                    else -> setContentText(
                        applicationContext.getString(
                            R.string.finding_for_format, task.mangaName
                        )
                    )
                }

                val max = successfuled.size + queue.size + errored.size
                if (max > 1)
                    setProgress(max, successfuled.size + errored.size, false)
                else
                    setProgress(0, 0, true)

                workerRepository.update(task)

                setSubText(messageToGo)
            } ?: kotlin.run {
                setContentText(messageToGo)
            }

            actionToLatest?.let(::setContentIntent)

            priority = NotificationCompat.PRIORITY_DEFAULT

            addAction(applicationContext.cancelAction(id))

            kotlin.runCatching {
                setForeground(ForegroundInfo(notifyId, build()))
            }
        }
    }

    override fun finishedNotify(ex: Throwable?) {
        val builder = NotificationCompat.Builder(applicationContext, channelId)
        builder.setSmallIcon(R.drawable.ic_notification_update)

        if (ex is WorkComplete) {
            builder.setContentTitle(applicationContext.getString(R.string.chapter_search_completed))

            val message = StringBuilder()

            if (successfuled.size == 1)
                message.appendLine(
                    applicationContext.getString(
                        R.string.checked_for_format, successfuled.first().mangaName
                    )
                )
            else
                message.appendLine(
                    applicationContext.getString(
                        R.string.checked_format, successfuled.size + errored.size
                    )
                )

            val newChapters = successfuled.sumOf { it.newChapters }
            if (newChapters > 0)
                message.appendLine(
                    applicationContext.getString(R.string.new_chapters_format, newChapters)
                )
            else
                message.appendLine(applicationContext.getString(R.string.new_chapters_no_find))

            if (errored.isNotEmpty()) {
                message.append(applicationContext.getString(R.string.with_error_format))
                message.append(errored.joinToString { it.mangaName })
            }

            builder.setStyle(NotificationCompat.BigTextStyle().bigText(message))
        } else {
            builder.setContentTitle(applicationContext.getString(R.string.new_chapter_search_canceled))
        }

        actionToLatest?.let(builder::setContentIntent)

        notificationManager.cancel(notifyId)
        notificationManager.notify(notifyId + 1, builder.build())
    }


    private val messageToGo by lazy {
        applicationContext.getString(R.string.press_notify_for_go_to_latest)
    }

    companion object {
        private val notifyId = ID.generate()

        private var actionToLatest: PendingIntent? = null

        fun setLatestDeepLink(ctx: Context, deepLinkIntent: Intent) {
            actionToLatest = TaskStackBuilder.create(ctx).run {
                addNextIntentWithParentStack(deepLinkIntent)
                getPendingIntent(
                    0,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        }
    }
}
