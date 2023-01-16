package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.base.CatalogTask
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogTaskDao : BaseDao<CatalogTask> {

    @Query("SELECT * FROM catalog_task")
    fun loadItems(): Flow<List<CatalogTask>>

    @Query("SELECT * FROM catalog_task WHERE name=:name")
    fun loadItemByName(name: String): Flow<CatalogTask?>

    @Query("SELECT * FROM catalog_task WHERE name=:name")
    fun itemByName(name: String): CatalogTask?

    @Query("DELETE FROM catalog_task WHERE id=:id")
    fun removeById(id: Long)

    @Query("DELETE FROM catalog_task")
    fun clear()
}
