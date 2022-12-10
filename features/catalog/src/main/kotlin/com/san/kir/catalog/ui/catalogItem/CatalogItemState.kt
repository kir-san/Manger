package com.san.kir.catalog.ui.catalogItem

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.SiteCatalogElement


internal data class CatalogItemState(
    val item: SiteCatalogElement = SiteCatalogElement(),
    val containingInLibrary: ContainingInLibraryState = ContainingInLibraryState.Check,
    val background: BackgroundState = BackgroundState.None,
) : ScreenState

internal sealed interface BackgroundState {
    data object Load : BackgroundState
    data object Error : BackgroundState
    data object None : BackgroundState
}

internal sealed interface ContainingInLibraryState {
    data object Check : ContainingInLibraryState
    data object None : ContainingInLibraryState
    data object Ok : ContainingInLibraryState
}
