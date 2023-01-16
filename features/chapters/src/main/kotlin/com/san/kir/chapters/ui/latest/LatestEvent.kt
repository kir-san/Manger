package com.san.kir.chapters.ui.latest

import com.san.kir.core.utils.viewModel.ScreenEvent


internal sealed interface LatestEvent : ScreenEvent {
    object DownloadNew : LatestEvent
    object CleanAll : LatestEvent
    object CleanRead : LatestEvent
    object CleanDownloaded : LatestEvent
    object RemoveSelected : LatestEvent
    object DownloadSelected : LatestEvent
    object UnselectAll : LatestEvent
    data class ChangeSelect(val index: Int) : LatestEvent
    data class StartDownload(val id: Long) : LatestEvent
    data class StopDownload(val id: Long) : LatestEvent
}
