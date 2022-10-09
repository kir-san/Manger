package com.san.kir.library.ui.mangaAbout

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface MangaAboutEvent : ScreenEvent {
    data class Set(val id: Long) : MangaAboutEvent
    data class ChangeUpdate(val newState: Boolean) : MangaAboutEvent
    data class ChangeColor(val newState: Int) : MangaAboutEvent
}
