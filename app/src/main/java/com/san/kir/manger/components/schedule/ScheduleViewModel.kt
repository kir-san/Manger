package com.san.kir.manger.components.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.PlannedRepository
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.PlannedTask

class ScheduleViewModel(app: Application) : AndroidViewModel(app) {
    private val mPlannedRepository = PlannedRepository(app)
    private val mMangaRepository = MangaRepository(app)

//    fun getPlannedItems(): LiveData<PagedList<PlannedTask>> {
//        return mPlannedRepository.loadPagedItems()
//    }

    fun plannedUpdate(item: PlannedTask) {
        mPlannedRepository.update(item)
    }

    fun getMangaItems(): List<Manga> {
        return mMangaRepository.getItems()
    }

    suspend fun update(item: Manga) = mMangaRepository.update(item)

}

