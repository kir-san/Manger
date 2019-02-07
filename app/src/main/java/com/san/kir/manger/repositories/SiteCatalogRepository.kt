package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.manger.room.CatalogDb
import com.san.kir.manger.room.models.SiteCatalogElement
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SiteCatalogRepository(context: Context, catalogName: String) {
    private val db = CatalogDb.getDatabase(context, catalogName)
    private val mDao = db.dao

    fun items() = mDao.getItems()

    fun update(vararg element: SiteCatalogElement) = GlobalScope.launch { mDao.update(*element) }
    fun insert(vararg element: SiteCatalogElement) = GlobalScope.launch { mDao.insert(*element) }
    fun clearDb() = GlobalScope.launch { mDao.deleteAll() }

    fun close() {
        db.close()
    }
}

