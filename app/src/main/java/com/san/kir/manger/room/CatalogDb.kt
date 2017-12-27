package com.san.kir.manger.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import com.san.kir.manger.room.DAO.SiteCatalogDao
import com.san.kir.manger.room.TypeConverters.ListStringConverter
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.utils.DIR

@Database(entities = arrayOf(SiteCatalogElement::class),
          version = CatalogDb.VERSION,
          exportSchema = false)
@TypeConverters(ListStringConverter::class)
abstract class CatalogDb : RoomDatabase() {
    companion object {
        const val VERSION = 1
        val NAME: (String) -> String = { "${DIR.CATALOGS}/$it.db" }
    }

    abstract val dao: SiteCatalogDao

    object migrate {
        val migrations: Array<Migration> = arrayOf()
    }
}
