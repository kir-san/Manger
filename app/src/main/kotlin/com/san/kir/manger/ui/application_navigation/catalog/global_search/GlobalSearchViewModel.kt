package com.san.kir.manger.ui.application_navigation.catalog.global_search

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.coroutines.mainLaunch
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.data.db.CatalogDb
import com.san.kir.data.models.base.SiteCatalogElement
import com.san.kir.data.parsing.SiteCatalog
import com.san.kir.data.parsing.SiteCatalogsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Collections.emptyList
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(InternalCoroutinesApi::class)
@HiltViewModel
class GlobalSearchViewModel @Inject constructor(
    private val application: Application,
    private val manager: SiteCatalogsManager,
) : ViewModel() {

    private val searchText = MutableStateFlow("")

    private val backupCatalog = MutableStateFlow(emptyList<SiteCatalogElement>())

    val items = searchText
        .combine(backupCatalog) { searchText, list ->
            _action.value = true
            changeOrder(list, searchText)
        }
        .catch { t -> throw t }
        .onEach { _action.value = false }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())



    // Индикатор фоновой работы
    private val _action = MutableStateFlow(true)
    val action = _action.asStateFlow()

    init {
        viewModelScope.defaultLaunch {
            kotlin.runCatching {
                _action.value = true
                backupCatalog.value = manager.catalog.flatMap { getItems(it) }
            }.onFailure { exception ->
                exception.printStackTrace()
            }
        }
    }

    private fun changeOrder(
        catalog: List<SiteCatalogElement>,
        searchText: String
    ): List<SiteCatalogElement> {
        if (searchText.isNotEmpty()) {
            return catalog.filter { item -> item.name.contains(searchText, true) }
        }
        return catalog
    }

    private suspend fun getItems(siteCatalog: SiteCatalog): List<SiteCatalogElement> =
        withDefaultContext {
            val db = CatalogDb.getDatabase(application, manager.catalogName(siteCatalog.catalogName))
            val items = db.dao.getItems()
            db.close()
            items
        }

    // Добавлена задержка поиска при вводе запроса
    private var job: Job? = null
    fun setSearchText(value: String) {
        job?.cancel()
        job = viewModelScope.launch {
            delay(1.seconds)
            searchText.value = value
        }
    }
}
