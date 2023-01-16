package com.san.kir.catalog.ui.catalog

import com.san.kir.core.utils.viewModel.ScreenEvent
import com.san.kir.data.models.extend.MiniCatalogItem


internal sealed interface CatalogEvent : ScreenEvent {
    data class Set(val catalogName: String) : CatalogEvent
    data class Search(val query: String) : CatalogEvent
    data class ChangeFilter(val type: FilterType, val index: Int) : CatalogEvent
    data class ChangeSort(val sort: SortType) : CatalogEvent
    data class UpdateManga(val item: MiniCatalogItem) : CatalogEvent
    data object Reverse : CatalogEvent
    data object ClearFilters : CatalogEvent
    data object UpdateContent : CatalogEvent
    data object CancelUpdateContent : CatalogEvent
}
