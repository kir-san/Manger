package com.san.kir.manger.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.paging.PagedList
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.PlannedRepository
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.PlannedTask

class ScheduleViewModel(app: Application) : AndroidViewModel(app) {
    private val mPlannedRepository = PlannedRepository(app)
    private val mMangaRepository = MangaRepository(app)

    fun getPlannedItems(): LiveData<PagedList<PlannedTask>> {
        return mPlannedRepository.loadPagedItems()
    }

    fun plannedUpdate(item: PlannedTask) {
        mPlannedRepository.update(item)
    }

    fun getMangaItems(): List<Manga> {
        return mMangaRepository.getItems()
    }

    fun mangaUpdate(item: Manga) {
        mMangaRepository.update(item)
    }
}

