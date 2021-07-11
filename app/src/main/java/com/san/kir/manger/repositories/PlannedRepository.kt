package com.san.kir.manger.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.san.kir.manger.room.entities.PlannedTask
import com.san.kir.manger.room.getDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PlannedRepository(context: Context) {
    private val db = getDatabase(context)
    private val mPlannedDao = db.plannedDao

    fun pagedItems(): DataSource.Factory<Int, PlannedTask> {
        return mPlannedDao.pagedItems()
    }

    fun getItems(): List<PlannedTask> {
        return mPlannedDao.getItems()
    }

    fun getItem(taskId: Long): PlannedTask {
        return mPlannedDao.getItem(taskId)
    }

    fun insert(vararg plannedTask: PlannedTask) =
        GlobalScope.launch { mPlannedDao.insert(*plannedTask) }

    fun update(vararg plannedTask: PlannedTask) =
        GlobalScope.launch { mPlannedDao.update(*plannedTask) }

    fun delete(vararg plannedTask: PlannedTask) =
        GlobalScope.launch { mPlannedDao.delete(*plannedTask) }

    fun loadPagedItems(): LiveData<PagedList<PlannedTask>> {
        return LivePagedListBuilder(pagedItems(), 30).build()
    }

    fun loadItems() = mPlannedDao.loadItems()
}
