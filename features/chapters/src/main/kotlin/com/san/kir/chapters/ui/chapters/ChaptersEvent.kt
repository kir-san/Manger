package com.san.kir.chapters.ui.chapters

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface ChaptersEvent : ScreenEvent {
    data class Set(val mangaId: Long) : ChaptersEvent
    data class WithSelected(val mode: Selection) : ChaptersEvent
    data class ChangeFilter(val mode: Filter) : ChaptersEvent
    data class StartDownload(val id: Long) : ChaptersEvent
    data class StopDownload(val id: Long) : ChaptersEvent
    data object UpdateManga : ChaptersEvent
    data object DownloadNext : ChaptersEvent
    data object DownloadNotRead : ChaptersEvent
    data object DownloadAll : ChaptersEvent
    data object ChangeIsUpdate : ChaptersEvent
    data object ChangeMangaSort : ChaptersEvent
}

internal sealed interface Selection {
    data object Download : Selection
    data object All : Selection
    data object Clear : Selection
    data object Above : Selection
    data object Below : Selection
    data object DeleteFromDB : Selection
    data object DeleteFiles : Selection
    data class Change(val index: Int) : Selection
    data class SetRead(val newState: Boolean) : Selection
}

internal sealed interface Filter {
    data object Reverse : Filter
    data object All : Filter
    data object Read : Filter
    data object NotRead : Filter
}
