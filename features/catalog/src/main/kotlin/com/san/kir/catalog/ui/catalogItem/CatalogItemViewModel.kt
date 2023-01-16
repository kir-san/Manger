package com.san.kir.catalog.ui.catalogItem

import com.san.kir.catalog.logic.repo.CatalogRepository
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.models.base.SiteCatalogElement
import com.san.kir.data.parsing.SiteCatalogsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
internal class CatalogItemViewModel @Inject constructor(
    private val manager: SiteCatalogsManager,
    private val catalogRepository: CatalogRepository,
) : BaseViewModel<CatalogItemEvent, CatalogItemState>() {
    private val item = MutableStateFlow(SiteCatalogElement())
    private val containingInLibrary =
        MutableStateFlow<ContainingInLibraryState>(ContainingInLibraryState.Check)
    private val background = MutableStateFlow<BackgroundState>(BackgroundState.Load)

    override val tempState = combine(item, containingInLibrary, background, ::CatalogItemState)

    override val defaultState = CatalogItemState()

    override suspend fun onEvent(event: CatalogItemEvent) {
        when (event) {
            is CatalogItemEvent.Set -> {
                val item = manager.elementByUrl(event.url)
                if (item == null) {
                    background.update { BackgroundState.Error }
                    containingInLibrary.update { ContainingInLibraryState.Ok }
                } else {
                    this.item.update { item }
                    background.update { BackgroundState.None }

                    containingInLibrary.update {
                        if (catalogRepository.checkContains(item.shortLink))
                            ContainingInLibraryState.Ok
                        else
                            ContainingInLibraryState.None
                    }
                }
            }
        }
    }
}
