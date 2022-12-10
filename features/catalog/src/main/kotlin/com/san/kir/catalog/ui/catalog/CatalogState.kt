package com.san.kir.catalog.ui.catalog

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import com.san.kir.catalog.R
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.extend.MiniCatalogItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf


internal data class CatalogState(
    val items: ImmutableList<MiniCatalogItem> = persistentListOf(),
    val title: String = "",
    val search: String = "",
    val background: BackgroundState = BackgroundState(
        updateItems = false,
        updateCatalogs = false,
        progress = null
    ),
    val sort: SortState = SortState(),
) : ScreenState {
    override fun toString() =
        "CatalogState(items = ${items.size}, title = $title, search = $search, " +
                "background = $background, sort = $sort"
}

@Stable
internal data class SortState(
    val type: SortType = SortType.Date,
    val reverse: Boolean = false,
)

internal data class FilterState(
    val search: String = "",
    val selectedFilters: PersistentMap<FilterType, List<String>> = persistentMapOf()
)

internal sealed interface SortType {
    data object Date : SortType
    data object Name : SortType
    data object Pop : SortType
}

@Stable
internal data class Filter(
    val type: FilterType,
    val items: PersistentList<SelectableItem>
)

@Stable
internal data class BackgroundState(
    val updateItems: Boolean,
    val updateCatalogs: Boolean,
    val progress: Float?,
) {
    val currentState: Boolean
        get() = updateCatalogs || updateItems
}

internal data class SelectableItem(
    val name: String,
    val state: Boolean
)

internal sealed class FilterType(@StringRes val stringId: Int) {
    data object Genres : FilterType(R.string.catalog_fot_one_site_genres)
    data object Types : FilterType(R.string.catalog_fot_one_site_type)
    data object Statuses : FilterType(R.string.catalog_fot_one_site_statuses)
    data object Authors : FilterType(R.string.catalog_fot_one_site_authors)
}
