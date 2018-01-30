package com.san.kir.manger.room

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import com.san.kir.manger.room.DAO.CategoryDao
import com.san.kir.manger.room.DAO.ChapterDao
import com.san.kir.manger.room.DAO.DownloadDao
import com.san.kir.manger.room.DAO.LatestChapterDao
import com.san.kir.manger.room.DAO.MainMenuDao
import com.san.kir.manger.room.DAO.MangaDao
import com.san.kir.manger.room.DAO.SiteDao
import com.san.kir.manger.room.DAO.StorageDao
import com.san.kir.manger.room.TypeConverters.FileConverter
import com.san.kir.manger.room.TypeConverters.MainMenuTypeConverter
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.LatestChapter
import com.san.kir.manger.room.models.MainMenuItem
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.Site
import com.san.kir.manger.room.models.Storage
import com.san.kir.manger.utils.DIR

@Database(entities =
          [(Site::class),
              (Manga::class),
              (Chapter::class),
              (Category::class),
              (Storage::class),
              (MainMenuItem::class),
              (LatestChapter::class),
              (DownloadItem::class)],
          version = RoomDB.VERSION,
          exportSchema = false)
@TypeConverters(FileConverter::class,
                MainMenuTypeConverter::class)
abstract class RoomDB : RoomDatabase() {
    companion object {
        val NAME = "${DIR.PROFILE}/profile.db"
        const val VERSION = 24
    }

    abstract val siteDao: SiteDao
    abstract val mangaDao: MangaDao
    abstract val chapterDao: ChapterDao
    abstract val storageDao: StorageDao
    abstract val categoryDao: CategoryDao
    abstract val mainMenuDao: MainMenuDao
    abstract val downloadDao: DownloadDao
    abstract val latestChapterDao: LatestChapterDao

    object Migrate {
        private fun migrate(from: Int, to: Int,
                            action: ((SupportSQLiteDatabase) -> Unit)? = null) =
                object : Migration(from, to) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        action?.invoke(database)
                    }
                }

        val migrations: Array<Migration> = arrayOf(
                migrate(9, 10) {
                    it.execSQL("ALTER TABLE manga RENAME TO tmp_manga")
                    it.execSQL("CREATE TABLE `manga` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `unic` TEXT NOT NULL, `host` TEXT NOT NULL, `name` TEXT NOT NULL, `authors` TEXT NOT NULL, `logo` TEXT NOT NULL, `about` TEXT NOT NULL, `categories` TEXT NOT NULL, `genres` TEXT NOT NULL, `path` TEXT NOT NULL, `status` TEXT NOT NULL, `site` TEXT NOT NULL, `color` INTEGER NOT NULL)")
                    it.execSQL("INSERT INTO manga(id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color) SELECT id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color FROM tmp_manga")
                    it.execSQL("DROP TABLE tmp_manga")
                },
                migrate(10, 11) {
                    it.execSQL("ALTER TABLE chapters RENAME TO tmp_chapters")
                    it.execSQL("CREATE TABLE `chapters` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `manga` TEXT NOT NULL, `name` TEXT NOT NULL, `date` TEXT NOT NULL, `path` TEXT NOT NULL, `isRead` INTEGER NOT NULL, `site` TEXT NOT NULL, `progress` INTEGER NOT NULL)")
                    it.execSQL("INSERT INTO chapters(id, manga, name, date, path, isRead, site, progress) SELECT id, manga, name, date, path, isRead, site, progress FROM tmp_chapters")
                    it.execSQL("DROP TABLE tmp_chapters")
                },
                migrate(11, 12) {
                    it.execSQL("ALTER TABLE categories RENAME TO tmp_categories")
                    it.execSQL("CREATE TABLE `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `order` INTEGER NOT NULL, `isVisible` INTEGER NOT NULL, `typeSort` TEXT NOT NULL, `isReverseSort` INTEGER NOT NULL)")
                    it.execSQL("INSERT INTO `categories`(`id`,`name`,`order`,`isVisible`,`typeSort`,`isReverseSort`) SELECT `id`,`name`,`order`,`isVisible`,`typeSort`,`isReverseSort` FROM tmp_categories")
                    it.execSQL("DROP TABLE tmp_categories")
                },
                migrate(12, 13) {
                    it.execSQL("ALTER TABLE latestChapters RENAME TO tmp_latestChapters")
                    it.execSQL("CREATE TABLE `latestChapters` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `manga` TEXT NOT NULL, `name` TEXT NOT NULL, `date` TEXT NOT NULL, `path` TEXT NOT NULL, `site` TEXT NOT NULL)")
                    it.execSQL("INSERT INTO `latestChapters` (`id`, `manga`, `name`, `date`, `path`, `site`) SELECT `id`, `manga`, `name`, `date`, `path`, `site` FROM tmp_latestChapters")
                    it.execSQL("DROP TABLE tmp_latestChapters")
                },
                migrate(13, 14) {
                    it.execSQL("DROP TABLE IF EXISTS `mainmenuitems`")
                    it.execSQL("CREATE TABLE IF NOT EXISTS `mainmenuitems` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` INTEGER NOT NULL, `isVisible` INTEGER NOT NULL, `order` INTEGER NOT NULL)")
                },
                migrate(14, 15) {
                    it.execSQL("ALTER TABLE sites RENAME TO tmp_sites")
                    it.execSQL("CREATE TABLE `sites` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `count` INTEGER NOT NULL)")
                    it.execSQL("INSERT INTO  `sites` (`id` , `name`, `count`) SELECT `id` , `name`, `count` FROM tmp_sites")
                    it.execSQL("DROP TABLE tmp_sites")
                },
                migrate(15, 16) {
                    it.execSQL("CREATE TABLE `StorageDir` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `file` TEXT NOT NULL, `size` INTEGER NOT NULL, `countDir` INTEGER NOT NULL)")
                    it.execSQL("CREATE TABLE `StorageItem` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `path` TEXT NOT NULL, `sizeFull` INTEGER NOT NULL, `sizeRead` INTEGER NOT NULL, `isNew` INTEGER NOT NULL, `catalogName` TEXT NOT NULL)")
                },
                migrate(16, 17),
                migrate(17, 18) {
                    it.execSQL("DROP TABLE IF EXISTS `mainmenuitems`")
                    it.execSQL("CREATE TABLE IF NOT EXISTS `mainmenuitems` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `isVisible` INTEGER NOT NULL, `order` INTEGER NOT NULL, `type` TEXT NOT NULL)")
                },
                migrate(18, 19) {
                    it.execSQL("DROP TABLE IF EXISTS `sites`")
                    it.execSQL("CREATE TABLE `sites` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `host` TEXT NOT NULL, `catalogName` TEXT NOT NULL, `volume` INTEGER NOT NULL, `oldVolume` INTEGER NOT NULL, `siteID` INTEGER NOT NULL)")
                },
                migrate(19, 20) {
                    it.execSQL("DROP TABLE IF EXISTS `StorageDir`")
                    it.execSQL("DROP TABLE IF EXISTS `StorageItem`")
                    it.execSQL("CREATE TABLE `StorageItem` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `path` TEXT NOT NULL, `sizeFull` REAL NOT NULL, `sizeRead` REAL NOT NULL, `isNew` INTEGER NOT NULL, `catalogName` TEXT NOT NULL)")
                },
                migrate(20, 21) {
                    it.execSQL("CREATE TABLE IF NOT EXISTS `downloads` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `link` TEXT NOT NULL, `path` TEXT NOT NULL, `totalPages` INTEGER NOT NULL, `downloadPages` INTEGER NOT NULL, `totalSize` INTEGER NOT NULL, `downloadSize` INTEGER NOT NULL, `totalTime` INTEGER NOT NULL, `status` INTEGER NOT NULL, `order` INTEGER NOT NULL)")
                },
                migrate(21, 22) {
                    it.execSQL("DROP TABLE IF EXISTS `downloads`")
                    it.execSQL("CREATE TABLE IF NOT EXISTS `downloads` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `link` TEXT NOT NULL, `path` TEXT NOT NULL, `totalPages` INTEGER NOT NULL, `downloadPages` INTEGER NOT NULL, `totalSize` INTEGER NOT NULL, `downloadSize` INTEGER NOT NULL, `totalTime` INTEGER NOT NULL, `status` INTEGER NOT NULL, `order` INTEGER NOT NULL)")
                },
                migrate(22, 23) {
                    it.execSQL("ALTER TABLE categories RENAME TO tmp_categories")
                    it.execSQL("CREATE TABLE `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `order` INTEGER NOT NULL, `isVisible` INTEGER NOT NULL, `typeSort` TEXT NOT NULL, `isReverseSort` INTEGER NOT NULL, `spanPortrait` INTEGER NOT NULL DEFAULT 2, 'spanLandscape' INTEGER NOT NULL DEFAULT 3, `isListPortrait` INTEGER NOT NULL DEFAULT 1, `isListLandscape` INTEGER NOT NULL DEFAULT 1)")
                    it.execSQL("INSERT INTO `categories`(`id`,`name`,`order`,`isVisible`,`typeSort`,`isReverseSort`) SELECT `id`,`name`,`order`,`isVisible`,`typeSort`,`isReverseSort` FROM tmp_categories")
                    it.execSQL("DROP TABLE tmp_categories")
                },
                migrate(23, 24) {
                    it.execSQL("ALTER TABLE manga RENAME TO tmp_manga")
                    it.execSQL("CREATE TABLE `manga` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `unic` TEXT NOT NULL, `host` TEXT NOT NULL, `name` TEXT NOT NULL, `authors` TEXT NOT NULL, `logo` TEXT NOT NULL, `about` TEXT NOT NULL, `categories` TEXT NOT NULL, `genres` TEXT NOT NULL, `path` TEXT NOT NULL, `status` TEXT NOT NULL, `site` TEXT NOT NULL, `color` INTEGER NOT NULL, `populate` INTEGER NOT NULL DEFAULT 0, `order` INTEGER NOT NULL DEFAULT 0)")
                    it.execSQL("INSERT INTO manga(id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color) SELECT id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color FROM tmp_manga")
                    it.execSQL("DROP TABLE tmp_manga")
                }
        )
    }
}
