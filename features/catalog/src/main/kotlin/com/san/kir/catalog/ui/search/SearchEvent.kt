package com.san.kir.catalog.ui.search

import com.san.kir.core.utils.viewModel.ScreenEvent
import com.san.kir.data.models.extend.MiniCatalogItem


internal sealed interface SearchEvent : ScreenEvent {
    data class Search(val query: String) : SearchEvent
    data class UpdateManga(val item: MiniCatalogItem) : SearchEvent
}
