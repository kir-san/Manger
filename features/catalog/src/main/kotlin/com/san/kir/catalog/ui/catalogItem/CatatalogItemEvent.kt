package com.san.kir.catalog.ui.catalogItem

import com.san.kir.core.utils.viewModel.ScreenEvent


internal sealed interface CatalogItemEvent : ScreenEvent {
    data class Set(val url: String) : CatalogItemEvent
}
