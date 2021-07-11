package com.san.kir.manger.ui.drawer.catalogs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.kittinunf.result.coroutines.SuspendableResult
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.SiteCatalog
import com.san.kir.manger.data.LibraryRepository
import com.san.kir.manger.repositories.SiteCatalogRepository
import com.san.kir.manger.room.entities.Site
import com.san.kir.manger.room.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CatalogsViewModel(app: Application) : AndroidViewModel(app) {
    private val libraryRepository = LibraryRepository(app)
    private val siteDao = getDatabase(app).siteDao


    private val _state = MutableStateFlow(CatalogsViewState())
    val state: StateFlow<CatalogsViewState>
        get() = _state

    init {
        viewModelScope.launch {
            combine(
                siteDao.loadItems(),
                libraryRepository.loadVisibleManga()
            ) { sites, _ ->
                CatalogsViewState(
                    siteItems = sites
                )
            }
                .catch { t -> throw t }
                .collect { _state.value = it }
        }
    }

    fun update() = viewModelScope.launch(Dispatchers.Default) {
        ManageSites.CATALOG_SITES.forEach {
            it.isInit = false
            save(it)
        }
    }

    fun site(site: Site): SiteCatalog {
        return ManageSites.CATALOG_SITES.first {
            it.allCatalogName.any { s->
                s == site.catalogName
            }
        }
    }

    suspend fun updateSiteInfo(site: SiteCatalog) = withContext(Dispatchers.Default) {
        SuspendableResult.of<Unit, Exception> {
            site.init()
            // Находим в базе данных наш сайт
            with(siteDao) {
                getItem(site.name)?.let {
                    // Сохраняем новое значение количества элементов
                    val siteCatalogRepository = SiteCatalogRepository(getApplication(), site.catalogName)
                    it.oldVolume = siteCatalogRepository.items().size
                    it.volume = site.volume
                    // Обновляем наш сайт в базе данных
                    update(it)
                    siteCatalogRepository.close()
                }
            }
        }
    }

    private suspend fun save(site: SiteCatalog) {
        val s = siteDao.getItem(site.name)
        if (s != null) {
            s.volume = site.volume
            s.host = site.host
            s.catalogName = site.catalogName
            siteDao.update(s)
        } else {
            siteDao.insert(
                Site(
                    id = 0,
                    name = site.name,
                    host = site.host,
                    catalogName = site.catalogName,
                    volume = site.volume,
                    oldVolume = site.oldVolume,
                    siteID = site.id
                )
            )
        }
    }
}

data class CatalogsViewState(
    val siteItems: List<Site> = emptyList()
)
