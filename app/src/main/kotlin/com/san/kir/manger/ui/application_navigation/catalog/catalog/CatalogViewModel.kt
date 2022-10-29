package com.san.kir.manger.ui.application_navigation.catalog.catalog

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.coroutines.mainLaunch
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.data.db.CatalogDb
import com.san.kir.data.db.dao.SiteDao
import com.san.kir.data.models.base.SiteCatalogElement
import com.san.kir.data.parsing.SiteCatalogsManager
import com.san.kir.manger.R
import com.san.kir.background.services.CatalogForOneSiteUpdaterService
import com.san.kir.manger.ui.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class CatalogViewModel @AssistedInject constructor(
    @Assisted val siteName: String,
    private val context: Application,
    private val siteDao: SiteDao,
    private val dbFactory: CatalogDb.Factory,
    private val siteCatalogsManager: SiteCatalogsManager
) : ViewModel() {
    private val genres = context.getString(R.string.catalog_fot_one_site_genres)
    private val type = context.getString(R.string.catalog_fot_one_site_type)
    private val statuses = context.getString(R.string.catalog_fot_one_site_statuses)
    private val authors = context.getString(R.string.catalog_fot_one_site_authors)

    var searchText by mutableStateOf("") // Сохранение информации о поисковом запросе
    var sortType by mutableStateOf(DATE) // Тип сортировки
    var isReversed by mutableStateOf(false) // порядок сортировки

    private val _action = MutableStateFlow(true)
    val action = _action.asStateFlow()

    private val _items = MutableStateFlow(emptyList<SiteCatalogElement>())
    val items = _items
        .onEach { list ->
            setAction(true)
            _filters.update {
                listOf(
                    CatalogFilter(
                        name = genres,
                        catalog = list.flatMap { it.genres }.toHashSet().sorted()
                    ),
                    CatalogFilter(
                        name = type,
                        catalog = list.map { it.type }.toHashSet().sorted()
                    ),
                    CatalogFilter(
                        name = statuses,
                        catalog = list.map { it.statusEdition }.toHashSet().sorted()
                    ),
                    CatalogFilter(
                        name = authors,
                        catalog = list.flatMap { it.authors }.toHashSet().sorted()
                    )
                )
            }
        }
        .onEach { list ->
            withDefaultContext {
                siteDao.getItem(siteName)?.let { site ->
                    site.oldVolume = list.size
                    siteDao.update(site)
                }
            }
        }
        // Обработка поискового запроса
        .combine(snapshotFlow { searchText }) { list, search -> list to search }
        .map { (list, search) ->
            if (search.isNotEmpty()) {
                list.filter { item ->
                    item.name.lowercase().contains(searchText.lowercase())
                }
            } else list
        }
        // Обработка фильтров
        .combine(snapshotFlow { selectedNames }) { list, f -> list to f }
        .map { (list, f) ->
            var temp = list
            f.transformToGroup().apply {
                // genres
                get(0).ifNotEmpty { temp = list.filter { it.genres.containsAll(this) } }

                // types
                get(1).ifNotEmpty { temp = list.filter { contains(it.type) } }

                // statuses
                get(2).ifNotEmpty { temp = list.filter { contains(it.statusEdition) } }

                // authors
                get(3).ifNotEmpty { temp = list.filter { it.authors.containsAll(this) } }
            }

            temp
        }
        // Обработка сортировки
        .combine(snapshotFlow { sortType }) { list, s -> list to s }
        .map { (list, sort) ->
            when (sort) {
                DATE -> list.sortedBy { it.dateId } // Сортировать по дате
                NAME -> list.sortedBy { it.name } // Сортировать по имени
                POP -> list.sortedBy { it.populate } // Сортировать по популярности
                else -> list //
            }
        }
        // Обработка направления сортировки и обновление адаптера
        .combine(snapshotFlow { isReversed }) { list, r -> list to r }
        .map { (list, reverse) ->
            if (reverse) list.reversed()
            else list
        }
        .onEach { setAction(false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _filters = MutableStateFlow(emptyList<CatalogFilter>())
    val filters = _filters.asStateFlow()

    var selectedNames by mutableStateOf<List<SelectedName>>(emptyList())

    init {
        update()
    }

    fun update() = viewModelScope.defaultLaunch {
        dbFactory.create(siteCatalogsManager.catalogName(siteName)).apply {
            _items.update { dao.getItems() }
            close()
        }
    }

    fun clearSelected() {
        filters.value.onEach { filter ->
            repeat(filter.selected.size) { index ->
                filter.selected[index] = false
            }

            selectedNames = emptyList()
        }
    }

    // переключение видимости индикатора выполнения фоновой работы
    fun setAction(value: Boolean, service: Boolean = false) = viewModelScope.mainLaunch {
        _action.value = when {
            value -> {
                if (service) CatalogForOneSiteUpdaterService.addIfNotContain(context, siteName)
                true
            }
            CatalogForOneSiteUpdaterService.isContain(siteName).not() -> false
            else -> false
        }
    }

    private fun List<SelectedName>.transformToGroup(): List<List<String>> {
        val list = mutableListOf(emptyList<String>(), emptyList(), emptyList(), emptyList())

        groupBy(keySelector = { it.nameType }, valueTransform = { it.value })
            .forEach { entry ->
                when (entry.key) {
                    genres -> list.add(0, entry.value)
                    type -> list.add(1, entry.value)
                    statuses -> list.add(2, entry.value)
                    authors -> list.add(3, entry.value)
                }
            }

        return list.toList()
    }

    @AssistedFactory
    interface Factory {
        fun create(siteName: String): CatalogViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        const val DATE = 0
        const val NAME = 1
        const val POP = 2

        fun provideFactory(
            assistedFactory: Factory,
            siteName: String,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(siteName) as T
            }
        }
    }
}

@Composable
fun catalogViewModel(siteName: String): CatalogViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as MainActivity,
        MainActivity.ViewModelFactoryProvider::class.java,
    ).catalogViewModelFactory()

    return viewModel(factory = CatalogViewModel.provideFactory(factory, siteName))
}

data class CatalogFilter(
    val name: String,
    val catalog: List<String> = emptyList(),
    val selected: SnapshotStateList<Boolean> =
        mutableStateListOf(*catalog.map { false }.toTypedArray()),
)

data class SelectedName(
    val nameType: String,
    val value: String,
)

private fun List<String>.ifNotEmpty(action: List<String>.() -> Unit) {
    if (isNotEmpty())
        action()
}
