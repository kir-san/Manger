package com.san.kir.manger.data.room

import android.app.Application
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.san.kir.manger.App
import com.san.kir.manger.components.parsing.SiteCatalogsManager
import com.san.kir.manger.data.room.dao.SiteCatalogDao
import com.san.kir.manger.data.room.entities.SiteCatalogElement
import com.san.kir.manger.data.room.type_converters.ListStringConverter
import com.san.kir.manger.utils.enums.DIR
import com.san.kir.manger.utils.extensions.getFullPath
import java.io.File
import javax.inject.Inject

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

        fun getDatabase(
            context: Context,
            catalogName: String,
            manager: SiteCatalogsManager,
        ): CatalogDb {
            val first = manager.catalog.first { it.catalogName == catalogName }
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
                    File(App.externalDir, NAME(catName)).absolutePath
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

    class Factory @Inject constructor(
        private val context: Application,
        private val manager: SiteCatalogsManager,
    ) {
        fun create(siteName: String): CatalogDb {
            val first = manager.catalog.first { it.name == siteName }

            var catName = first.catalogName

            first.allCatalogName
                .firstOrNull { getFullPath(NAME(it)).exists() }
                ?.also { catName = it }

            return Room
                .databaseBuilder(
                    context.applicationContext,
                    CatalogDb::class.java,
                    File(App.externalDir, NAME(catName)).absolutePath
                )
                .addMigrations(*Migrate.migrations)
                .allowMainThreadQueries()
                .build()
        }
    }
}
