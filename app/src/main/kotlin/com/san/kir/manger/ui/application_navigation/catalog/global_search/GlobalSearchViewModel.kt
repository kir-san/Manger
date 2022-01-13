package com.san.kir.manger.ui.application_navigation.catalog.global_search

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.data.db.CatalogDb
import com.san.kir.data.models.base.SiteCatalogElement
import com.san.kir.data.parsing.SiteCatalog
import com.san.kir.data.parsing.SiteCatalogsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import java.util.Collections.emptyList
import javax.inject.Inject

@OptIn(InternalCoroutinesApi::class)
@HiltViewModel
class GlobalSearchViewModel @Inject constructor(
    private val application: Application,
    private val manager: SiteCatalogsManager,
) : ViewModel() {
    private val searchText = MutableStateFlow("")
    private val backupCatalog = MutableStateFlow(emptyList<SiteCatalogElement>())

    // Индикатор фоновой работы
    private val _action = MutableStateFlow(true)
    val action: StateFlow<Boolean>
        get() = _action

    private val _state = MutableStateFlow(GlobalSearchViewState())
    val state: StateFlow<GlobalSearchViewState>
        get() = _state

    init {
        viewModelScope.defaultLaunch {
            combine(
                backupCatalog, searchText
            ) { backupCatalog, searchText ->
                _action.value = true
                val lstate = GlobalSearchViewState(
                    items = changeOrder(backupCatalog, searchText),
                    searchText = searchText,
                )
                lstate
            }.catch { t -> throw t }.collect {
                _action.value = false
                _state.value = it
            }
        }
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
            val db = CatalogDb.getDatabase(application, siteCatalog.catalogName, manager)
            val items = db.dao.getItems()
            db.close()
            items
        }

    fun setSearchText(value: String) {
        searchText.value = value
    }

}

data class GlobalSearchViewState(
    val items: List<SiteCatalogElement> = emptyList(),
    val searchText: String = ""
)
