package com.san.kir.manger.components.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.san.kir.manger.repositories.CategoryRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.PlannedRepository
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.PlannedTask

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

    suspend fun getCategoryItems() = mCategoryRepository.items()
    suspend fun getSiteItems() = mSiteRepository.getItems()
}

