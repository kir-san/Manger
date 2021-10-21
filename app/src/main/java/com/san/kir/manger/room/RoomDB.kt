package com.san.kir.manger.room

import android.content.ContentValues
import android.content.Context
import androidx.room.Database
import androidx.room.OnConflictStrategy
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.san.kir.manger.App
import com.san.kir.manger.room.columns.CategoryColumn
import com.san.kir.manger.room.dao.CategoryDao
import com.san.kir.manger.room.dao.ChapterDao
import com.san.kir.manger.room.dao.DownloadDao
import com.san.kir.manger.room.dao.LatestChapterDao
import com.san.kir.manger.room.dao.MainMenuDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.dao.PlannedDao
import com.san.kir.manger.room.dao.SiteDao
import com.san.kir.manger.room.dao.StatisticDao
import com.san.kir.manger.room.dao.StorageDao
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.room.entities.LatestChapter
import com.san.kir.manger.room.entities.MainMenuItem
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.room.entities.PlannedTask
import com.san.kir.manger.room.entities.Site
import com.san.kir.manger.room.entities.Storage
import com.san.kir.manger.room.migrations.migrations
import com.san.kir.manger.room.type_converters.ChapterFilterTypeConverter
import com.san.kir.manger.room.type_converters.DownloadStateTypeConverter
import com.san.kir.manger.room.type_converters.FileConverter
import com.san.kir.manger.room.type_converters.ListStringConverter
import com.san.kir.manger.room.type_converters.MainMenuTypeConverter
import com.san.kir.manger.room.type_converters.PlannedPeriodTypeConverter
import com.san.kir.manger.room.type_converters.PlannedTypeTypeConverter
import com.san.kir.manger.room.type_converters.PlannedWeekTypeConverter
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.enums.DIR
import com.san.kir.manger.utils.enums.MainMenuType
import com.san.kir.manger.utils.extensions.log
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
    companion object {
        const val NAME = "${DIR.PROFILE}/profile.db"
        const val VERSION = 37
    }

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
}

private lateinit var sDb: RoomDB

fun getDatabase(context: Context): RoomDB {
    if (!::sDb.isInitialized)
        synchronized(RoomDB::class.java) {
            if (!::sDb.isInitialized)
                sDb = Room.databaseBuilder(
                        context.applicationContext,
                        RoomDB::class.java,
                        File(App.externalDir, RoomDB.NAME).absolutePath
                    )
                    .addMigrations(*migrations)
                    .addCallback(Callback(context))
                    .build()
        }
    return sDb
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
        cat.put(CategoryColumn.name, CATEGORY_ALL)
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
