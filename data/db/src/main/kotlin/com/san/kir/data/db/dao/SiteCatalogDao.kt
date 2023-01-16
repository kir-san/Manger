package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.base.SiteCatalogElement
import com.san.kir.data.models.extend.MiniCatalogItem

@Dao
interface SiteCatalogDao : BaseDao<SiteCatalogElement> {
    @Query("SELECT * FROM items")
    suspend fun items(): List<SiteCatalogElement>

    @Query(
        "SELECT id, catalogName, name, statusEdition, shotLink, " +
                "link, genres, type, authors, dateId, populate FROM items"
    )
    suspend fun miniItems(): List<MiniCatalogItem>

    @Query("SELECT * FROM items WHERE id=:id")
    suspend fun itemById(id: Long): SiteCatalogElement

    @Query("SELECT COUNT(id) FROM items")
    suspend fun itemsCount(): Int

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}
