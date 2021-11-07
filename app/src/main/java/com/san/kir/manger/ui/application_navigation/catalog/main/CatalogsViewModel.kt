package com.san.kir.manger.ui.application_navigation.catalog.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.ankofork.startService
import com.san.kir.manger.components.parsing.SiteCatalog
import com.san.kir.manger.components.parsing.SiteCatalogsManager
import com.san.kir.manger.di.DefaultDispatcher
import com.san.kir.manger.repositories.SiteCatalogRepository
import com.san.kir.manger.room.dao.CategoryDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.dao.SiteDao
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.Site
import com.san.kir.manger.services.CatalogForOneSiteUpdaterService
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.SortLibraryUtil
import com.san.kir.manger.utils.enums.MangaFilter
import com.san.kir.manger.utils.enums.SortLibrary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CatalogsViewModel @Inject constructor(
    private val context: Application,
    private val siteDao: SiteDao,
    private val categoryDao: CategoryDao,
    private val mangaDao: MangaDao,
    private val manager: SiteCatalogsManager,
    @DefaultDispatcher private val default: CoroutineDispatcher
) : ViewModel() {
    val siteList = siteDao.loadItems()

    fun update() = viewModelScope.launch(default) {
        manager.catalog.forEach {
            it.isInit = false
            save(it)
        }
    }

    fun site(site: Site): SiteCatalog {
        return manager.catalog.first {
            it.allCatalogName.any { s ->
                s == site.catalogName
            }
        }
    }

    suspend fun updateSiteInfo(site: SiteCatalog) = withContext(default) {
        kotlin.runCatching {
            site.init()
            // Находим в базе данных наш сайт
            with(siteDao) {
                getItem(site.name)?.let {
                    // Сохраняем новое значение количества элементов
                    val siteCatalogRepository =
                        SiteCatalogRepository(context, site.catalogName, manager)
                    it.oldVolume = siteCatalogRepository.items().size
                    it.volume = site.volume
                    // Обновляем наш сайт в базе данных
                    update(it)
                    siteCatalogRepository.close()
                }
            }
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
                    oldVolume = site.volume,
                    siteID = site.id
                )
            )
        }
    }

    fun updateCatalogs() {
        manager.catalog.forEach {
            if (!CatalogForOneSiteUpdaterService.isContain(it.catalogName))
                context.startService<CatalogForOneSiteUpdaterService>("catalogName" to it.catalogName)
        }
    }
}
