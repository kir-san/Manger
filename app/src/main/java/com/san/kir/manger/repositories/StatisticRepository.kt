package com.san.kir.manger.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import android.content.Context
import androidx.lifecycle.asFlow
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.utils.extensions.sumByLong
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class StatisticRepository(context: Context) {
    private val db = getDatabase(context)
    private val mStatisticDao = db.statisticDao

    fun pagedItems() = mStatisticDao.pagedItems()
    fun loadItems() = mStatisticDao.loadItems()

    suspend fun getItems() = mStatisticDao.getItems()
    suspend fun getItem(unic: String) = mStatisticDao.getItem(unic)

    suspend fun update(vararg site: MangaStatistic) = mStatisticDao.update(*site)
    suspend fun insert(vararg site: MangaStatistic) = mStatisticDao.insert(*site)
    suspend fun delete(vararg site: MangaStatistic) = mStatisticDao.delete(*site)

    fun loadPagedItems() = LivePagedListBuilder(pagedItems(), 30).build().asFlow()
    fun loadAllTime() = loadItems().map { list -> list.sumByLong { it.allTime } }
}
