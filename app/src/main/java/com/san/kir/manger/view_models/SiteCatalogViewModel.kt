package com.san.kir.manger.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.paging.PagedList
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.SiteCatalog
import com.san.kir.manger.extending.asyncCtx
import com.san.kir.manger.repositories.SiteCatalogRepository
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.room.models.Site
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class SiteCatalogViewModel(private val app: Application) : AndroidViewModel(app), CoroutineScope {
    private val job = Job()
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    override val coroutineContext: CoroutineContext
        get() = dispatcher + job

    private val mSiteRepository = SiteRepository(app)

    fun getSiteItems(): LiveData<PagedList<Site>> {
        return mSiteRepository.loadPagedItems()
    }

    fun updateSitesInfo() = launch {
        ManageSites.CATALOG_SITES.forEach {
            it.isInit = false
            save(it)
        }
    }

    fun update() = launch {
        ManageSites.CATALOG_SITES.forEach {
            it.isInit = false
            save(it)
        }
    }

    fun updateSiteInfo(site: SiteCatalog) = asyncCtx {
        runCatching {
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

    private fun save(site: SiteCatalog) {
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

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}

