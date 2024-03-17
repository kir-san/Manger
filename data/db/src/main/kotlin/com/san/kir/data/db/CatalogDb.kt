package com.san.kir.data.db

import android.app.Application
import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import com.san.kir.core.utils.DIR
import com.san.kir.core.utils.externalDir
import com.san.kir.data.db.dao.SiteCatalogDao
import com.san.kir.data.db.typeConverters.ListStringConverter
import com.san.kir.data.models.base.SiteCatalogElement
import java.io.File
import javax.inject.Inject

@Database(
    entities = [(SiteCatalogElement::class)],
    version = CatalogDb.VERSION,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = From1to2::class)
    ]
)
@TypeConverters(ListStringConverter::class)
abstract class CatalogDb : RoomDatabase() {
    companion object {
        const val VERSION = 2

        fun getDatabase(
            context: Context,
            catalogName: String,
        ): CatalogDb {
            return Room
                .databaseBuilder(
                    context.applicationContext,
                    CatalogDb::class.java,
                    File(externalDir, "${DIR.CATALOGS}/$catalogName.db").absolutePath
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

    class Factory @Inject constructor(private val context: Application) {
        fun create(catalogName: String): CatalogDb {
            return getDatabase(context, catalogName)
        }
    }
}

/*
Таблица SiteCatalogElement
Удаление полей siteId, isAdded
*/
@DeleteColumn.Entries(
    DeleteColumn(tableName = "items", columnName = "isAdded"),
    DeleteColumn(tableName = "items", columnName = "siteId")
)
internal class From1to2 : AutoMigrationSpec
