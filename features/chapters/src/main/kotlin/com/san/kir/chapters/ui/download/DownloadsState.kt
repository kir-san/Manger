package com.san.kir.chapters.ui.download

import com.san.kir.core.internet.NetworkState
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.extend.DownloadChapter
import kotlinx.collections.immutable.ImmutableList


internal data class DownloadsState(
    val network: NetworkState,
    val items: ImmutableList<DownloadChapter>,
    val loadingCount: Int = items.count { it.status == DownloadState.QUEUED || it.status == DownloadState.LOADING },
    val stoppedCount: Int = items.count { it.status == DownloadState.PAUSED },
    val completedCount: Int = items.count { it.status == DownloadState.COMPLETED }
) : ScreenState
