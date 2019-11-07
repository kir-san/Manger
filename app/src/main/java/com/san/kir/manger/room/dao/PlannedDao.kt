package com.san.kir.manger.room.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.san.kir.manger.room.entities.PlannedTask
import com.san.kir.manger.room.entities.PlannedTaskColumn

@Dao
interface PlannedDao : BaseDao<PlannedTask> {
    @Query("SELECT * FROM ${PlannedTaskColumn.tableName} ORDER BY ${PlannedTaskColumn.id}")
    fun pagedItems(): DataSource.Factory<Int, PlannedTask>

    @Query("SELECT * FROM ${PlannedTaskColumn.tableName} ORDER BY ${PlannedTaskColumn.id}")
    fun getItems(): List<PlannedTask>

    @Query("SELECT * FROM ${PlannedTaskColumn.tableName} WHERE `${PlannedTaskColumn.addedTime}` IS :taskId ORDER BY ${PlannedTaskColumn.id}")
    fun getItem(taskId: Long): PlannedTask
}

