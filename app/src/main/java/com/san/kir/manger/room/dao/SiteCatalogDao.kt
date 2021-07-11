package com.san.kir.manger.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.manger.room.entities.SiteCatalogElement
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteCatalogDao: BaseDao<SiteCatalogElement> {
    @Query("SELECT * FROM items")
    suspend fun getItems(): List<SiteCatalogElement>

    @Query("SELECT * FROM items")
    fun loadItems(): Flow<List<SiteCatalogElement>>

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}
