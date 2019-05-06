package com.san.kir.manger.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.PagedList
import com.san.kir.manger.repositories.CategoryRepository
import com.san.kir.manger.repositories.DownloadRepository
import com.san.kir.manger.repositories.LatestChapterRepository
import com.san.kir.manger.repositories.MainMenuRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.PlannedRepository
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.repositories.StorageRepository
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.utils.enums.DownloadStatus
import com.san.kir.manger.room.models.LatestChapter
import com.san.kir.manger.room.models.MainMenuItem
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.PlannedTask
import com.san.kir.manger.room.models.Site

class DrawerViewModel(app: Application) : AndroidViewModel(app) {
    private val mMainMenuRepository = MainMenuRepository(app)
    private val mMangaRepository = MangaRepository(app)
    private val mStorageRepository = StorageRepository(app)
    private val mCategorRepository = CategoryRepository(app)
    private val mSiteRepository = SiteRepository(app)
    private val mDownloadRepository = DownloadRepository(app)
    private val mLatestChapterRepository = LatestChapterRepository(app)
    private val mPlannedRepository = PlannedRepository(app)

    fun mainMenuUpdate(vararg mainMenuItem: MainMenuItem) {
        mMainMenuRepository.update(*mainMenuItem)
    }

    fun getMainMenuItems(): List<MainMenuItem> {
        return mMainMenuRepository.getItems()
    }

    fun getMangaData(): LiveData<List<Manga>> {
        return mMangaRepository.loadItems()
    }

    fun getStorageData(): LiveData<Double> {
        mStorageRepository.updateStorageItems()
        return mStorageRepository.loadAllSize()
    }

    fun getCategoryData(): LiveData<List<Category>> {
        return mCategorRepository.loadItems()
    }

    fun getSiteData(): LiveData<PagedList<Site>> {
        return mSiteRepository.loadPagedItems()
    }

    fun getDownloadData(): LiveData<Int> {
        return Transformations.map(mDownloadRepository.loadItems()) { list ->
            list.filter {
                it.status == DownloadStatus.queued ||
                        it.status == DownloadStatus.loading
            }.size
        }
    }

    fun getLatestData(): LiveData<List<LatestChapter>> {
        return mLatestChapterRepository.loadItems()
    }

    fun getPlannedData(): LiveData<PagedList<PlannedTask>> {
        return mPlannedRepository.loadPagedItems()
    }
}

