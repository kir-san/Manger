package com.san.kir.manger.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.StatisticRepository
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.MangaStatistic

class StatisticViewModel(app: Application) : AndroidViewModel(app) {
    private val mStatisticRepository = StatisticRepository(app)
    private val mMangaRepository = MangaRepository(app)

    fun getStatisticAllTime(): LiveData<Long> {
        return mStatisticRepository.loadAllTime()
    }

    fun getStatisticPagedItems(): LiveData<PagedList<MangaStatistic>> {
        return mStatisticRepository.loadPagedItems()
    }

    fun getMangaItemOrNull(item: MangaStatistic): Manga? {
        return mMangaRepository.getItemOrNull(item.manga)
    }

    fun getStatisticItem(string: String): MangaStatistic {
        return mStatisticRepository.getItem(string)
    }
}

