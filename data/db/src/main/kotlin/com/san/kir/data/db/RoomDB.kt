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
import com.san.kir.core.support.DIR
import com.san.kir.core.support.MainMenuType
import com.san.kir.core.utils.getFullPath
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MainMenuDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.PlannedDao
import com.san.kir.data.db.dao.SettingsDao
import com.san.kir.data.db.dao.ShikimoriDao
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
import com.san.kir.data.db.type_converters.ShikimoriMangaConverter
import com.san.kir.data.db.type_converters.ShikimoriRateConverter
import com.san.kir.data.models.base.Category
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.MainMenuItem
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.PlannedTask
import com.san.kir.data.models.base.Settings
import com.san.kir.data.models.base.ShikiDbManga
import com.san.kir.data.models.base.Site
import com.san.kir.data.models.base.Statistic
import com.san.kir.data.models.base.Storage
import com.san.kir.data.models.extend.MiniManga
import com.san.kir.data.models.extend.PlannedTaskExt
import com.san.kir.data.models.extend.SimplifiedManga
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import timber.log.Timber

@Database(
    entities =
    [
        (Site::class),
        (Manga::class),
        (Chapter::class),
        (Category::class),
        (Storage::class),
        (MainMenuItem::class),
        (PlannedTask::class),
        (Statistic::class),
        (ShikiDbManga::class),
        (Settings::class)
    ],
    version = RoomDB.VERSION,
    views = [
        SimplifiedManga::class,
        SimplifiedMangaWithChapterCounts::class,
        PlannedTaskExt::class,
        MiniManga::class,
    ],
    autoMigrations = [
        AutoMigration(from = 41, to = 42), // SimplifiedManga add categoryId
        AutoMigration(from = 43, to = 44), // add view PlannedTaskExt
        AutoMigration(from = 44, to = 45), // add view MiniManga
        AutoMigration(from = 46, to = 47), // add table Settings
    ]
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
    ShikimoriRateConverter::class,
    ShikimoriMangaConverter::class,
)
abstract class RoomDB : RoomDatabase() {
    abstract val siteDao: SiteDao
    abstract val mangaDao: MangaDao
    abstract val chapterDao: ChapterDao
    abstract val plannedDao: PlannedDao
    abstract val storageDao: StorageDao
    abstract val categoryDao: CategoryDao
    abstract val mainMenuDao: MainMenuDao
    abstract val statisticDao: StatisticDao
    abstract val shikimoriDao: ShikimoriDao
    abstract val settingsDao: SettingsDao

    companion object {
        const val NAME = "${DIR.PROFILE}/profile.db"
        const val VERSION = 47

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
                            .createFromAsset("database/default.db")
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
        cat.put(Category.Col.name, context.CATEGORY_ALL)
        cat.put(Category.Col.order, 0)
        cat.put(Category.Col.isVisible, true)
        cat.put(Category.Col.typeSort, "")
        cat.put(Category.Col.isReverseSort, true)
        cat.put(Category.Col.spanPortrait, 2)
        cat.put(Category.Col.spanLandscape, 3)
        cat.put(Category.Col.isLargePortrait, true)
        cat.put(Category.Col.isLargeLandscape, true)

        insert(Category.tableName, OnConflictStrategy.IGNORE, cat)
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

        val result = db.query("SELECT id from ${Settings.tableName}")
        if (result.count == 0) {
            db.insert(Settings.tableName, OnConflictStrategy.REPLACE, defaultSettings())
        }
    }

    private fun defaultSettings() = ContentValues().apply {
        put(Settings.Col.id, 1)
        put(Settings.Col.isIndividual, true)
        put(Settings.Col.isTitle, true)
        put(Settings.Col.filterStatus, ChapterFilter.ALL_READ_ASC.name)
        put(Settings.Col.concurrent, true)
        put(Settings.Col.retry, false)
        put(Settings.Col.wifi, false)
        put(Settings.Col.isFirstLaunch, true)
        put(Settings.Col.theme, true)
        put(Settings.Col.isShowCategory, true)
        put(Settings.Col.editMenu, false)
        put(Settings.Col.orientation, Settings.Viewer.Orientation.AUTO_LAND.name)
        put(Settings.Col.cutOut, true)
        put(Settings.Col.withoutSaveFiles, false)
        put(Settings.Col.isLogin, false)
        put(Settings.Col.taps, false)
        put(Settings.Col.swipes, true)
        put(Settings.Col.keys, false)
        put(Settings.Col.accessToken, "")
        put(Settings.Col.tokenType, "")
        put(Settings.Col.expiresIn, 0L)
        put(Settings.Col.refreshToken, "")
        put(Settings.Col.scope, "")
        put(Settings.Col.createdAt, 0L)
        put(Settings.Col.shikimoriWhoamiId, 0)
        put(Settings.Col.nickname, "")
        put(Settings.Col.avatar, "")
    }

}
