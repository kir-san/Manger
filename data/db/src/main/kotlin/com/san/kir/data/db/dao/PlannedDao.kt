package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.base.PlannedTask
import com.san.kir.data.models.columns.PlannedTaskColumn
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannedDao : BaseDao<PlannedTask> {
    @Query("SELECT * FROM ${PlannedTaskColumn.tableName} " +
                   "ORDER BY ${PlannedTaskColumn.id}")
    fun loadItems(): Flow<List<PlannedTask>>

    @Query("SELECT * FROM ${PlannedTaskColumn.tableName} " +
                   "ORDER BY ${PlannedTaskColumn.id}")
    fun getItems(): List<PlannedTask>

    @Query("SELECT * FROM ${PlannedTaskColumn.tableName} " +
                   "WHERE `${PlannedTaskColumn.id}` IS :taskId")
    fun getItem(taskId: Long): PlannedTask

    @Query("SELECT * FROM ${PlannedTaskColumn.tableName} " +
                   "WHERE `${PlannedTaskColumn.id}` IS :taskId")
    fun loadItem(taskId: Long): Flow<PlannedTask>
}

