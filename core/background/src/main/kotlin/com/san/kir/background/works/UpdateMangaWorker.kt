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
import com.san.kir.core.utils.fuzzy
import com.san.kir.data.chapterRepository
import com.san.kir.data.mangaRepository
import com.san.kir.data.mangaWorkerRepository
import com.san.kir.data.models.main.Chapter
import com.san.kir.data.models.utils.DownloadState
import com.san.kir.data.models.workers.MangaTask
import com.san.kir.data.parsing.SiteCatalogsManager
import com.san.kir.data.parsing.siteCatalogsManager
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import java.util.regex.Pattern

internal class UpdateMangaWorker(
    context: Context,
    params: WorkerParameters,
) : BaseUpdateWorker<MangaTask>(context, params) {

    private val mangaRepository = ManualDI.mangaRepository()
    private val chaptersRepository = ManualDI.chapterRepository()
    private val manager: SiteCatalogsManager = ManualDI.siteCatalogsManager()

    override val workerRepository = ManualDI.mangaWorkerRepository()
    override val TAG = "Chapter Finder"

    private val reg = Pattern.compile("\\d+")
    private var successfuled = listOf<MangaTask>()

    override suspend fun prepareTasks(new: List<MangaTask>): List<Long> {
        val unicsTasks = new.fold(listOf<MangaTask>()) { list, item ->
            if (item.mangaId in list.map { it.mangaId }) list else list + item
        }

        workerRepository.remove((new - unicsTasks.toSet()).map { it.id })

        return super.prepareTasks(unicsTasks)
    }

    override suspend fun work(task: MangaTask) {

        kotlin.runCatching {
            val mangaDb = mangaRepository.item(task.mangaId)
            if (mangaDb == null || mangaDb.isUpdate.not()) return

            updateCurrentTask { copy(state = DownloadState.LOADING, mangaName = mangaDb.name) }
            notify()

            val dbChapters = chaptersRepository.allItems(mangaDb.id)
            val siteChapters = manager.chapters(mangaDb)

            val result = compare(dbChapters, siteChapters.asReversed())
            /** Все главы из списка оставшихся удаляем из БД */
            if (result.remains.isNotEmpty()) chaptersRepository.delete(result.remains.map { it.id })

            /** Находим первую главу из новых */
            val firstNew = result.prepared.indexOfFirst { it.isNew }

            /** Все главы до первой новой, обновляем в БД */
            if (firstNew == -1) {
                chaptersRepository.save(result.prepared.map { it.ch })
            } else {
                val take = result.prepared.take(firstNew)
                chaptersRepository.save(take.map { it.ch })

                /** Все главы, что остались */
                val new = result.prepared.subList(firstNew, result.prepared.size)

                /** Находим и удаляем главы, которые были в БД, чтобы не нарушать порядок добавления */
                val oldInNew = new.filter { it.isNew.not() }
                if (oldInNew.isNotEmpty()) chaptersRepository.delete(oldInNew.map { it.ch.id })

                /** Сохраняем все оставшиеся главы в БД */
                chaptersRepository.save(new.distinct().map { it.ch.copy(id = 0) })

                updateCurrentTask { copy(newChapters = new.size - oldInNew.size) }
            }
        }.onFailure { ex ->
            if (ex is CancellationException) return
            ex.printStackTrace()
            Timber.i("failure work")
            withCurrentTask { task ->
                Timber.v("manga = ${task.mangaName}")
                errored = errored + task
                workerRepository.save(task.copy(state = DownloadState.UNKNOWN))
            }
        }.onSuccess {
            withCurrentTask { task ->
                successfuled = successfuled + task
                workerRepository.save(task.copy(state = DownloadState.COMPLETED))
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

                workerRepository.save(task)

                setSubText(messageToGo)
            } ?: kotlin.run {
                setContentText(messageToGo)
            }

            actionToLatest?.let(::setContentIntent)

            priority = NotificationCompat.PRIORITY_DEFAULT

            addAction(applicationContext.cancelAction(id))

            kotlin.runCatching { setForeground(notifyId, build()) }
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

    /**
     * Слияние двух списков глав, в один
     * Новый список считается приоритетным
     *
     * @return Результат слияния
     * */
    private fun compare(old: List<Chapter>, new: List<Chapter>): ChaptersContainer {
        val oldRemovable = old.toMutableList()

        val prepared = new.map { newChapter ->
            /**
             * Поиск главы в старом списке
             * Правильной главой считается первая найденная глава в которой
             *      Совпадают ссылки на страницу главы
             *  или Совпадают все найденные цифры в названии глав
             *  или Нечеткое сравнение названий двух строк выше 75%
             * */
            val oldChapter = oldRemovable.firstOrNull { oldChapter ->
                newChapter.link.equals(oldChapter.link, true)
                        || findNumbers(newChapter.name) == findNumbers(oldChapter.name)
                        || (newChapter.name fuzzy oldChapter.name).first > 0.75
            }

            /** Если глава была найдена, то удаляем ее из старого списка для уменьшения количества итераций */
            if (oldChapter != null) oldRemovable -= oldChapter

            /** Если глова была найдена, то обновляем ее значениями из новой главы, иначе используем новую */
            val chapter = oldChapter?.copy(
                link = newChapter.link,
                _path = newChapter.path,
                name = newChapter.name,
                date = newChapter.date,
            ) ?: newChapter.copy(isInUpdate = true)
            /** Объединение главы и ее статуса наличия в старой главе */
            ChapterContainer(chapter, oldChapter == null)
        }

        /** Возвращаем весь подготовленный список и все значения из старого списка которые остались */
        return ChaptersContainer(prepared, oldRemovable)
    }

    private fun findNumbers(name: String): Long {
        val matcher1 = reg.matcher(name)
        var numbers1 = listOf<String>()

        while (matcher1.find()) {
            numbers1 = numbers1 + matcher1.group()
        }

        return numbers1.ifEmpty { listOf("0") }.joinToString(separator = "").toLong()
    }

    companion object {
        private val notifyId = ID.generate()

        private var actionToLatest: PendingIntent? = null

        fun setLatestDeepLink(deepLinkIntent: Intent) {
            actionToLatest = TaskStackBuilder.create(ManualDI.application).run {
                addNextIntentWithParentStack(deepLinkIntent)
                getPendingIntent(
                    0,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        }
    }
}

private data class ChaptersContainer(
    val prepared: List<ChapterContainer>,
    val remains: List<Chapter>,
)

private data class ChapterContainer(val ch: Chapter, val isNew: Boolean)
