package com.san.kir.manger.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import android.content.Context
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.room.dao.SiteCatalogDao
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.room.type_converters.ListStringConverter
import com.san.kir.manger.utils.enums.DIR
import com.san.kir.manger.utils.getFullPath

@Database(
    entities = [(SiteCatalogElement::class)],
    version = CatalogDb.VERSION,
    exportSchema = false
)
@TypeConverters(ListStringConverter::class)
abstract class CatalogDb : RoomDatabase() {
    companion object {
        const val VERSION = 1
        val NAME: (String) -> String = { "${DIR.CATALOGS}/$it.db" }

        fun getDatabase(context: Context, catalogName: String): CatalogDb {
            val first = ManageSites.CATALOG_SITES.first { it.catalogName == catalogName }
            var catName = first.catalogName
            first.allCatalogName
                .firstOrNull { getFullPath(NAME(it)).exists() }
                ?.also {
                    catName = it
                }

            return Room
                .databaseBuilder(
                    context.applicationContext,
                    CatalogDb::class.java,
                    CatalogDb.NAME(catName)
                )
                .addMigrations(*CatalogDb.Migrate.migrations)
                .allowMainThreadQueries()
                .build()
        }
    }

    abstract val dao: SiteCatalogDao

    object Migrate {
        val migrations: Array<Migration> = arrayOf()
    }
}
