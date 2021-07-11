package com.san.kir.manger.ui.global_search

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.SiteCatalog
import com.san.kir.manger.room.CatalogDb
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.ui.AbstractMangaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections.emptyList

class GlobalSearchViewModel(app: Application) : AbstractMangaViewModel(app) {
    private val searchText = MutableStateFlow("")
    private val backupCatalog = MutableStateFlow(emptyList<SiteCatalogElement>())

    private val mangaDao by lazy { getDatabase(app).mangaDao }

    // Индикатор фоновой работы
    private val _action = MutableStateFlow(true)
    val action: StateFlow<Boolean>
        get() = _action

    private val _state = MutableStateFlow(GlobalSearchViewState())
    val state: StateFlow<GlobalSearchViewState>
        get() = _state

    init {
        viewModelScope.launch(Dispatchers.Default) {
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
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                _action.value = true
                backupCatalog.value =
                    ManageSites.CATALOG_SITES
                        .flatMap { getItems(it) }
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
        withContext(Dispatchers.Default) {
            val db = CatalogDb.getDatabase(getApplication(), siteCatalog.catalogName)
            val items = db.dao.getItems()
            db.close()
            items
        }

    fun setSearchText(value: String) {
        searchText.value = value
    }

    data class GlobalSearchViewState(
        val items: List<SiteCatalogElement> = emptyList(),
        val searchText: String = ""
    )
}
