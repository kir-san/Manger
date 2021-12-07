package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.data.db.CatalogDb.Companion.getDatabase
import com.san.kir.data.models.SiteCatalogElement
import com.san.kir.data.parsing.SiteCatalogsManager

class SiteCatalogRepository(context: Context, catalogName: String, manager: SiteCatalogsManager) {
    private val db = getDatabase(context, catalogName, manager)
    private val mDao = db.dao

    suspend fun items() = mDao.getItems()
    suspend fun update(vararg element: SiteCatalogElement) = mDao.update(*element)
    suspend fun insert(vararg element: SiteCatalogElement) = mDao.insert(*element)

    fun close() {
        db.close()
    }
}

