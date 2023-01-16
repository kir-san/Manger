package com.san.kir.background.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.CatalogDb
import com.san.kir.data.models.base.SiteCatalogElement
import com.san.kir.data.parsing.SiteCatalogsManager
import javax.inject.Inject

class CatalogRepository @Inject constructor(
    private val db: CatalogDb.Factory,
    private val manager: SiteCatalogsManager,
) {
    suspend fun save(name: String, items: List<SiteCatalogElement>) = withIoContext {
        db.create(manager.catalogName(name)).apply {
            dao.deleteAll()
            dao.insert(*items.toTypedArray())
            close()
        }
    }
}
