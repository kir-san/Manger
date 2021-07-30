package com.san.kir.manger.ui.application_navigation.drawer.catalogs

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kittinunf.result.coroutines.SuspendableResult
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.SiteCatalog
import com.san.kir.manger.repositories.SiteCatalogRepository
import com.san.kir.manger.room.dao.CategoryDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.dao.SiteDao
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.Site
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.SortLibraryUtil
import com.san.kir.manger.utils.enums.MangaFilter
import com.san.kir.manger.utils.enums.SortLibrary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CatalogsViewModel @Inject constructor(
    private val context: Application,
    private val siteDao: SiteDao,
    private val categoryDao: CategoryDao,
    private val mangaDao: MangaDao,
) : ViewModel() {
    val siteList = siteDao.loadItems()

    /*init {
        viewModelScope.launch {
            combine(
                siteDao.loadItems(),
                loadVisibleManga()
            ) { sites, _ ->
                CatalogsViewState(
                    siteItems = sites
                )
            }
                .catch { t -> throw t }
                .collect { _state.value = it }
        }
    }*/

    fun update() = viewModelScope.launch(Dispatchers.Default) {
        ManageSites.CATALOG_SITES.forEach {
            it.isInit = false
            save(it)
        }
    }

    fun site(site: Site): SiteCatalog {
        return ManageSites.CATALOG_SITES.first {
            it.allCatalogName.any { s ->
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
                    val siteCatalogRepository = SiteCatalogRepository(context, site.catalogName)
                    it.oldVolume = siteCatalogRepository.items().size
                    it.volume = site.volume
                    // Обновляем наш сайт в базе данных
                    update(it)
                    siteCatalogRepository.close()
                }
            }
        }
    }

    private fun loadVisibleManga(): Flow<List<List<Manga>>> {
        return combine(categoryDao.loadItems(), mangaDao.flowItems()) { cats, mangas ->
            cats.filter { it.isVisible }.map { cat -> mangas.loadMangas(cat) }
        }
    }

    private fun List<Manga>.loadMangas(cat: Category): List<Manga> {
        val filter = toFilter(cat)
        if (cat.name != CATEGORY_ALL) filter { it.name == cat.name }

        return when (filter) {
            MangaFilter.ADD_TIME_ASC -> sortedBy { it.id }
            MangaFilter.ADD_TIME_DESC -> sortedByDescending { it.id }
            MangaFilter.ABC_SORT_ASC -> sortedBy { it.name }
            MangaFilter.ABC_SORT_DESC -> sortedByDescending { it.name }
            MangaFilter.POPULATE_ASC -> sortedBy { it.populate }
            MangaFilter.POPULATE_DESC -> sortedByDescending { it.populate }
        }
    }

    private fun toFilter(category: Category): MangaFilter {
        return when (SortLibraryUtil.toType(category.typeSort)) {
            SortLibrary.AddTime -> if (category.isReverseSort) MangaFilter.ADD_TIME_DESC
            else MangaFilter.ADD_TIME_ASC

            SortLibrary.AbcSort -> if (category.isReverseSort) MangaFilter.ABC_SORT_DESC
            else MangaFilter.ABC_SORT_ASC

            SortLibrary.Populate -> if (category.isReverseSort) MangaFilter.POPULATE_DESC
            else MangaFilter.POPULATE_ASC
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
