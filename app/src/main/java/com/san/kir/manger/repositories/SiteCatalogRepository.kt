package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.data.parsing.SiteCatalogsManager
import com.san.kir.manger.data.room.entities.SiteCatalogElement

class SiteCatalogRepository(context: Context, catalogName: String, manager: com.san.kir.data.parsing.SiteCatalogsManager) {
    private val db = com.san.kir.data.db.CatalogDb.getDatabase(context, catalogName, manager)
    private val mDao = db.dao

    suspend fun items() = mDao.getItems()
    suspend fun update(vararg element: SiteCatalogElement) = mDao.update(*element)
    suspend fun insert(vararg element: SiteCatalogElement) = mDao.insert(*element)

    fun close() {
        db.close()
    }
}

