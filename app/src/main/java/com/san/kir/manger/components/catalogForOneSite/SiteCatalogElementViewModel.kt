package com.san.kir.manger.components.catalogForOneSite

import android.arch.persistence.room.Room
import com.san.kir.manger.App
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.room.CatalogDb
import com.san.kir.manger.room.models.SiteCatalogElement

object SiteCatalogElementViewModel {
    private var elements: CatalogDb? = null
    private var siteId = -1

    fun setSiteId(siteId: Int): SiteCatalogElementViewModel {
        if (this.siteId != siteId) {
            elements?.close()
            elements = Room.databaseBuilder(App.context,
                                            CatalogDb::class.java,
                                            CatalogDb.NAME(ManageSites.CATALOG_SITES[siteId].catalogName))
                    .addMigrations(*CatalogDb.Migrate.migrations)
                    .allowMainThreadQueries()
                    .build()
            this.siteId = siteId
        }
        return SiteCatalogElementViewModel
    }

    fun items() = elements?.dao?.getItems() ?: emptyList()

    fun update(element: SiteCatalogElement) = elements?.dao?.update(element)

    fun insert(element: SiteCatalogElement) = elements?.dao?.insert(element)

    fun clearDb() = elements?.dao?.let { it.delete(*it.getItems().toTypedArray()) }
}

