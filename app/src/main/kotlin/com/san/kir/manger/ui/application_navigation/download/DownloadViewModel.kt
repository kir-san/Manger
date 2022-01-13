package com.san.kir.manger.ui.application_navigation.download

import android.app.Application
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
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.store.DownloadStore
import com.san.kir.core.download.DownloadService
import com.san.kir.core.internet.CellularNetwork
import com.san.kir.core.internet.NetworkState
import com.san.kir.core.internet.WifiNetwork
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    fun manga(item: Chapter) = mangaDao.itemWhereName(item.manga).filterNotNull()

    @OptIn(ExperimentalTime::class)
    fun remove(item: Chapter) = viewModelScope.defaultLaunch {
        DownloadService.pause(ctx, item)
        delay(Duration.Companion.seconds(1))
        item.status = DownloadState.UNKNOWN
        chapterDao.update(item)
    }
}


