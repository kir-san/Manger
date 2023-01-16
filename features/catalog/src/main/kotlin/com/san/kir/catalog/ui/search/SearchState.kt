package com.san.kir.catalog.ui.search

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.extend.MiniCatalogItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf


internal data class SearchState(
    val items: ImmutableList<MiniCatalogItem> = persistentListOf(),
    val background: Boolean = false
) : ScreenState

