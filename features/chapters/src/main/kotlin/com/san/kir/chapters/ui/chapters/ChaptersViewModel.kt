package com.san.kir.chapters.ui.chapters

import android.content.Context
import com.san.kir.background.logic.DownloadChaptersManager
import com.san.kir.background.logic.UpdateMangaManager
import com.san.kir.background.logic.di.downloadChaptersManager
import com.san.kir.background.logic.di.updateMangaManager
import com.san.kir.chapters.R
import com.san.kir.chapters.logic.utils.SelectionHelper
import com.san.kir.core.utils.ManualDI
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.core.utils.coroutines.withMainContext
import com.san.kir.core.utils.delChapters
import com.san.kir.core.utils.longToast
import com.san.kir.core.utils.toast
import com.san.kir.core.utils.viewModel.Action
import com.san.kir.core.utils.viewModel.ViewModel
import com.san.kir.data.chapterRepository
import com.san.kir.data.db.main.repo.ChapterRepository
import com.san.kir.data.db.main.repo.MangaRepository
import com.san.kir.data.db.main.repo.SettingsRepository
import com.san.kir.data.mangaRepository
import com.san.kir.data.models.base.countPages
import com.san.kir.data.models.main.Manga
import com.san.kir.data.models.main.SimplifiedChapter
import com.san.kir.data.models.utils.ChapterComparator
import com.san.kir.data.models.utils.ChapterFilter
import com.san.kir.data.models.utils.DownloadState
import com.san.kir.data.settingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

internal class ChaptersViewModel(
    private val mangaId: Long,
    private val context: Context = ManualDI.application,
    private val chaptersRepository: ChapterRepository = ManualDI.chapterRepository(),
    private val mangaRepository: MangaRepository = ManualDI.mangaRepository(),
    private val settingsRepository: SettingsRepository = ManualDI.settingsRepository(),
    private val updateManager: UpdateMangaManager = ManualDI.updateMangaManager(),
    private val downloadManager: DownloadChaptersManager = ManualDI.downloadChaptersManager(),
) : ViewModel<ChaptersState>(), ChaptersStateHolder {

    private val chapterComparator by lazy { ChapterComparator() }
    private var oneTimeFlag = true
    private val backgroundAction = MutableStateFlow(BackgroundActions())
    private val manga = mangaRepository.loadItem(mangaId)
        .filterNotNull()
        .distinctUntilChanged()
        .onEach { initFilterAndIncreasePopulate(it) }

    private val filter = MutableStateFlow(ChapterFilter.ALL_READ_ASC)

    override val itemsContent = MutableStateFlow(Items())

    override val nextChapter = chaptersRepository
        .items(mangaId)
        .map { checkNextChapter(it) }
        .stateInSubscribed(NextChapter.Loading)

    override val selectionMode = itemsContent
        .onEach {
            backgroundAction.value = BackgroundActions(updateManga = false, updateItems = false, updatePages = false)
        }
        .map { current ->
            val selected = current.items.filter { it.selected }
            val count = selected.size

            var above = 0
            var below = 0
            if (count == 1) {
                above = SelectionHelper.aboveCount(current.items)
                below = if (above == 0) 0 else SelectionHelper.belowCount(current.items)
                above = if (below == 0) 0 else above
            }

            SelectionMode(
                count = count,
                hasReading = selected.count { it.chapter.progress > 1 || it.chapter.isRead },
                canSetRead = selected.count { it.chapter.isRead.not() },
                canSetUnread = selected.count { it.chapter.isRead },
                canRemovePages = selected.count { current.memoryPagesCounts[it.chapter.id] != null },
                remain = current.items.size - count,
                aboveCount = above,
                belowCount = below,
            )
        }
        .stateInSubscribed(SelectionMode())

    override val tempState =
        combine(backgroundAction, manga, settingsRepository.showTitle, filter) { background, manga, title, filter ->
            ChaptersState(manga, background.result, title, filter)
        }

    override val defaultState = ChaptersState()

    init {
        combine(chaptersRepository.items(mangaId), filter, manga) { items, filter, manga ->
            itemsContent.value = SelectionHelper.update(itemsContent.value, items, filter, manga)
            updateMemoryCounts(items)
        }.launch()

        registerReceiver(mangaId)
    }

    override suspend fun onAction(action: Action) {
        when (action) {
            is ChaptersAction.WithSelected -> withSelected(action.mode)
            is ChaptersAction.ChangeFilter -> changeFilter(action.mode)
            is ChaptersAction.StartDownload -> downloadManager.addTask(action.id)
            is ChaptersAction.StopDownload -> downloadManager.removeTask(action.id)
            ChaptersAction.ChangeIsUpdate -> changeIsUpdate()
            ChaptersAction.ChangeMangaSort -> changeMangaSort()
            ChaptersAction.DownloadAll -> downloadAll()
            ChaptersAction.DownloadNext -> downloadNext()
            ChaptersAction.DownloadNotRead -> downloadNotReads()
            ChaptersAction.UpdateManga -> updateManager.addTask(manga.first().id)
            ChaptersAction.FullReset -> fullReadingReset()
            ChaptersAction.ResetError -> resetError()
        }
    }

    // Подписка на сообщения UpdateMangaWorker
    private fun CoroutineScope.registerReceiver(mangaId: Long) {
        updateManager.loadTask(mangaId).onEach { task ->
            backgroundAction.update { it.copy(updateManga = task != null) }

            when (task?.state) {
                DownloadState.UNKNOWN -> withMainContext {
                    context.longToast(R.string.update_error)
                }

                DownloadState.COMPLETED -> withMainContext {
                    if (task.newChapters <= 0) context.longToast(R.string.new_chapters_no_found)
                    else context.longToast(R.string.have_new_chapters_count, task.newChapters)
                }

                else -> {}
            }
        }.launchIn(this)
    }

    private suspend fun changeIsUpdate() = with(manga.first()) { mangaRepository.changeIsUpdate(id, isUpdate.not()) }

    private suspend fun changeMangaSort() {
        with(manga.first()) { mangaRepository.save(copy(isAlternativeSort = isAlternativeSort.not())) }
    }

    private suspend fun downloadAll() {
        val chapterIds = itemsContent.value.allIds()
        downloadManager.addTasks(chapterIds)
        showDownloadToast(chapterIds.size)
    }

    private suspend fun downloadNotReads() {
        val chapterIds = itemsContent.value.notReadIds()
        downloadManager.addTasks(chapterIds)
        showDownloadToast(chapterIds.size)
    }

    private suspend fun downloadNext() {
        chaptersRepository.newItem(manga.first().id)?.let { chapter -> downloadManager.addTask(chapter.id) }
    }

    private fun showDownloadToast(count: Int) {
        if (count == 0)
            context.toast(R.string.all_downloaded_before)
        else
            context.toast(context.getString(R.string.download_chapters_format, count))
    }

    private suspend fun withSelected(mode: Selection) {
        backgroundAction.update { it.copy(updateItems = true) }

        val selectedItems = itemsContent.value.items.filter { it.selected }
        when (mode) {
            Selection.Above -> itemsContent.update { SelectionHelper.above(it) }
            Selection.Below -> itemsContent.update { SelectionHelper.below(it) }
            Selection.All -> itemsContent.update { SelectionHelper.all(it) }
            Selection.Clear -> itemsContent.update { SelectionHelper.clear(it) }
            is Selection.Change -> itemsContent.update { SelectionHelper.change(it, mode.index) }

            Selection.DeleteFiles -> delChapters(
                selectedItems.map { chaptersRepository.item(it.chapter.id).path }
            ).apply {
                withMainContext {
                    if (current == 0) {
                        context.toast(R.string.nothing_delete)
                    } else {
                        context.toast(R.string.delete_successful)
                    }
                }
                itemsContent.update { SelectionHelper.clear(it) }
                updateMemoryCounts()
            }

            Selection.DeleteFromDB -> chaptersRepository.delete(selectedItems.map { it.chapter.id })

            Selection.Download -> {
                downloadManager.addTasks(selectedItems.map { it.chapter.id })
                itemsContent.update { SelectionHelper.clear(it) }
            }

            is Selection.SetRead -> {
                chaptersRepository.updateIsRead(selectedItems.map { it.chapter.id }, mode.newState)
                itemsContent.update { SelectionHelper.clear(it) }
            }

            Selection.Reset -> {
                chaptersRepository.reset(selectedItems.map { it.chapter.id })
                itemsContent.update { SelectionHelper.clear(it) }
            }
        }
    }

    private suspend fun updateMemoryCounts(list: List<SimplifiedChapter>? = null) {
        itemsContent.update { oldItems ->
            val countsMap = mutableMapOf<Long, Int>()

            withIoContext {
                if (list != null) {
                    list.forEach {
                        val count = it.countPages
                        if (count > 0) countsMap[it.id] = count
                    }
                } else {
                    oldItems.items.forEach {
                        val count = it.chapter.countPages
                        if (count > 0) countsMap[it.chapter.id] = count
                    }
                }
            }

            oldItems.copy(memoryPagesCounts = countsMap)
        }
    }

    private suspend fun changeFilter(mode: Filter) {
        val chapterFilter = filter.updateAndGet {
            when (mode) {
                Filter.All -> it.toAll()
                Filter.NotRead -> it.toNot()
                Filter.Read -> it.toRead()
                Filter.Reverse -> it.inverse()
            }
        }
        if (settingsRepository.isIndividual()) {
            mangaRepository.changeFilter(manga.first().id, chapterFilter)
        } else {
            settingsRepository.update(chapterFilter)
        }
    }

    private suspend fun initFilterAndIncreasePopulate(manga: Manga) {
        if (oneTimeFlag) {
            oneTimeFlag = false

            mangaRepository.save(manga.copy(populate = manga.populate + 1))

            filter.value =
                if (settingsRepository.isIndividual()) manga.chapterFilter else settingsRepository.filterStatus()
        }
    }

    private suspend fun checkNextChapter(list: List<SimplifiedChapter>): NextChapter {
        val newList = kotlin.runCatching {
            if (manga.first().isAlternativeSort.not()) null
            else list.sortedWith(chapterComparator)
        }.getOrNull() ?: list.sortedBy { it.id }

        val firstUnreadChapter = newList.firstOrNull { item -> item.isRead.not() }
        val firstChapter = newList.firstOrNull()
        return when {
            firstUnreadChapter == null -> NextChapter.None
            newList.size == 1 -> NextChapter.Ok.Single(firstUnreadChapter.id, firstUnreadChapter.name)
            firstUnreadChapter == firstChapter -> NextChapter.Ok.First(firstUnreadChapter.id, firstUnreadChapter.name)
            else -> NextChapter.Ok.Continue(firstUnreadChapter.id, firstUnreadChapter.name)
        }
    }

    private suspend fun fullReadingReset() {
        backgroundAction.update { it.copy(updateItems = true) }
        chaptersRepository.reset(mangaId)
    }

    private suspend fun resetError() {
        mangaRepository.save(manga.first().copy(lastUpdateError = null))
    }
}
