package com.san.kir.data.db

import android.content.ContentValues
import android.content.Context
import androidx.room.Database
import androidx.room.OnConflictStrategy
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.san.kir.core.support.CATEGORY_ALL
import com.san.kir.core.support.DIR
import com.san.kir.core.utils.externalDir
import com.san.kir.core.utils.log
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.DownloadDao
import com.san.kir.data.db.dao.LatestChapterDao
import com.san.kir.data.db.dao.MainMenuDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.PlannedDao
import com.san.kir.data.db.dao.SiteDao
import com.san.kir.data.db.dao.StatisticDao
import com.san.kir.data.db.dao.StorageDao
import com.san.kir.data.db.migrations.migrations
import com.san.kir.data.db.type_converters.ChapterFilterTypeConverter
import com.san.kir.data.db.type_converters.DownloadStateTypeConverter
import com.san.kir.data.db.type_converters.FileConverter
import com.san.kir.data.db.type_converters.ListStringConverter
import com.san.kir.data.db.type_converters.MainMenuTypeConverter
import com.san.kir.data.db.type_converters.PlannedPeriodTypeConverter
import com.san.kir.data.db.type_converters.PlannedTypeTypeConverter
import com.san.kir.data.db.type_converters.PlannedWeekTypeConverter
import com.san.kir.data.models.Category
import com.san.kir.data.models.Chapter
import com.san.kir.data.models.DownloadItem
import com.san.kir.data.models.LatestChapter
import com.san.kir.data.models.MainMenuItem
import com.san.kir.data.models.Manga
import com.san.kir.data.models.MangaStatistic
import com.san.kir.data.models.PlannedTask
import com.san.kir.data.models.Site
import com.san.kir.data.models.Storage
import com.san.kir.data.models.columns.CategoryColumn
import com.san.kir.core.support.MainMenuType
import java.io.File

@Database(
    entities =
    [(Site::class),
        (Manga::class),
        (Chapter::class),
        (Category::class),
        (Storage::class),
        (MainMenuItem::class),
        (LatestChapter::class),
        (DownloadItem::class),
        (PlannedTask::class),
        (MangaStatistic::class)
    ],
    version = RoomDB.VERSION,
    exportSchema = false
)
@TypeConverters(
    FileConverter::class,
    ListStringConverter::class,
    MainMenuTypeConverter::class,
    PlannedTypeTypeConverter::class,
    PlannedWeekTypeConverter::class,
    PlannedPeriodTypeConverter::class,
    ChapterFilterTypeConverter::class,
    DownloadStateTypeConverter::class,
)
abstract class RoomDB : RoomDatabase() {
    abstract val siteDao: SiteDao

    abstract val mangaDao: MangaDao
    abstract val chapterDao: ChapterDao
    abstract val plannedDao: PlannedDao
    abstract val storageDao: StorageDao
    abstract val categoryDao: CategoryDao
    abstract val downloadDao: DownloadDao
    abstract val mainMenuDao: MainMenuDao
    abstract val latestChapterDao: LatestChapterDao
    abstract val statisticDao: StatisticDao

    companion object {
        const val NAME = "${DIR.PROFILE}/profile.db"
        const val VERSION = 37

        private lateinit var sDb: RoomDB

        fun getDatabase(context: Context): RoomDB {
            if (!::sDb.isInitialized)
                synchronized(RoomDB::class.java) {
                    if (!::sDb.isInitialized)
                        sDb = Room.databaseBuilder(
                            context.applicationContext,
                            RoomDB::class.java,
                            File(externalDir, NAME).absolutePath
                        )
                            .addMigrations(*migrations)
                            .addCallback(Callback(context))
                            .build()
                }
            return sDb
        }
    }
}

class Callback(private val context: Context) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        log("db create")

        db.addCategoryAll()
        log("category ALL was added to db")

        db.addMenuItems(context)
        log("menuitems was added to db")

    }


    private fun SupportSQLiteDatabase.addCategoryAll() {
        val cat = ContentValues()
        cat.put(CategoryColumn.name, context.CATEGORY_ALL)
        cat.put(CategoryColumn.order, 0)
        cat.put(CategoryColumn.isVisible, true)
        cat.put(CategoryColumn.typeSort, "")
        cat.put(CategoryColumn.isReverseSort, true)
        cat.put(CategoryColumn.spanPortrait, 2)
        cat.put(CategoryColumn.spanLandscape, 3)
        cat.put(CategoryColumn.isLargePortrait, true)
        cat.put(CategoryColumn.isLargeLandscape, true)

        insert(CategoryColumn.tableName, OnConflictStrategy.IGNORE, cat)
    }

    private fun SupportSQLiteDatabase.addMenuItems(ctx: Context) {
        MainMenuType.values()
            .filter { it != MainMenuType.Default }
            .forEachIndexed { index, type ->

                val item = ContentValues()
                item.put("name", ctx.getString(type.stringId()))
                item.put("isVisible", true)
                item.put("'order'", index)
                item.put("'type'", type.name)

                insert("mainmenuitems", OnConflictStrategy.REPLACE, item)
            }
    }
}
