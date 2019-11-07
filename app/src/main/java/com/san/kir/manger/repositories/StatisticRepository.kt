package com.san.kir.manger.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import android.content.Context
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.utils.extensions.sumByLong
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StatisticRepository(context: Context) {
    private val db = getDatabase(context)
    private val mStatisticDao = db.statisticDao

    fun pagedItems(): DataSource.Factory<Int, MangaStatistic> {
        return mStatisticDao.pagedItems()
    }

    fun loadItems(): LiveData<List<MangaStatistic>> {
        return mStatisticDao.loadItems()
    }

    fun getItems(): List<MangaStatistic> {
        return mStatisticDao.getItems()
    }

    fun getItem(unic: String): MangaStatistic {
        return mStatisticDao.getItem(unic)
    }

    fun update(vararg site: MangaStatistic) = GlobalScope.launch { mStatisticDao.update(*site) }
    fun insert(vararg site: MangaStatistic) = GlobalScope.launch { mStatisticDao.insert(*site) }
    fun delete(vararg site: MangaStatistic) = GlobalScope.launch { mStatisticDao.delete(*site) }

    fun loadPagedItems(): LiveData<PagedList<MangaStatistic>> {
        return LivePagedListBuilder(pagedItems(), 30).build()
    }

    fun loadAllTime(): LiveData<Long> {
        return Transformations.map(loadItems()) { list -> list.sumByLong { it.allTime } }
    }
}
