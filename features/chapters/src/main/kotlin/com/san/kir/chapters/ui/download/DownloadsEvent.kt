package com.san.kir.chapters.ui.download

import com.san.kir.core.utils.viewModel.ScreenEvent


internal sealed interface DownloadsEvent : ScreenEvent {
    data object StartAll : DownloadsEvent
    data object StopAll : DownloadsEvent
    data object ClearAll : DownloadsEvent
    data object CompletedClear : DownloadsEvent
    data object PausedClear : DownloadsEvent
    data object ErrorClear : DownloadsEvent
    data class StartDownload(val itemId: Long) : DownloadsEvent
    data class StopDownload(val itemId: Long) : DownloadsEvent
}
