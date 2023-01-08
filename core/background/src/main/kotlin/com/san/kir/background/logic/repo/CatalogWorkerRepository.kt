package com.san.kir.background.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.WorkersDb
import com.san.kir.data.models.base.CatalogTask
import javax.inject.Inject

class CatalogWorkerRepository @Inject constructor(
    private val db: WorkersDb.Instance,
) : BaseWorkerRepository<CatalogTask> {

    override val catalog = db.catalog.loadItems()

    override suspend fun remove(item: CatalogTask) {
        withIoContext { db.catalog.removeById(item.id) }
    }

    override suspend fun clear() = withIoContext { db.catalog.clear() }

    fun loadTask(name: String) = db.catalog.loadItemByName(name)
    suspend fun task(name: String) = withIoContext { db.catalog.itemByName(name) }
    suspend fun add(item: CatalogTask) = withIoContext { db.catalog.insert(item) }
    suspend fun update(item: CatalogTask) = withIoContext { db.catalog.update(item) }
}
