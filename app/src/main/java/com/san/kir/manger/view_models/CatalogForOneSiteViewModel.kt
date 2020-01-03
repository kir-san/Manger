package com.san.kir.manger.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.san.kir.manger.components.parsing.SiteCatalog
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.SiteCatalogRepository
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.Site
import com.san.kir.manger.room.entities.SiteCatalogElement
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

    suspend fun siteUpdate(site: Site) = mSiteRepository.update(site)

    suspend fun items(site: SiteCatalog, force: Boolean = false): List<SiteCatalogElement> {
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

        if (force) {
            mSiteCatalogItems = mSiteCatalogRepository.items()
        }

        return mSiteCatalogItems
    }

    suspend fun update(siteCatalogElement: SiteCatalogElement) =
        mSiteCatalogRepository.update(siteCatalogElement)


    fun getMangaItem(shortLink: String): Manga {
        return mMangaRepository.getItemWhereShortLink(shortLink)
    }

    suspend fun mangaUpdate(manga: Manga) = mMangaRepository.update(manga)


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
