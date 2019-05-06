package com.san.kir.manger.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
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
import com.san.kir.manger.room.migrations.migrations
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.LatestChapter
import com.san.kir.manger.room.models.MainMenuItem
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.MangaStatistic
import com.san.kir.manger.room.models.PlannedTask
import com.san.kir.manger.room.models.Site
import com.san.kir.manger.room.models.Storage
import com.san.kir.manger.room.type_converters.ChapterFilterTypeConverter
import com.san.kir.manger.room.type_converters.FileConverter
import com.san.kir.manger.room.type_converters.ListStringConverter
import com.san.kir.manger.room.type_converters.MainMenuTypeConverter
import com.san.kir.manger.utils.enums.DIR

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
    ChapterFilterTypeConverter::class
)
abstract class RoomDB : RoomDatabase() {
    companion object {
        const val NAME = "${DIR.PROFILE}/profile.db"
        const val VERSION = 34
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
                    RoomDB.NAME
                )
                    .addMigrations(*migrations)
                    .build()
        }
    return sDb
}
