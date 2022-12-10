package com.san.kir.catalog.ui.catalog

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.san.kir.background.logic.UpdateCatalogManager
import com.san.kir.catalog.logic.repo.CatalogRepository
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.longToast
import com.san.kir.core.utils.mapP
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.models.extend.MiniCatalogItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
internal class CatalogViewModel @Inject constructor(
    private val context: Application,
    private val catalogRepository: CatalogRepository,
    private val manager: UpdateCatalogManager,
) : BaseViewModel<CatalogEvent, CatalogState>() {
    private var job: Job? = null
    private val items = MutableStateFlow(persistentListOf<MiniCatalogItem>())
    private val title = MutableStateFlow("")
    private val background = MutableStateFlow(
        BackgroundState(updateItems = false, updateCatalogs = false, progress = null)
    )
    private val sort = MutableStateFlow(SortState())
    private val filter = MutableStateFlow(FilterState())

    private val _filters = MutableStateFlow(persistentListOf<Filter>())
    val filters = _filters.asStateFlow()

    override val tempState = combine(
        items, title, filter, background, sort
    ) { items, title, filter, background, sort ->
        CatalogState(
            items = items.applyFilters(filter).applySort(sort),
            title = title,
            search = filter.search,
            background = background,
            sort = sort
        )
    }

    override val defaultState = CatalogState()

    override suspend fun onEvent(event: CatalogEvent) {
        when (event) {
            is CatalogEvent.Set -> set(event.catalogName)
            is CatalogEvent.UpdateManga -> updateManga(event.item)
            is CatalogEvent.ChangeFilter -> changeFilter(event.type, event.index)
            is CatalogEvent.Search -> filter.update { it.copy(search = event.query) }
            is CatalogEvent.ChangeSort -> sort.update { it.copy(type = event.sort) }
            CatalogEvent.Reverse -> sort.update { it.copy(reverse = it.reverse.not()) }
            CatalogEvent.ClearFilters -> clearFilters()
            CatalogEvent.UpdateContent -> manager.addTask(state.value.title)
            CatalogEvent.CancelUpdateContent -> manager.removeTask(state.value.title)
        }
    }

    private suspend fun set(catalogName: String) {
        if (job?.isActive == true) return

        title.update { catalogName }

        job = manager.loadTask(catalogName)
            .onEach { task ->
                var old = background.value
                old = if (task == null) {
                    old.copy(updateCatalogs = false, progress = null)
                } else {
                    when (task.state) {
                        DownloadState.LOADING ->
                            old.copy(updateCatalogs = true, progress = task.progress)

                        DownloadState.QUEUED,
                        DownloadState.PAUSED,
                        -> old.copy(updateCatalogs = true, progress = null)

                        else -> old
                    }
                }
                background.update { old }
                if (task == null) updateData(catalogName)
            }
            .launchIn(viewModelScope)
    }

    private suspend fun updateData(catalogName: String) {
        background.update { it.copy(updateItems = true) }
        items.update { catalogRepository.items(catalogName).toPersistentList() }
        _filters.update { initFilters() }
        background.update { it.copy(updateItems = false) }
    }

    private fun initFilters(): PersistentList<Filter> {
        return listOf(
            Filter(FilterType.Genres, items.value.flatMap { it.genres }.toSetSortedList()),
            Filter(FilterType.Types, items.value.map { it.type }.toSetSortedList()),
            Filter(FilterType.Statuses, items.value.map { it.statusEdition }.toSetSortedList()),
            Filter(FilterType.Authors, items.value.flatMap { it.authors }.toSetSortedList())
        ).filter { it.items.size > 1 }.toPersistentList()
    }

    private fun PersistentList<MiniCatalogItem>.applyFilters(filter: FilterState): PersistentList<MiniCatalogItem> {
        var prepare = if (filter.search.isNotEmpty()) {
            filter { it.name.lowercase().contains(filter.search.lowercase()) }
        } else this

        filter.selectedFilters.forEach { entry ->
            prepare = when (entry.key) {
                FilterType.Authors -> prepare.filter { it.authors.containsAll(entry.value) }
                FilterType.Genres -> prepare.filter { it.genres.containsAll(entry.value) }
                FilterType.Statuses -> prepare.filter { entry.value.contains(it.statusEdition) }
                FilterType.Types -> prepare.filter { entry.value.contains(it.type) }
            }
        }

        return prepare.toPersistentList()
    }

    private fun PersistentList<MiniCatalogItem>.applySort(sort: SortState): PersistentList<MiniCatalogItem> {
        val sorted = when (sort.type) {
            SortType.Date -> sortedBy { it.dateId }
            SortType.Name -> sortedBy { it.name }
            SortType.Pop -> sortedBy { it.populate }
        }

        return (if (sort.reverse) sorted.reversed() else sorted).toPersistentList()
    }

    private fun changeFilter(type: FilterType, index: Int) {
        val old = _filters.value
        val indexFilter = old.indexOfFirst { it.type == type }

        if (indexFilter != -1) {
            val filter = old[indexFilter]
            val item = filter.items[index].run { copy(state = state.not()) }
            _filters.update {
                old.set(indexFilter, filter.copy(items = filter.items.set(index, item)))
            }

            this.filter.update {
                it.copy(selectedFilters = it.selectedFilters.addOrRemoveSelectedFilter(type, item))
            }
        }
    }

    private fun PersistentMap<FilterType, List<String>>.addOrRemoveSelectedFilter(
        type: FilterType, item: SelectableItem,
    ): PersistentMap<FilterType, List<String>> {
        val oldItems = get(type)
        return if (oldItems == null) {
            put(type, if (item.state) listOf(item.name) else listOf())
        } else {
            val newItems = if (item.state) oldItems + item.name else oldItems - item.name
            if (newItems.isEmpty())
                remove(type)
            else
                put(type, newItems)
        }
    }

    private fun clearFilters() {
        filter.update { it.copy(selectedFilters = persistentMapOf()) }
        _filters.update { old ->
            old.mapP { filter ->
                filter.copy(items = filter.items.mapP { it.copy(state = false) })
            }
        }
    }

    private suspend fun updateManga(item: MiniCatalogItem) {
        catalogRepository.updateMangaBy(item)
        context.longToast("Информация о манге ${item.name} обновлена")
    }

    private fun List<String>.toSetSortedList() =
        map(String::trim).toHashSet().sorted()
            .filter(String::isNotEmpty).mapP { SelectableItem(it, false) }
}
