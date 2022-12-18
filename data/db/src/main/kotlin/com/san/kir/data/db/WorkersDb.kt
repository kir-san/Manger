package com.san.kir.data.db

import android.app.Application
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.san.kir.data.db.dao.CatalogTaskDao
import com.san.kir.data.db.dao.MangaTaskDao
import com.san.kir.data.db.typeConverters.ListStringConverter
import com.san.kir.data.models.base.CatalogTask
import com.san.kir.data.models.base.MangaTask
import javax.inject.Inject

@Database(
    entities = [CatalogTask::class, MangaTask::class],
    version = 1,
    autoMigrations = []
)
@TypeConverters(ListStringConverter::class)
abstract class WorkersDb : RoomDatabase() {
    companion object {
        private lateinit var sDb: WorkersDb

        fun getDatabase(context: Context): WorkersDb {
            if (::sDb.isInitialized.not())
                synchronized(WorkersDb::class.java) {
                    if (::sDb.isInitialized.not())
                        sDb = Room
                            .databaseBuilder(
                                /* context = */ context.applicationContext,
                                /* klass = */ WorkersDb::class.java,
                                /* name = */ WorkersDb::class.java.name + ".db"
                            )
                            .addMigrations(*Migrate.migrations)
                            .build()
                }

            return sDb
        }
    }

    abstract val catalogDao: CatalogTaskDao
    abstract val mangasDao: MangaTaskDao

    object Migrate {
        val migrations: Array<Migration> = arrayOf()
    }

    class Instance @Inject constructor(context: Application) {
        val db = getDatabase(context)
        val catalog = db.catalogDao
        val mangas = db.mangasDao
    }
}
