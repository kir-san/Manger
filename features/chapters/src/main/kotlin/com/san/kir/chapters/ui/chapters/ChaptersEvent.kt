package com.san.kir.chapters.ui.chapters

import com.san.kir.core.utils.viewModel.ScreenEvent


internal sealed interface ChaptersEvent : ScreenEvent {
    data class Set(val mangaId: Long) : ChaptersEvent
    data class WithSelected(val mode: Selection) : ChaptersEvent
    data class ChangeFilter(val mode: Filter) : ChaptersEvent
    data class StartDownload(val id: Long) : ChaptersEvent
    data class StopDownload(val id: Long) : ChaptersEvent
    object UpdateManga : ChaptersEvent
    object DownloadNext : ChaptersEvent
    object DownloadNotRead : ChaptersEvent
    object DownloadAll : ChaptersEvent
    object ChangeIsUpdate : ChaptersEvent
    object ChangeMangaSort : ChaptersEvent
}

internal sealed interface Selection {
    object Download : Selection
    object All : Selection
    object Clear : Selection
    object Above : Selection
    object Below : Selection
    object DeleteFromDB : Selection
    object DeleteFiles : Selection
    data class Change(val index: Int) : Selection
    data class SetRead(val newState: Boolean) : Selection
}

internal sealed interface Filter {
    object Reverse : Filter
    object All : Filter
    object Read : Filter
    object NotRead : Filter
}
