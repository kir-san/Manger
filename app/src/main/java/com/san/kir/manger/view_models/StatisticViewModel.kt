package com.san.kir.manger.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.StatisticRepository
import com.san.kir.manger.room.entities.MangaStatistic

class StatisticViewModel(app: Application) : AndroidViewModel(app) {
    private val mStatisticRepository = StatisticRepository(app)
    private val mMangaRepository = MangaRepository(app)

    fun getStatisticAllTime() = mStatisticRepository.loadAllTime()
    fun getStatisticPagedItems() = mStatisticRepository.loadPagedItems()
    suspend fun getStatisticItem(string: String) = mStatisticRepository.getItem(string)

    suspend fun getMangaItemOrNull(item: MangaStatistic) =
        mMangaRepository.getItemOrNull(item.manga)
}

