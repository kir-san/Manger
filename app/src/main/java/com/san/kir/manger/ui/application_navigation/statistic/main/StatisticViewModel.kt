package com.san.kir.manger.ui.application_navigation.statistic.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.StatisticDao
import com.san.kir.manger.data.room.entities.MangaStatistic
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(
    statisticDao: com.san.kir.data.db.dao.StatisticDao,
    private val mangaDao: com.san.kir.data.db.dao.MangaDao,
) : ViewModel() {
    fun manga(item: MangaStatistic) = mangaDao.loadItem(item.manga).filterNotNull()
    val allTime = statisticDao.loadItems().map { list -> list.sumByLong { it.allTime } }
    val allItems = Pager(
        config = PagingConfig(
            pageSize = 30,
            enablePlaceholders = true,
            maxSize = 100,
        )
    ) {
        statisticDao.allItemsByAllTime()
    }.flow.cachedIn(viewModelScope)

}
