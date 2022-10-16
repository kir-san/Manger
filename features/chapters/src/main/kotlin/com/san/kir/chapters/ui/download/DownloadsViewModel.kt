package com.san.kir.chapters.ui.download

import android.app.Application
import com.san.kir.chapters.logic.repo.ChaptersRepository
import com.san.kir.chapters.logic.repo.SettingsRepository
import com.san.kir.core.download.DownloadService
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
    private val context: Application,
    private val chaptersRepository: ChaptersRepository,
    private val cellularNetwork: CellularNetwork,
    private val wifiNetwork: WifiNetwork,
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
            DownloadsEvent.ClearAll -> clearAll()
            DownloadsEvent.CompletedClear -> clearCompleted()
            DownloadsEvent.ErrorClear -> clearError()
            DownloadsEvent.PausedClear -> clearPaused()
            DownloadsEvent.StartAll -> DownloadService.startAll(context)
            DownloadsEvent.StopAll -> DownloadService.pauseAll(context)
            is DownloadsEvent.StartDownload -> DownloadService.start(context, event.itemId)
            is DownloadsEvent.StopDownload -> DownloadService.pause(context, event.itemId)
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
                .filter { it.status == DownloadState.PAUSED && it.isError }
                .map { it.id }
        )
    }

    private suspend fun clearPaused() {
        chaptersRepository.clear(
            state.value
                .items
                .filter { it.status == DownloadState.PAUSED && it.isError.not() }
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
