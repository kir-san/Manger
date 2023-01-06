package com.san.kir.chapters.ui.latest

import android.app.Application
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.san.kir.background.logic.DownloadChaptersManager
import com.san.kir.background.works.LatestClearWorkers
import com.san.kir.chapters.logic.repo.LatestRepository
import com.san.kir.core.support.ChapterStatus
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.models.base.action
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class LatestViewModel @Inject constructor(
    private val context: Application,
    private val latestRepository: LatestRepository,
    private val downloadManager: DownloadChaptersManager,
) : BaseViewModel<LatestEvent, LatestState>() {

    private var job: Job? = null
    private val hasBackground = MutableStateFlow(true)
    private val items = MutableStateFlow<PersistentList<SelectableItem>>(persistentListOf())

    private val newItems = latestRepository
        .notReadItems
        .distinctUntilChanged()
        .mapLatest { list -> list.filter { it.action == ChapterStatus.DOWNLOADABLE } }

    init {
        latestRepository
            .items
            .onEach { list ->
                items.update { oldItems ->
                    if (list.size != oldItems.size) {
                        list.map { SelectableItem(it, false) }
                            .toPersistentList()
                    } else {
                        list.zip(oldItems)
                            .map { (chapter, item) -> item.copy(chapter = chapter) }
                            .toPersistentList()
                    }
                }
            }.launchIn(viewModelScope)
    }

    override val tempState = combine(
        items,
        newItems.onEach { runWorkersObserver() },
        hasBackground,
    ) { items, newItems, background ->
        LatestState(
            items = items,
            hasNewChapters = newItems.isNotEmpty(),
            hasBackgroundWork = background,
        )
    }

    override val defaultState = LatestState(
        items = persistentListOf(),
        hasNewChapters = false,
        hasBackgroundWork = true,
    )

    override suspend fun onEvent(event: LatestEvent) {
        when (event) {
            LatestEvent.CleanAll -> LatestClearWorkers.clearAll(context)
            LatestEvent.CleanDownloaded -> LatestClearWorkers.clearDownloaded(context)
            LatestEvent.CleanRead -> LatestClearWorkers.clearReaded(context)
            LatestEvent.DownloadNew -> downloadNewChapters()
            LatestEvent.RemoveSelected -> removeSelected()
            LatestEvent.DownloadSelected -> downloadSelected()
            LatestEvent.UnselectAll -> unselect()
            is LatestEvent.ChangeSelect -> changeSelect(event.index)
            is LatestEvent.StartDownload -> downloadManager.addTask(event.id)
            is LatestEvent.StopDownload -> downloadManager.removeTask(event.id)
        }
    }

    private suspend fun downloadNewChapters() = viewModelScope.launch {
        downloadManager.addTasks(newItems.first().map { it.id })
    }

    private suspend fun removeSelected() {
        latestRepository.update(items.value.filter { it.selected }.map { it.chapter.id }, false)
    }

    private fun downloadSelected() = viewModelScope.launch {
        downloadManager.addTasks(items.value.filter { it.selected }.map { it.chapter.id })
    }

    private fun changeSelect(index: Int) {
        items.update { list ->
            val changedItem = list[index]
            list.set(index, changedItem.copy(selected = changedItem.selected.not()))
        }
    }

    private fun unselect() {
        items.update { list -> list.map { it.copy(selected = false) }.toPersistentList() }
    }

    private fun runWorkersObserver() {
        if (job?.isActive == true) return
        hasBackground.value = false
        job = WorkManager
            .getInstance(context)
            .getWorkInfosByTagLiveData(LatestClearWorkers.tag)
            .asFlow()
            .onEach { works ->
                hasBackground.value = works.isNotEmpty() && works.none { it.state.isFinished }
            }
            .launchIn(viewModelScope)
    }
}
