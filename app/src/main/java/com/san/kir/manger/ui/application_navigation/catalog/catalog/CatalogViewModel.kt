package com.san.kir.manger.ui.application_navigation.catalog.catalog

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.san.kir.manger.components.parsing.SiteCatalogsManager
import com.san.kir.manger.room.CatalogDb
import com.san.kir.manger.room.dao.SiteDao
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.services.CatalogForOneSiteUpdaterService
import com.san.kir.manger.ui.application_navigation.catalog.CatalogViewModel.Companion.DATE
import com.san.kir.manger.utils.extensions.startForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val application: Application,
    private val siteDao: SiteDao,
    private val manager: SiteCatalogsManager,
) : ViewModel() {
    companion object {
        const val DATE = 0
        const val NAME = 1
        const val POP = 2
    }

    private var db: CatalogDb? = null

    private val siteCatalog by lazy { manager.catalog.first { it.name == siteName }.catalogName }

    private val searchText = MutableStateFlow("") // Сохранение информации о поисковом запросе
    private val sort = MutableStateFlow(CatalogSort()) // Порядок сортировки и тип сортировки
    private val filters = MutableStateFlow(emptyList<List<String>>()) // фильтры
    private val backupCatalog = MutableStateFlow(emptyList<SiteCatalogElement>())
    private val catalogFilter = MutableStateFlow(emptyList<CatalogFilter>())

    private val _action = MutableStateFlow(true)
    val action: StateFlow<Boolean>
        get() = _action

    private val _state = MutableStateFlow(CatalogViewState())
    val state: StateFlow<CatalogViewState>
        get() = _state

    init {
        viewModelScope.launch(Dispatchers.Default) {
            combine(
                backupCatalog, catalogFilter, searchText, sort, filters
            ) { backupCatalog, catalogFilter, searchText, sort, filters ->
                setAction(true)
                CatalogViewState(
                    items = changeOrder(
                        backupCatalog, sort.type, sort.isReversed, searchText, filters
                    ),
                    filters = catalogFilter,
                    searchText = searchText,
                    sortType = sort.type,
                    isReversed = sort.isReversed
                )
            }.catch { t -> throw t }.collect {
                setAction(false)
                _state.value = it
            }
        }
    }

    fun setSite(
        siteCatalog: String,
    ) = viewModelScope.launch(Dispatchers.Default) {
        setAction(true)

            if (db == null) {
                db = CatalogDb.getDatabase(application, siteCatalog, manager)
            } else if (this@CatalogViewModel.siteCatalog != siteCatalog) {
                db?.close()
                db = CatalogDb.getDatabase(application, siteCatalog, manager)

        }

        this@CatalogViewModel.siteCatalog = siteCatalog

        db?.let {
            val list = it.dao.loadItems().first()
            backupCatalog.value = list
            catalogFilter.value = listOf(
                CatalogFilter(
                    name = "Жанры",
                    catalog = list.flatMap { it.genres }.toHashSet().sorted()
                ),
                CatalogFilter(
                    name = "Тип манги",
                    catalog = list.map { it.type }.toHashSet().sorted()
                ),
                CatalogFilter(
                    name = "Статус манги",
                    catalog = list.map { it.statusEdition }.toHashSet().sorted()
                ),
                CatalogFilter(
                    name = "Авторы",
                    catalog = list.flatMap { it.authors }.toHashSet().sorted()
                )
            )
        }

        siteDao.getItem(siteCatalog)?.let { site ->
            site.oldVolume = backupCatalog.value.size
            siteDao.update(site)
        }
    }

    private fun changeOrder(
        catalog: List<SiteCatalogElement>,
        sortType: Int, isReversed: Boolean, searchText: String,
        filters: List<List<String>>
    ): List<SiteCatalogElement> {
        var list = catalog

        // Обработка поискового запроса
        if (searchText.isNotEmpty()) {
            list = list.filter {
                it.name.lowercase(Locale.ROOT).contains(searchText.lowercase(Locale.ROOT))
            }
        }

        // Обработка фильтров
        if (!filters.all { it.isEmpty() }) {
            val genres = filters[0]
            val types = filters[1]
            val statuses = filters[2]
            val authors = filters[3]

            if (genres.isNotEmpty())
                list = list.filter { it.genres.containsAll(genres) }

            if (types.isNotEmpty())
                list = list.filter { types.contains(it.type) }

            if (statuses.isNotEmpty())
                list = list.filter { statuses.contains(it.statusEdition) }

            if (authors.isNotEmpty())
                list = list.filter { it.authors.containsAll(authors) }
        }

        // Обработка сортировки
        list = when (sortType) {
            DATE -> list.sortedBy { it.dateId } // Сортировать по дате
            NAME -> list.sortedBy { it.name } // Сортировать по имени
            POP -> list.sortedBy { it.populate } // Сортировать по популярности
            else -> list // .sortedBy { it.dateId } // Сортировать по дате
        }

        // Обработка направления сортировки и обновление адаптера
        return if (isReversed) list.reversed() else list
    }

    fun clearSelected() {
        filters.value = emptyList()
    }

    fun setAction(value: Boolean, service: Boolean = false) {
        if (value) {
            if (service && !CatalogForOneSiteUpdaterService.isContain(siteCatalog))
                application
                    .startForegroundService<CatalogForOneSiteUpdaterService>("catalogName" to siteCatalog)
            _action.value = true
        } else if (!CatalogForOneSiteUpdaterService.isContain(siteCatalog)) {
            _action.value = false
        }
    }

    fun setSortType(value: Int) {
        sort.value = CatalogSort(isReversed = sort.value.isReversed, type = value)
    }

    fun setIsReversed(value: Boolean) {
        sort.value = CatalogSort(isReversed = value, type = sort.value.type)
    }

    fun setSearchText(value: String) {
        searchText.value = value
    }

    // Смена фильтра списка
    fun changeFilter(pageIndex: Int, isAdd: Boolean, item: String) {

        val newNamed = filters.value.toMutableList()

        if (newNamed.isEmpty()) {
            repeat(4) { newNamed.add(emptyList()) }
        }

        if (isAdd) {
            newNamed[pageIndex] = newNamed[pageIndex] + item
        } else {
            newNamed[pageIndex] = newNamed[pageIndex] - item
        }
//        filters.value = newNamed
        viewModelScope.launch { filters.emit(newNamed) }
    }
}

data class CatalogSort(
    val isReversed: Boolean = false,
    val type: Int = DATE,
)

data class CatalogViewState(
    val items: List<SiteCatalogElement> = emptyList(),
    val filters: List<CatalogFilter> = emptyList(),
    val isReversed: Boolean = false,
    val sortType: Int = DATE,
    val searchText: String = ""
)

data class CatalogFilter(
    val name: String,
    val catalog: List<String> = emptyList(),
    val selectedName: MutableList<String> = mutableListOf(),
    val selected: List<MutableState<Boolean>> = catalog.map { mutableStateOf(false) }
)
