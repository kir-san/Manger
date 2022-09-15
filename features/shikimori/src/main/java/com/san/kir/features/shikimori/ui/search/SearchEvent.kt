package com.san.kir.features.shikimori.ui.search

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface SearchEvent : ScreenEvent {
    data class Search(val text: String) : SearchEvent
}
