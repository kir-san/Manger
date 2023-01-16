package com.san.kir.categories.ui.category

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface CategoryEvent : ScreenEvent {
    data class Set(val categoryName: String) : CategoryEvent
    object Save : CategoryEvent
    object Delete : CategoryEvent
    data class Update(
        val newName: String? = null,
        val newTypeSort: String? = null,
        val newReverseSort: Boolean? = null,
        val newVisible: Boolean? = null,
        val newLargePortrait: Boolean? = null,
        val newSpanPortrait: Int? = null,
        val newLargeLandscape: Boolean? = null,
        val newSpanLandscape: Int? = null,
    ) : CategoryEvent
}
