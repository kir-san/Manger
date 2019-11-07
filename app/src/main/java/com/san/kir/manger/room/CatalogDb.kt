package com.san.kir.manger.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.room.dao.SiteCatalogDao
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.room.type_converters.ListStringConverter
import com.san.kir.manger.utils.enums.DIR
import com.san.kir.manger.utils.extensions.getFullPath

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
                    NAME(catName)
                )
                .addMigrations(*Migrate.migrations)
                .allowMainThreadQueries()
                .build()
        }
    }

    abstract val dao: SiteCatalogDao

    object Migrate {
        val migrations: Array<Migration> = arrayOf()
    }


}
