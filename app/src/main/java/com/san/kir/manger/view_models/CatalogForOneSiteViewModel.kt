package com.san.kir.manger.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.san.kir.manger.components.parsing.SiteCatalog
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.SiteCatalogRepository
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.Site
import com.san.kir.manger.room.models.SiteCatalogElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class CatalogForOneSiteViewModel(private val app: Application) : AndroidViewModel(app),
                                                                 CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val mSiteRepository = SiteRepository(app)
    private val mMangaRepository = MangaRepository(app)

    private lateinit var mSiteCatalog: SiteCatalog
    private lateinit var mSiteCatalogRepository: SiteCatalogRepository
    private var mSiteCatalogItems: List<SiteCatalogElement> = listOf()

    fun getSiteItem(name: String): Site? {
        return mSiteRepository.getItem(name)
    }

    fun siteUpdate(site: Site) {
        mSiteRepository.update(site)
    }

    fun getSiteCatalogItems(site: SiteCatalog): List<SiteCatalogElement> {
        if (!::mSiteCatalogRepository.isInitialized) {
            mSiteCatalog = site
            mSiteCatalogRepository = SiteCatalogRepository(app, site.catalogName)
            mSiteCatalogItems = mSiteCatalogRepository.items()
        } else {
            if (site.catalogName != mSiteCatalog.catalogName) {
                mSiteCatalog = site
                mSiteCatalogRepository.close()
                mSiteCatalogRepository = SiteCatalogRepository(app, site.catalogName)
                mSiteCatalogItems = mSiteCatalogRepository.items()
            }
        }

        return mSiteCatalogItems
    }

    fun siteCatalogUpdate(siteCatalogElement: SiteCatalogElement) {
        mSiteCatalogRepository.update(siteCatalogElement)
    }

    fun getMangaItem(mangaUnic: String): Manga {
        return mMangaRepository.getItem(mangaUnic)
    }

    fun mangaUpdate(manga: Manga) {
        mMangaRepository.update(manga)
    }

    fun mangaContain(item: SiteCatalogElement): Boolean {
        return mMangaRepository.contain(item)
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
        if (::mSiteCatalogRepository.isInitialized)
            mSiteCatalogRepository.close()
    }

}
