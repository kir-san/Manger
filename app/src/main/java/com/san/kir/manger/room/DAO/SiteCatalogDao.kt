package com.san.kir.manger.room.DAO

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.SiteCatalogElement

@Dao
interface SiteCatalogDao: BaseDao<SiteCatalogElement> {
    @Query("SELECT * FROM items")
    fun loadItems(): List<SiteCatalogElement>
}
