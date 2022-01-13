package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.base.SiteCatalogElement

@Dao
interface SiteCatalogDao : BaseDao<SiteCatalogElement> {
    @Query("SELECT * FROM items")
    suspend fun getItems(): List<SiteCatalogElement>

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}
