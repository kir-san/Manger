package com.san.kir.catalog.ui.catalogItem

import com.san.kir.catalog.logic.linksForCatalog
import com.san.kir.core.internet.AuthorizationException
import com.san.kir.core.utils.ManualDI
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.viewModel.Action
import com.san.kir.core.utils.viewModel.ViewModel
import com.san.kir.data.catalogsRepository
import com.san.kir.data.db.catalog.repo.CatalogsRepository
import com.san.kir.data.db.main.repo.MangaRepository
import com.san.kir.data.mangaRepository
import com.san.kir.data.models.catalog.SiteCatalogElement
import com.san.kir.data.parsing.SiteCatalogsManager
import com.san.kir.data.parsing.siteCatalogsManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

internal class CatalogItemViewModel(
    private val item: SiteCatalogElement,
    private val manager: SiteCatalogsManager = ManualDI.siteCatalogsManager(),
    private val catalogRepository: CatalogsRepository = ManualDI.catalogsRepository(),
    private val mangaRepository: MangaRepository = ManualDI.mangaRepository(),
) : ViewModel<CatalogItemState>(), CatalogItemStateHolder {
    private val catalogName = manager.catalogName(item.catalogName)
    private val siteCatalog = manager.catalog(item.catalogName)

    private val itemFlow = MutableStateFlow(item)
    private val background = MutableStateFlow<BackgroundState>(BackgroundState.Load)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val containingInLibrary = mangaRepository
        .items
        .mapLatest { mangas ->
            val contains = mangas.linksForCatalog(siteCatalog)
                .any { it.contains(item.shortLink) }
            if (contains) ContainingInLibraryState.Ok else ContainingInLibraryState.None
        }
        .stateInSubscribed(ContainingInLibraryState.None)

    override val tempState = combine(itemFlow, containingInLibrary, background, ::CatalogItemState)
    override val defaultState = CatalogItemState()

    init {
        defaultLaunch {
            itemFlow.update { catalogRepository.item(catalogName, item.id) ?: it }

            runCatching {
                val item = manager.elementByUrl(item.link)!!
                itemFlow.update { item }
                background.update { BackgroundState.None }

                catalogRepository.insert(catalogName, item.copy(id = this@CatalogItemViewModel.item.id))
            }.onFailure { ex ->
                background.update {
                    if (ex is AuthorizationException) {
                        BackgroundState.Error.Auth
                    } else {
                        BackgroundState.Error.Base
                    }
                }
            }
        }
    }

    override suspend fun onAction(action: Action) {}
}
