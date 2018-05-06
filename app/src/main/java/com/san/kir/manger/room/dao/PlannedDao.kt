package com.san.kir.manger.room.dao

import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.PlannedTask
import com.san.kir.manger.room.models.PlannedTaskColumn

@Dao
interface PlannedDao : BaseDao<PlannedTask> {
    @Query("SELECT * FROM ${PlannedTaskColumn.tableName} ORDER BY ${PlannedTaskColumn.id}")
    fun loadPlannedTasks(): DataSource.Factory<Int, PlannedTask>

    @Query("SELECT * FROM ${PlannedTaskColumn.tableName} ORDER BY ${PlannedTaskColumn.id}")
    fun loadPTasks(): List<PlannedTask>

    @Query("SELECT * FROM ${PlannedTaskColumn.tableName} WHERE `${PlannedTaskColumn.addedTime}` IS :taskId ORDER BY ${PlannedTaskColumn.id}")
    fun loadPlannedTask(taskId: Long): PlannedTask
}

fun PlannedDao.loadPagedPlannedTasks() =
    LivePagedListBuilder(loadPlannedTasks(), 20).build()
