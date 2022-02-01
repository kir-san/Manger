package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.base.PlannedTask
import com.san.kir.data.models.extend.PlannedTaskExt
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannedDao : BaseDao<PlannedTask> {
    @Query("SELECT * FROM ${PlannedTaskColumn.tableName} " +
                   "ORDER BY ${PlannedTaskColumn.id}")
    fun loadItems(): Flow<List<PlannedTask>>

    @Query(
        "SELECT * FROM ${PlannedTask.tableName} " +
                "WHERE `${PlannedTask.Col.id}` IS :taskId"
    )
    fun itemById(taskId: Long): PlannedTask

    @Query("SELECT * FROM ${PlannedTaskColumn.tableName} " +
                   "WHERE `${PlannedTaskColumn.id}` IS :taskId")
    fun getItem(taskId: Long): PlannedTask

    @Query("SELECT * FROM ${PlannedTaskColumn.tableName} " +
                   "WHERE `${PlannedTaskColumn.id}` IS :taskId")
    fun loadItem(taskId: Long): Flow<PlannedTask>

    @Query(
        "SELECT * FROM ${PlannedTaskExt.viewName} " +
                "ORDER BY ${PlannedTask.Col.id}"
    )
    fun loadExtItems(): Flow<List<PlannedTaskExt>>

    @Query(
        "UPDATE ${PlannedTask.tableName} " +
                "SET ${PlannedTask.Col.isEnabled} = :enable " +
                "WHERE ${PlannedTask.Col.id} = :id"
    )
    fun update(id: Long, enable: Boolean)
}

