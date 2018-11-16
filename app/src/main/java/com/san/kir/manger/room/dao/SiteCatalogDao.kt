package com.san.kir.manger.room.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.SiteCatalogElement

@Dao
interface SiteCatalogDao: BaseDao<SiteCatalogElement> {
    @Query("SELECT * FROM items")
    fun getItems(): List<SiteCatalogElement>
}
