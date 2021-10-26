package com.san.kir.manger.ui.application_navigation.download

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.ankofork.sdk28.connectivityManager
import com.san.kir.manger.data.datastore.DownloadRepository
import com.san.kir.manger.di.DefaultDispatcher
import com.san.kir.manger.di.MainDispatcher
import com.san.kir.manger.room.dao.ChapterDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.services.DownloadService
import com.san.kir.manger.utils.enums.DownloadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val ctx: Application,
    private val chapterDao: ChapterDao,
    private val mangaDao: MangaDao,
    private val cellularNetwork: CellularNetwork,
    private val wifiNetwork: WifiNetwork,
    download: DownloadRepository,
    @MainDispatcher private val main: CoroutineDispatcher,
    @DefaultDispatcher private val default: CoroutineDispatcher,
) : ViewModel() {

    var items by mutableStateOf(listOf<Chapter>())
        private set

    var loadingCount by mutableStateOf(0)
        private set
    var stoppedCount by mutableStateOf(0)
        private set
    var completedCount by mutableStateOf(0)
        private set

    var network by mutableStateOf(NetworkState.OK)
        private set

    init {
        chapterDao.loadDownloadItemsWhereStatusNot()
            .distinctUntilChanged()
            .onEach { withContext(main) { items = it.sortedBy { c -> c.status.ordinal } } }
            .onEach { list ->
                list.filter { chapter ->
                    chapter.status == DownloadState.QUEUED ||
                            chapter.status == DownloadState.LOADING
                }.size.let { withContext(main) { loadingCount = it } }
            }
            .onEach { list ->
                list.filter { chapter ->
                    chapter.status == DownloadState.PAUSED
                }.size.let { withContext(main) { stoppedCount = it } }
            }
            .onEach { list ->
                list.filter { chapter ->
                    chapter.status == DownloadState.COMPLETED
                }.size.let { withContext(main) { completedCount = it } }
            }
            .launchIn(viewModelScope)

        combine(cellularNetwork.state, wifiNetwork.state, download.data) { cell, wifi, data ->
            if (data.wifi) {
                if (wifi) {
                    NetworkState.OK
                } else {
                    NetworkState.NOT_WIFI
                }
            } else {
                if (cell || wifi)
                    NetworkState.OK
                else
                    NetworkState.NOT_CELLURAR
            }
        }
            .onEach { state -> withContext(main) { network = state } }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        cellularNetwork.stop()
        wifiNetwork.stop()
    }

    fun clearCompletedDownloads() = viewModelScope.launch(default) {
        chapterDao.update(*items
            .filter { it.status == DownloadState.COMPLETED }
            .onEach { it.status = DownloadState.UNKNOWN }
            .toTypedArray())
    }

    fun clearPausedDownloads() = viewModelScope.launch(default) {
        chapterDao.update(*items
            .filter { it.status == DownloadState.PAUSED && !it.isError }
            .onEach { it.status = DownloadState.UNKNOWN }
            .toTypedArray())
    }

    fun clearErrorDownloads() = viewModelScope.launch(default) {
        chapterDao.update(*items
            .filter { it.status == DownloadState.PAUSED && it.isError }
            .onEach { it.status = DownloadState.UNKNOWN }
            .toTypedArray())
    }

    fun clearAllDownloads() = viewModelScope.launch(default) {
        chapterDao.update(*items
            .filter {
                it.status == DownloadState.COMPLETED || it.status == DownloadState.PAUSED
            }
            .onEach { it.status = DownloadState.UNKNOWN }
            .toTypedArray())
    }

    fun manga(item: Chapter) = mangaDao.loadItem(item.manga).filterNotNull()

    @OptIn(ExperimentalTime::class)
    fun remove(item: Chapter) = viewModelScope.launch(default) {
        DownloadService.pause(ctx, item)
        delay(Duration.Companion.seconds(1))
        item.status = DownloadState.UNKNOWN
        chapterDao.update(item)
    }
}

enum class NetworkState {
    NOT_WIFI, NOT_CELLURAR, OK
}

class CellularNetwork @Inject constructor(context: Application) : TemplateNetwork(
    context, NetworkCapabilities.TRANSPORT_CELLULAR
)

class WifiNetwork @Inject constructor(context: Application) : TemplateNetwork(
    context, NetworkCapabilities.TRANSPORT_WIFI,
)

abstract class TemplateNetwork(
    private val context: Application,
    networkTransport: Int,
) : ConnectivityManager.NetworkCallback() {

    private val request =
        NetworkRequest.Builder().addTransportType(networkTransport).build()

    private val _state = MutableStateFlow(false)
    val state = _state.asStateFlow()

    init {
        context.connectivityManager.registerNetworkCallback(request, this)
    }

    fun stop() {
        context.connectivityManager.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network) {
        _state.update { true }
    }

    override fun onLost(network: Network) {
        _state.update { false }
    }
}
