package com.san.kir.manger.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.san.kir.manger.repositories.CategoryRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.PlannedRepository
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.PlannedTask
import com.san.kir.manger.room.models.Site

class AddEditPlannedTaskViewModel(app: Application) : AndroidViewModel(app) {
    private val mPlannedRepository = PlannedRepository(app)
    private val mMangaRepository = MangaRepository(app)
    private val mCategoryRepository = CategoryRepository(app)
    private val mSiteRepository = SiteRepository(app)

    fun plannedUpdate(task: PlannedTask) {
        mPlannedRepository.update(task)
    }

    fun plannedInsert(task: PlannedTask) {
        mPlannedRepository.insert(task)
    }

    fun getMangaItems(): List<Manga> {
        return mMangaRepository.getItems()
    }

    fun getCategoryItems(): List<Category> {
        return mCategoryRepository.getItems()
    }

    fun getSiteItems(): List<Site> {
        return mSiteRepository.getItems()
    }
}

