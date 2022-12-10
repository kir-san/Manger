package com.san.kir.background.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.WorkersDb
import com.san.kir.data.models.base.CatalogTask
import javax.inject.Inject

class WorkersRepository @Inject constructor(
    private val db: WorkersDb.Instance,
) {
    val catalog = db.catalog.loadItems()
    fun loadItem(name: String) = db.catalog.loadItemByName(name)
    suspend fun item(name: String) = withIoContext { db.catalog.itemByName(name) }
    suspend fun add(item: CatalogTask) = withIoContext { db.catalog.insert(item) }
    suspend fun remove(item: CatalogTask) = withIoContext { db.catalog.removeById(item.id) }
    suspend fun update(item: CatalogTask) = withIoContext { db.catalog.update(item) }
}

