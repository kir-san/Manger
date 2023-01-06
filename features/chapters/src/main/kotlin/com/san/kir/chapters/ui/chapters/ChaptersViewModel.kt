package com.san.kir.chapters.ui.chapters

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.san.kir.background.logic.DownloadChaptersManager
import com.san.kir.background.logic.UpdateMangaManager
import com.san.kir.chapters.R
import com.san.kir.chapters.logic.repo.ChaptersRepository
import com.san.kir.chapters.logic.repo.SettingsRepository
import com.san.kir.chapters.logic.utils.SelectionHelper
import com.san.kir.core.support.ChapterFilter
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.coroutines.withMainContext
import com.san.kir.core.utils.delChapters
import com.san.kir.core.utils.longToast
import com.san.kir.core.utils.toast
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.utils.ChapterComparator
import com.san.kir.data.parsing.SiteCatalogsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ChaptersViewModel @Inject constructor(
    private val context: Application,
    private val chaptersRepository: ChaptersRepository,
    private val settingsRepository: SettingsRepository,
    private val manager: SiteCatalogsManager,
    private val updateManager: UpdateMangaManager,
    private val downloadManager: DownloadChaptersManager,
) : BaseViewModel<ChaptersEvent, ChaptersState>() {
    private var job: Job? = null
    private val selectableItemComparator by lazy { ChapterComparator() }
    private var oneTimeFlag = true

    private val backgroundAction = MutableStateFlow(BackgroundActions())
    private val manga = MutableStateFlow(Manga())
    private val items = MutableStateFlow(Items())
    private val filter = MutableStateFlow(ChapterFilter.ALL_READ_ASC)
    private val nextChapter = manga
        .map { manga -> checkNextChapter(manga) }
        .onStart { emit(NextChapter.None) }

    override val tempState = combine(
        backgroundAction,
        manga,
        items.onEach { backgroundAction.update { it.copy(updateItems = false) } },
        settingsRepository.showTitle,
        filter.combine(nextChapter) { f1, f2 -> f1 to f2 }
    ) { background, manga, items, title, filterNext ->
        ChaptersState(
            backgroundAction = background.result,
            items = items.items,
            manga = manga,
            showTitle = title,
            chapterFilter = filterNext.first,
            nextChapter = filterNext.second,
            count = items.count,
            readCount = items.readCount,
        )
    }

    override val defaultState = ChaptersState(
        items = persistentListOf(),
        manga = Manga(),
        backgroundAction = false,
        showTitle = true,
        nextChapter = NextChapter.None,
        chapterFilter = ChapterFilter.ALL_READ_ASC,
    )

    override suspend fun onEvent(event: ChaptersEvent) {
        when (event) {
            is ChaptersEvent.Set -> set(event.mangaId)
            is ChaptersEvent.WithSelected -> withSelected(event.mode)
            is ChaptersEvent.ChangeFilter -> changeFilter(event.mode)
            is ChaptersEvent.StartDownload -> downloadManager.addTask(event.id)
            is ChaptersEvent.StopDownload -> downloadManager.removeTask(event.id)
            ChaptersEvent.ChangeIsUpdate -> changeIsUpdate()
            ChaptersEvent.ChangeMangaSort -> changeMangaSort()
            ChaptersEvent.DownloadAll -> downloadAll()
            ChaptersEvent.DownloadNext -> downloadNext()
            ChaptersEvent.DownloadNotRead -> downloadNotReads()
            ChaptersEvent.UpdateManga -> updateManager.addTask(manga.value.id)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun set(mangaId: Long) {
        if (job?.isActive == true) return

        job = viewModelScope.launch {
            combine(
                chaptersRepository
                    .loadManga(mangaId)
                    .filterNotNull()
                    .onEach { item ->
                        manga.update { item }
                        initFilterAndIncreasePopulate(item)
                    }
                    .flatMapLatest { chaptersRepository.items(it.id).onEach { } },
                filter, manga
            ) { list, filter, manga ->
                items.update { old -> SelectionHelper.update(old, list, filter, manga) }
            }.launchIn(this)

            combine(settingsRepository.isIndividual, filter) { individual, filter ->
                if (individual) {
                    chaptersRepository.update(manga.value.copy(chapterFilter = filter))
                } else {
                    settingsRepository.update(filter)
                }
            }.launchIn(this)


        }
        registerReceiver(mangaId)
    }

    // Подписка на сообщения UpdateMangaWorker
    private fun registerReceiver(mangaId: Long) {
        updateManager.loadTask(mangaId).onEach { task ->
            backgroundAction.update { it.copy(updateManga = task != null) }

            when (task?.state) {
                DownloadState.UNKNOWN -> context.longToast(R.string.list_chapters_message_error)
                DownloadState.COMPLETED -> {
                    if (task.newChapters > 0)
                        context.longToast(
                            R.string.list_chapters_message_count_new, task.newChapters
                        )
                    else
                        context.longToast(R.string.list_chapters_message_no_found)
                }

                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    private suspend fun changeIsUpdate() {
        with(manga.value) { chaptersRepository.update(copy(isUpdate = isUpdate.not())) }
    }

    private suspend fun changeMangaSort() {
        with(manga.value) { chaptersRepository.update(copy(isAlternativeSort = isAlternativeSort.not())) }
    }

    private fun downloadAll() = viewModelScope.launch {
        val chapterIds = chaptersRepository.allItems(manga.value.id).map { it.id }
        downloadManager.addTasks(chapterIds)
        showDownloadToast(chapterIds.size)
    }

    private fun downloadNotReads() = viewModelScope.launch {
        val chapterIds = chaptersRepository.notReadItems(manga.value.id).map { it.id }
        downloadManager.addTasks(chapterIds)
        showDownloadToast(chapterIds.size)
    }

    private fun downloadNext() = viewModelScope.launch {
        chaptersRepository.newItem(manga.value.id)?.let { chapter ->
            downloadManager.addTask(chapter.id)
        }
    }

    private fun showDownloadToast(count: Int) {
        if (count == 0)
            context.toast(R.string.list_chapters_selection_load_error)
        else
            context.toast(context.getString(R.string.list_chapters_selection_load_ok, count))
    }

    private fun withSelected(mode: Selection) = viewModelScope.launch {
        backgroundAction.update { it.copy(updateItems = true) }

        val selectedItems = state.value.items.filter { it.selected }
        when (mode) {
            Selection.Above -> items.update { SelectionHelper.above(it) }
            Selection.Below -> items.update { SelectionHelper.below(it) }
            Selection.All -> items.update { SelectionHelper.all(it) }
            Selection.Clear -> items.update { SelectionHelper.clear(it) }
            is Selection.Change -> items.update { SelectionHelper.change(it, mode.index) }

            Selection.DeleteFiles -> delChapters(
                selectedItems.map { chaptersRepository.item(it.chapter.id).path }
            ).apply {
                withMainContext {
                    if (current == 0) {
                        context.toast(R.string.list_chapters_selection_del_error)
                    } else {
                        context.toast(R.string.list_chapters_selection_del_ok)
                    }
                }
                items.update { SelectionHelper.clear(it) }
            }

            Selection.DeleteFromDB -> chaptersRepository.delete(selectedItems.map { it.chapter.id })

            Selection.Download -> {
                downloadManager.addTasks(selectedItems.map { it.chapter.id })
                items.update { SelectionHelper.clear(it) }
            }

            Selection.UpdatePages -> {
                backgroundAction.update { it.copy(updatePages = true) }
                items.update { SelectionHelper.clear(it) }
                selectedItems.onEach {
                    val item = chaptersRepository.item(it.chapter.id)
                    chaptersRepository.update(item.copy(pages = manager.pages(item)))
                }
                backgroundAction.update { it.copy(updatePages = false) }
            }

            is Selection.SetRead -> {
                chaptersRepository.update(selectedItems.map { it.chapter.id }, mode.newState)
                items.update { SelectionHelper.clear(it) }
            }
        }
    }

    private fun changeFilter(mode: Filter) {
        filter.update {
            when (mode) {
                Filter.All -> it.toAll()
                Filter.NotRead -> it.toNot()
                Filter.Read -> it.toRead()
                Filter.Reverse -> it.inverse()
            }
        }
    }

    private fun initFilterAndIncreasePopulate(manga: Manga) = viewModelScope.launch {
        if (oneTimeFlag) {
            oneTimeFlag = false

            chaptersRepository.update(manga.copy(populate = manga.populate))
            filter.update {
                if (settingsRepository.currentChapters().isIndividual) {
                    manga.chapterFilter
                } else {
                    settingsRepository.currentChapters().filterStatus
                }
            }
        }
    }

    private suspend fun checkNextChapter(manga: Manga): NextChapter {
        val list = chaptersRepository.allItems(manga.id)

        val newList = kotlin.runCatching {
            if (manga.isAlternativeSort)
                list.sortedWith(selectableItemComparator)
            else null
        }.getOrNull() ?: list

        val result = newList.firstOrNull { item -> item.isRead.not() }
        return if (result != null)
            NextChapter.Ok(result.id, result.name)
        else
            NextChapter.None
    }
}


