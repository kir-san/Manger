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
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.coroutines.withMainContext
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.Chapter
import com.san.kir.data.store.DownloadStore
import com.san.kir.manger.foreground_work.services.DownloadService
import com.san.kir.manger.utils.extensions.connectivityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
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
    download: DownloadStore,
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
            .onEach {
                withMainContext {
                    items = it.sortedBy { c -> c.status.ordinal }
                }
            }
            .onEach { list ->
                list.filter { chapter ->
                    chapter.status == DownloadState.QUEUED ||
                            chapter.status == DownloadState.LOADING
                }.size.let {
                    withMainContext {
                        loadingCount = it
                    }
                }
            }
            .onEach { list ->
                list.filter { chapter ->
                    chapter.status == DownloadState.PAUSED
                }.size.let {
                    withMainContext {
                        stoppedCount = it
                    }
                }
            }
            .onEach { list ->
                list.filter { chapter ->
                    chapter.status == DownloadState.COMPLETED
                }.size.let {
                    withMainContext {
                        completedCount = it
                    }
                }
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
            .onEach { state ->
                withMainContext {
                    network = state
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        cellularNetwork.stop()
        wifiNetwork.stop()
    }

    fun clearCompletedDownloads() = viewModelScope.defaultLaunch {
        chapterDao.update(*items
            .filter { it.status == DownloadState.COMPLETED }
            .onEach { it.status = DownloadState.UNKNOWN }
            .toTypedArray())
    }

    fun clearPausedDownloads() = viewModelScope.defaultLaunch {
        chapterDao.update(*items
            .filter { it.status == DownloadState.PAUSED && !it.isError }
            .onEach { it.status = DownloadState.UNKNOWN }
            .toTypedArray())
    }

    fun clearErrorDownloads() = viewModelScope.defaultLaunch {
        chapterDao.update(*items
            .filter { it.status == DownloadState.PAUSED && it.isError }
            .onEach { it.status = DownloadState.UNKNOWN }
            .toTypedArray())
    }

    fun clearAllDownloads() = viewModelScope.defaultLaunch {
        chapterDao.update(*items
            .filter {
                it.status == DownloadState.COMPLETED || it.status == DownloadState.PAUSED
            }
            .onEach { it.status = DownloadState.UNKNOWN }
            .toTypedArray())
    }

    fun manga(item: Chapter) = mangaDao.loadItem(item.manga).filterNotNull()

    @OptIn(ExperimentalTime::class)
    fun remove(item: Chapter) = viewModelScope.defaultLaunch {
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
