package com.san.kir.chapters.ui.latest

import android.content.Context
import com.san.kir.background.logic.DownloadChaptersManager
import com.san.kir.background.logic.di.downloadChaptersManager
import com.san.kir.background.works.LatestClearWorkers
import com.san.kir.chapters.R
import com.san.kir.core.utils.ManualDI
import com.san.kir.core.utils.viewModel.Action
import com.san.kir.core.utils.viewModel.ViewModel
import com.san.kir.data.chapterRepository
import com.san.kir.data.db.main.repo.ChapterRepository
import com.san.kir.data.models.base.addedTime
import com.san.kir.data.models.base.canDelete
import com.san.kir.data.models.main.SimplifiedChapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import java.util.Locale
import kotlin.math.min

@OptIn(ExperimentalCoroutinesApi::class)
internal class LatestViewModel(
    private val context: Context = ManualDI.application,
    private val chaptersRepository: ChapterRepository = ManualDI.chapterRepository(),
    private val downloadManager: DownloadChaptersManager = ManualDI.downloadChaptersManager(),
) : ViewModel<LatestState>(), LatestStateHolder {

    private val monthFormat = LocalDate.Format { monthName(MonthNames.ENGLISH_FULL) }
    private val monthYearFormat = LocalDate.Format { monthName(MonthNames.ENGLISH_FULL); char(' '); year() }

    private val hasBackground = MutableStateFlow(true)
    private val newItems = chaptersRepository.simplifiedItems.mapLatest { list -> list.filter { it.isRead.not() } }
    private val readItems = chaptersRepository.simplifiedItems.mapLatest { list -> list.count { it.isRead } }
    private val downloadedItems = chaptersRepository.simplifiedItems.mapLatest { list -> list.count { it.canDelete } }

    override val items = MutableStateFlow<List<DateContainer>>(emptyList())
    override val selection = MutableStateFlow(SelectionState())

    private var job: Job? = null

    override val tempState = combine(
        chaptersRepository.latestCount,
        newItems.onEach { runWorkersObserver() },
        hasBackground,
        readItems,
        downloadedItems,
    ) { all, newItems, background, read, downloaded ->
        LatestState(
            newChapters = newItems.size,
            hasBackgroundWork = background,
            itemsSize = all,
            readSize = read,
            downloadedSize = downloaded
        )
    }
    override val defaultState = LatestState()

    init {
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val nearestWeekDay = 7 - (currentDate.dayOfWeek.isoDayNumber - 1)
        val nearestMonthDay = currentDate.daysUntil(
            LocalDate(
                year = currentDate.year,
                monthNumber = min(currentDate.monthNumber + 1, 12),
                dayOfMonth = 1
            )
        )

        val nearestYearDay = currentDate.daysUntil(
            LocalDate(year = currentDate.year + 1, monthNumber = 1, dayOfMonth = 1)
        )

        chaptersRepository.simplifiedItems
            .onEach {
                val groupedItems = it.groupBy { chapter ->

                    val diff = chapter.addedTime.daysUntil(currentDate)
                    when {
                        chapter.addedTimestamp == 0L -> context.getString(R.string.long_ago)
                        diff == 0 -> context.getString(R.string.today)
                        diff == 1 -> context.getString(R.string.yesterday)
                        diff <= nearestWeekDay -> context.getString(R.string.this_week)
                        diff <= nearestMonthDay -> context.getString(R.string.this_month)
                        diff <= nearestYearDay -> chapter.addedTime.format(monthFormat)
                        else -> chapter.addedTime.format(monthYearFormat)
                    }
                }.map { (date, chapters) ->
                    DateContainer(
                        date.replaceFirstChar { char -> char.titlecase(Locale.getDefault()) },
                        chapters
                            .groupBy(SimplifiedChapter::manga)
                            .map { (manga, chapters) -> MangaContainer(manga, chapters) }
                    )
                }
                items.value = groupedItems
                Timber.tag("LatestViewModel").i("NEW ITEMS ${it.size}")
            }
            .flowOn(Dispatchers.Default)
            .launch()
    }

    override suspend fun onAction(action: Action) {
        when (action) {
            LatestAction.CleanAll -> LatestClearWorkers.clearAll()
            LatestAction.CleanDownloaded -> LatestClearWorkers.clearDownloaded()
            LatestAction.CleanRead -> LatestClearWorkers.clearRead()
            LatestAction.DownloadNew -> downloadNewChapters()
            LatestAction.RemoveSelected -> removeSelected()
            LatestAction.DownloadSelected -> downloadSelected()
            LatestAction.UnselectAll -> unselect()
            LatestAction.SelectAll -> select()
            is LatestAction.ChangeSelect -> changeSelect(action.itemIds)
            is LatestAction.StartDownload -> downloadManager.addTask(action.id)
            is LatestAction.StopDownload -> downloadManager.removeTask(action.id)
        }
    }

    private suspend fun downloadNewChapters() {
        downloadManager.addTasks(newItems.first().map { it.id })
        unselect()
    }

    private suspend fun removeSelected() {
        chaptersRepository.updateIsInUpdate(selection.value.selections, false)
        unselect()
    }

    private suspend fun downloadSelected() {
        downloadManager.addTasks(selection.value.selections)
        unselect()
    }

    private fun changeSelect(ids: List<Long>) {
        selection.update { old ->
            if (old.hasItems(ids)) {
                old.copy(selections = old.selections - ids.toSet())
            } else {
                old.copy(selections = old.selections + ids)
            }
        }
        Timber.tag("LatestViewModel").i("NEW SELECTION ${selection.value}")
    }

    private fun select() {
        selection.value = SelectionState(items.value.flatMap { it.chaptersIds }.toSet())
        Timber.tag("LatestViewModel").i("NEW ITEMS ${selection.value}")
    }

    private fun unselect() {
        selection.value = SelectionState()
        Timber.tag("LatestViewModel").i("NEW ITEMS ${selection.value}")
    }

    private fun runWorkersObserver() {
        if (job?.isActive == true) return
        hasBackground.value = false
        job = LatestClearWorkers.workInfos()
            .onEach { works -> hasBackground.value = works.any { it.state.isFinished.not() } }
            .launchIn(this)
    }
}
