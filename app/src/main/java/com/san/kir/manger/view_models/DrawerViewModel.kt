package com.san.kir.manger.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import com.san.kir.manger.repositories.CategoryRepository
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.DownloadRepository
import com.san.kir.manger.repositories.MainMenuRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.PlannedRepository
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.repositories.StorageRepository
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.room.entities.MainMenuItem
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.PlannedTask
import com.san.kir.manger.room.entities.Site
import com.san.kir.manger.utils.enums.DownloadStatus

class DrawerViewModel(app: Application) : AndroidViewModel(app) {
    private val mMainMenuRepository = MainMenuRepository(app)
    private val mMangaRepository = MangaRepository(app)
    private val mStorageRepository = StorageRepository(app)
    private val mCategorRepository = CategoryRepository(app)
    private val mSiteRepository = SiteRepository(app)
    private val mDownloadRepository = DownloadRepository(app)
    private val mChapterRepository = ChapterRepository(app)
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

    fun getLatestData() = mChapterRepository.loadInUpdateItems()

    fun getPlannedData(): LiveData<PagedList<PlannedTask>> {
        return mPlannedRepository.loadPagedItems()
    }
}

