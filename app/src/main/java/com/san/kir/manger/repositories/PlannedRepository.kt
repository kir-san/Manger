package com.san.kir.manger.repositories

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.content.Context
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.room.models.PlannedTask
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
}
