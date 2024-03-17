package com.san.kir.data.db

import android.content.ContentValues
import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.OnConflictStrategy
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.san.kir.core.support.CATEGORY_ALL
import com.san.kir.core.support.ChapterFilter
import com.san.kir.core.utils.DIR
import com.san.kir.core.support.MainMenuType
import com.san.kir.core.utils.getFullPath
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MainMenuDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.PlannedDao
import com.san.kir.data.db.dao.SettingsDao
import com.san.kir.data.db.dao.ShikimoriDao
import com.san.kir.data.db.dao.StatisticDao
import com.san.kir.data.db.dao.StorageDao
import com.san.kir.data.db.migrations.From58to59
import com.san.kir.data.db.migrations.From59to60
import com.san.kir.data.db.migrations.From62to63
import com.san.kir.data.db.migrations.migrations
import com.san.kir.data.db.typeConverters.FileConverter
import com.san.kir.data.db.typeConverters.ListLongConverter
import com.san.kir.data.db.typeConverters.ListStringConverter
import com.san.kir.data.db.typeConverters.PlannedPeriodTypeConverter
import com.san.kir.data.db.typeConverters.PlannedTypeTypeConverter
import com.san.kir.data.db.typeConverters.PlannedWeekTypeConverter
import com.san.kir.data.db.typeConverters.ShikimoriMangaConverter
import com.san.kir.data.db.typeConverters.ShikimoriRateConverter
import com.san.kir.data.models.base.Category
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.MainMenuItem
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.PlannedTask
import com.san.kir.data.models.base.Settings
import com.san.kir.data.models.base.ShikiDbManga
import com.san.kir.data.models.base.Statistic
import com.san.kir.data.models.base.Storage
import com.san.kir.data.models.extend.SimplifiedChapter
import com.san.kir.data.models.extend.SimplifiedManga
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import com.san.kir.data.models.extend.SimplifiedStatistic
import timber.log.Timber

@Database(
    entities = [
        Manga::class,
        Chapter::class,
        Category::class,
        Storage::class,
        MainMenuItem::class,
        PlannedTask::class,
        Statistic::class,
        ShikiDbManga::class,
        Settings::class
    ],
    version = 63,
    views = [
        SimplifiedManga::class,
        SimplifiedMangaWithChapterCounts::class,
        SimplifiedStatistic::class,
        SimplifiedChapter::class,
    ],
    autoMigrations = [
        AutoMigration(from = 41, to = 42), // SimplifiedManga add categoryId
        AutoMigration(from = 43, to = 44), // add view PlannedTaskExt
        AutoMigration(from = 44, to = 45), // add view MiniManga
        AutoMigration(from = 46, to = 47), // add table Settings
        AutoMigration(from = 47, to = 48), // add noRead field to view SimplifiedManga
        AutoMigration(from = 48, to = 49), // rename name field in view SimplifiedManga
        AutoMigration(from = 51, to = 52), // add view SimplifiedChapters
        AutoMigration(from = 52, to = 53), // add new field to view SimplifiedChapters
        AutoMigration(from = 53, to = 54), // add new field to view SimplifiedChapters
        AutoMigration(from = 56, to = 57), // add mangas field to table planned_task
        AutoMigration(from = 57, to = 58), // remove view MiniManga
        AutoMigration(from = 58, to = 59, spec = From58to59::class), // remove Site table
        AutoMigration(from = 59, to = 60, spec = From59to60::class), // remove field from Manga
        AutoMigration(from = 60, to = 61), // update SimplifiedManga view
        AutoMigration(from = 62, to = 63, spec = From62to63::class), // remove unused fields from Chapters
    ]
)
@TypeConverters(
    FileConverter::class,
    ListLongConverter::class,
    ListStringConverter::class,
    PlannedTypeTypeConverter::class,
    PlannedWeekTypeConverter::class,
    PlannedPeriodTypeConverter::class,
    ShikimoriRateConverter::class,
    ShikimoriMangaConverter::class,
)
abstract class RoomDB : RoomDatabase() {
    abstract fun mangaDao(): MangaDao
    abstract fun chapterDao(): ChapterDao
    abstract fun plannedDao(): PlannedDao
    abstract fun storageDao(): StorageDao
    abstract fun categoryDao(): CategoryDao
    abstract fun mainMenuDao(): MainMenuDao
    abstract fun statisticDao(): StatisticDao
    abstract fun shikimoriDao(): ShikimoriDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        private const val NAME = "${DIR.PROFILE}/profile.db"

        private lateinit var sDb: RoomDB

        fun getDatabase(context: Context): RoomDB {
            if (!::sDb.isInitialized)
                synchronized(RoomDB::class.java) {
                    if (!::sDb.isInitialized)
                        sDb = Room
                            .databaseBuilder(
                                context.applicationContext,
                                RoomDB::class.java,
                                getFullPath(NAME).path
                            )
                            .addMigrations(*migrations)
                            .addCallback(Callback(context))
                            .build()
                }
            return sDb
        }

        fun getDefaultDatabase(context: Context): RoomDB {
            if (!::sDb.isInitialized)
                synchronized(RoomDB::class.java) {
                    if (!::sDb.isInitialized)
                        sDb = Room
                            .databaseBuilder(
                                context.applicationContext,
                                RoomDB::class.java,
                                "default.db"
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

        Timber.v("db create")

        db.addCategoryAll()
        Timber.v("category ALL was added to db")

        db.addMenuItems(context)
        Timber.v("menuitems was added to db")

    }


    private fun SupportSQLiteDatabase.addCategoryAll() {
        val cat = ContentValues()
        cat.put("name", context.CATEGORY_ALL)
        cat.put("ordering", 0)
        cat.put("isVisible", true)
        cat.put("typeSort", "")
        cat.put("isReverseSort", true)
        cat.put("spanPortrait", 2)
        cat.put("spanLandscape", 3)
        cat.put("isListPortrait", true)
        cat.put("isListLandscape", true)

        insert("categories", OnConflictStrategy.IGNORE, cat)
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

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)

        val result = db.query("SELECT id from settings")
        if (result.count == 0) {
            db.insert("settings", OnConflictStrategy.REPLACE, defaultSettings())
        }
    }

    private fun defaultSettings() = ContentValues().apply {
        put("id", 1)
        put("isIndividual", true)
        put("isTitle", true)
        put("filterStatus", ChapterFilter.ALL_READ_ASC.name)
        put("concurrent", true)
        put("retry", false)
        put("wifi", false)
        put("isFirstLaunch", true)
        put("theme", true)
        put("isShowCategory", true)
        put("editMenu", false)
        put("orientation", Settings.Viewer.Orientation.AUTO_LAND.name)
        put("cutOut", true)
        put("withoutSaveFiles", false)
        put("isLogin", false)
        put("taps", false)
        put("swipes", true)
        put("keys", false)
        put("access_token", "")
        put("token_type", "")
        put("expires_in", 0L)
        put("refresh_token", "")
        put("scope", "")
        put("created_at", 0L)
        put("shikimori_whoami_id", 0)
        put("nickname", "")
        put("avatar", "")
        put("scrollbars", 1)
    }

}
