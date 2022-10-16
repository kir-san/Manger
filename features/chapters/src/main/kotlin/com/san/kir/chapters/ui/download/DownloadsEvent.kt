package com.san.kir.chapters.ui.download

import com.san.kir.core.utils.viewModel.ScreenEvent


internal sealed interface DownloadsEvent : ScreenEvent {
    object StartAll : DownloadsEvent
    object StopAll : DownloadsEvent
    object ClearAll : DownloadsEvent
    object CompletedClear : DownloadsEvent
    object PausedClear : DownloadsEvent
    object ErrorClear : DownloadsEvent
    data class StartDownload(val itemId: Long) : DownloadsEvent
    data class StopDownload(val itemId: Long) : DownloadsEvent
}
