package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.base.PlannedTask
import com.san.kir.data.models.extend.PlannedTaskExt
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannedDao : BaseDao<PlannedTask> {

    // Получение flow с количеством элементов в таблице
    @Query("SELECT COUNT(*) FROM ${PlannedTask.tableName}")
    fun loadItemsCount(): Flow<Int>

    // Получение flow с элементов по его id
    @Query(
        "SELECT * FROM ${PlannedTask.tableName} " +
                "WHERE ${PlannedTask.Col.id} IS :taskId"
    )
    fun loadItemById(taskId: Long): Flow<PlannedTask>

    // Получение flow со списком всех элементов из view
    @Query(
        "SELECT * FROM ${PlannedTaskExt.viewName} " +
                "ORDER BY ${PlannedTask.Col.id}"
    )
    fun loadExtItems(): Flow<List<PlannedTaskExt>>

    // Получение элемента по его id
    @Query(
        "SELECT * FROM ${PlannedTask.tableName} " +
                "WHERE ${PlannedTask.Col.id} IS :taskId"
    )
    suspend fun itemById(taskId: Long): PlannedTask

    // Обновление поля isEnable
    @Query(
        "UPDATE ${PlannedTask.tableName} " +
                "SET ${PlannedTask.Col.isEnabled} = :enable " +
                "WHERE ${PlannedTask.Col.id} = :id"
    )
    suspend fun update(id: Long, enable: Boolean)
}

