package com.san.kir.manger.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.manger.room.entities.SiteCatalogElement

@Dao
interface SiteCatalogDao: BaseDao<SiteCatalogElement> {
    @Query("SELECT * FROM items")
    suspend fun getItems(): List<SiteCatalogElement>

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}
