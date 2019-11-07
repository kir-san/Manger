package com.san.kir.manger.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.github.kittinunf.result.coroutines.SuspendableResult
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.SiteCatalog
import com.san.kir.manger.repositories.SiteCatalogRepository
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.room.entities.Site
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SiteCatalogViewModel(private val app: Application) : AndroidViewModel(app) {
    private val mSiteRepository = SiteRepository(app)

    fun getSiteItems(): LiveData<PagedList<Site>> {
        return mSiteRepository.loadPagedItems()
    }

    fun updateSitesInfo() = viewModelScope.launch(Dispatchers.Default) {
        ManageSites.CATALOG_SITES.forEach {
            it.isInit = false
            save(it)
        }
    }

    fun update() = viewModelScope.launch(Dispatchers.Default) {
        ManageSites.CATALOG_SITES.forEach {
            it.isInit = false
            save(it)
        }
    }

    suspend fun updateSiteInfo(site: SiteCatalog) = withContext(Dispatchers.Default) {
        SuspendableResult.of<Unit, Exception> {
            site.init()
            // Находим в базе данных наш сайт
            with(mSiteRepository) {
                getItem(site.name)?.let {
                    // Сохраняем новое значение количества элементов
                    val siteCatalogRepository = SiteCatalogRepository(app, site.catalogName)
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
        val s = mSiteRepository.getItem(site.name)
        if (s != null) {
            s.volume = site.volume
            s.host = site.host
            s.catalogName = site.catalogName
            mSiteRepository.update(s)
        } else {
            mSiteRepository.insert(
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

