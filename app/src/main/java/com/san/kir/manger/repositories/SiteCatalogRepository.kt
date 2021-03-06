package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.manger.room.CatalogDb
import com.san.kir.manger.room.entities.SiteCatalogElement

class SiteCatalogRepository(context: Context, catalogName: String) {
    private val db = CatalogDb.getDatabase(context, catalogName)
    private val mDao = db.dao

    suspend fun items() = mDao.getItems()
    suspend fun update(vararg element: SiteCatalogElement) = mDao.update(*element)
    suspend fun insert(vararg element: SiteCatalogElement) = mDao.insert(*element)
    suspend fun clearDb() = mDao.deleteAll()

    fun close() {
        db.close()
    }
}

