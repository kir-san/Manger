package com.san.kir.manger.ui.application_navigation.statistic.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.san.kir.manger.data.room.dao.MangaDao
import com.san.kir.manger.data.room.dao.StatisticDao
import com.san.kir.manger.data.room.entities.MangaStatistic
import com.san.kir.manger.utils.extensions.sumByLong
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(
    statisticDao: StatisticDao,
    private val mangaDao: MangaDao,
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
