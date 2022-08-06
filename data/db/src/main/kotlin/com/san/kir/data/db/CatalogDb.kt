package com.san.kir.data.db

import android.app.Application
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.san.kir.core.utils.externalDir
import com.san.kir.data.db.dao.SiteCatalogDao
import com.san.kir.data.db.typeConverters.ListStringConverter
import com.san.kir.data.models.base.SiteCatalogElement
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

        fun getDatabase(
            context: Context,
            catalogName: String,
        ): CatalogDb {
            return Room
                .databaseBuilder(
                    context.applicationContext,
                    CatalogDb::class.java,
                    File(externalDir, catalogName).absolutePath
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
