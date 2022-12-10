package com.san.kir.catalog.ui.search

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.san.kir.catalog.logic.repo.CatalogRepository
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.longToast
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.models.extend.MiniCatalogItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
internal class SearchViewModel @Inject constructor(
    private val context: Application,
    private val catalogRepository: CatalogRepository,
) : BaseViewModel<SearchEvent, SearchState>() {
    private var job: Job? = null

    private val background = MutableStateFlow(false)
    private val search = MutableStateFlow("")
    private val items = MutableStateFlow(persistentListOf<MiniCatalogItem>())

    override val tempState = combine(items, search, background) { items, search, background ->
        SearchState(items.applyFilters(search), background)
    }

    override val defaultState = SearchState()

    init {
        loadItems()
    }

    override suspend fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.Search -> updateFilter(event.query)
            is SearchEvent.UpdateManga -> updateManga(event.item)
        }
    }

    private fun loadItems() {
        background.update { true }
        viewModelScope.defaultLaunch {
            catalogRepository.items.forEach { catalog ->
                items.update { old ->
                    old.addAll(catalogRepository.items(catalog.name))
                }
            }
            background.update { false }
        }
    }

    private fun updateFilter(value: String) {
        job?.cancel()
        job = viewModelScope.launch {
            delay(1.seconds)
            search.update { value }
        }
    }

    private suspend fun updateManga(item: MiniCatalogItem) {
        catalogRepository.updateMangaBy(item)
        context.longToast("Информация о манге ${item.name} обновлена")
    }

    private fun PersistentList<MiniCatalogItem>.applyFilters(query: String): PersistentList<MiniCatalogItem> {
        return if (query.isNotEmpty()) {
            filter { it.name.lowercase().contains(query.lowercase()) }.toPersistentList()
        } else this
    }
}
