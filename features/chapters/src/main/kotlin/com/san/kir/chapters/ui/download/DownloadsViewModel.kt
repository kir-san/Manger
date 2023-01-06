package com.san.kir.chapters.ui.download

import com.san.kir.background.logic.DownloadChaptersManager
import com.san.kir.chapters.logic.repo.ChaptersRepository
import com.san.kir.chapters.logic.repo.SettingsRepository
import com.san.kir.core.internet.CellularNetwork
import com.san.kir.core.internet.NetworkState
import com.san.kir.core.internet.WifiNetwork
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
internal class DownloadsViewModel @Inject constructor(
    private val chaptersRepository: ChaptersRepository,
    private val cellularNetwork: CellularNetwork,
    private val wifiNetwork: WifiNetwork,
    private val manager: DownloadChaptersManager,
    settingsRepository: SettingsRepository,
) : BaseViewModel<DownloadsEvent, DownloadsState>() {

    override val tempState = combine(
        chaptersRepository.items,
        cellularNetwork.state,
        wifiNetwork.state,
        settingsRepository.wifi,
    ) { items, cell, wifi, settingsWifi ->
        val network = if (settingsWifi) {
            if (wifi) NetworkState.OK else NetworkState.NOT_WIFI
        } else {
            if (cell || wifi) NetworkState.OK else NetworkState.NOT_CELLURAR
        }
        DownloadsState(
            items = items.toPersistentList(),
            network = network
        )
    }

    override val defaultState = DownloadsState(
        network = NetworkState.OK,
        items = persistentListOf()
    )

    override suspend fun onEvent(event: DownloadsEvent) {
        when (event) {
            DownloadsEvent.ClearAll         -> clearAll()
            DownloadsEvent.CompletedClear   -> clearCompleted()
            DownloadsEvent.ErrorClear       -> clearError()
            DownloadsEvent.PausedClear      -> clearPaused()
            DownloadsEvent.StartAll         -> manager.addPausedTasks()
            DownloadsEvent.StopAll          -> manager.removeAllTasks()
            is DownloadsEvent.StartDownload -> manager.addTask(event.itemId)
            is DownloadsEvent.StopDownload  -> manager.removeTask(event.itemId)
        }
    }

    private suspend fun clearAll() {
        chaptersRepository.clear(
            state.value
                .items
                .filter { it.status == DownloadState.COMPLETED || it.status == DownloadState.PAUSED }
                .map { it.id }
        )
    }

    private suspend fun clearError() {
        chaptersRepository.clear(
            state.value
                .items
                .filter { it.status == DownloadState.ERROR }
                .map { it.id }
        )
    }

    private suspend fun clearPaused() {
        chaptersRepository.clear(
            state.value
                .items
                .filter { it.status == DownloadState.PAUSED }
                .map { it.id }
        )
    }

    private suspend fun clearCompleted() {
        chaptersRepository.clear(
            state.value
                .items
                .filter { it.status == DownloadState.COMPLETED }
                .map { it.id }
        )
    }

    override fun onCleared() {
        cellularNetwork.stop()
        wifiNetwork.stop()
    }
}
