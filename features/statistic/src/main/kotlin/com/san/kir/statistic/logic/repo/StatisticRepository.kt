package com.san.kir.statistic.logic.repo

import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.StatisticDao
import javax.inject.Inject

class StatisticRepository @Inject constructor(
    private val statisticDao: StatisticDao,
    private val mangaDao: MangaDao,
) {
    val items = statisticDao.loadSimpleItems()
    val allTime = statisticDao.loadAllTime()

    suspend fun delete(itemId: Long) = withDefaultContext { statisticDao.delete(itemId) }
    suspend fun item(itemId: Long) = withDefaultContext { statisticDao.itemById(itemId) }
    suspend fun mangaName(mangaId: Long) = withDefaultContext { mangaDao.itemById(mangaId) }
}
