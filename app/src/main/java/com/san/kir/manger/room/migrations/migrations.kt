package com.san.kir.manger.room.migrations

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration
import com.san.kir.manger.room.models.DownloadColumn
import com.san.kir.manger.room.models.PlannedTaskColumn


private fun migrate(from: Int, to: Int, vararg sql: String) =
    object : Migration(from, to) {
        override fun migrate(database: SupportSQLiteDatabase) {
            sql.forEach { database.execSQL(it) }
        }
    }


val migrations: Array<Migration> = arrayOf(
    migrate(
        9, 10,
        "ALTER TABLE manga RENAME TO tmp_manga",
        "CREATE TABLE `manga` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`unic` TEXT NOT NULL, " +
                "`host` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`authors` TEXT NOT NULL, " +
                "`logo` TEXT NOT NULL, " +
                "`about` TEXT NOT NULL, " +
                "`categories` TEXT NOT NULL, " +
                "`genres` TEXT NOT NULL, " +
                "`path` TEXT NOT NULL, " +
                "`status` TEXT NOT NULL, " +
                "`site` TEXT NOT NULL, " +
                "`color` INTEGER NOT NULL)",
        "INSERT INTO manga(" +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color) " +
                "SELECT " +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color " +
                "FROM tmp_manga",
        "DROP TABLE tmp_manga"
    ),
    migrate(
        10, 11,
        "ALTER TABLE chapters RENAME TO tmp_chapters",
        "CREATE TABLE `chapters` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`manga` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`date` TEXT NOT NULL, " +
                "`path` TEXT NOT NULL, " +
                "`isRead` INTEGER NOT NULL, " +
                "`site` TEXT NOT NULL, " +
                "`progress` INTEGER NOT NULL)",
        "INSERT INTO chapters(" +
                "id, manga, name, date, path, isRead, site, progress) " +
                "SELECT " +
                "id, manga, name, date, path, isRead, site, progress " +
                "FROM tmp_chapters",
        "DROP TABLE tmp_chapters"
    ),
    migrate(
        11, 12,
        "ALTER TABLE categories RENAME TO tmp_categories",
        "CREATE TABLE `categories` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`order` INTEGER NOT NULL, " +
                "`isVisible` INTEGER NOT NULL, " +
                "`typeSort` TEXT NOT NULL, " +
                "`isReverseSort` INTEGER NOT NULL)",
        "INSERT INTO `categories`(" +
                "`id`,`name`,`order`,`isVisible`,`typeSort`,`isReverseSort`) " +
                "SELECT " +
                "`id`,`name`,`order`,`isVisible`,`typeSort`,`isReverseSort` " +
                "FROM tmp_categories",
        "DROP TABLE tmp_categories"
    ),
    migrate(
        12, 13,
        "ALTER TABLE latestChapters RENAME TO tmp_latestChapters",
        "CREATE TABLE `latestChapters` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`manga` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`date` TEXT NOT NULL, " +
                "`path` TEXT NOT NULL, " +
                "`site` TEXT NOT NULL)",
        "INSERT INTO `latestChapters` (" +
                "`id`, `manga`, `name`, `date`, `path`, `site`) " +
                "SELECT " +
                "`id`, `manga`, `name`, `date`, `path`, `site` " +
                "FROM tmp_latestChapters",
        "DROP TABLE tmp_latestChapters"
    ),
    migrate(
        13, 14,
        "DROP TABLE IF EXISTS `mainmenuitems`",
        "CREATE TABLE IF NOT EXISTS `mainmenuitems` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` INTEGER NOT NULL, " +
                "`isVisible` INTEGER NOT NULL, " +
                "`order` INTEGER NOT NULL)"
    ),
    migrate(
        14, 15,
        "ALTER TABLE sites RENAME TO tmp_sites",
        "CREATE TABLE `sites` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`count` INTEGER NOT NULL)",
        "INSERT INTO `sites` (" +
                "`id` , `name`, `count`) " +
                "SELECT " +
                "`id` , `name`, `count` " +
                "FROM tmp_sites",
        "DROP TABLE tmp_sites"
    ),
    migrate(
        15, 16,
        "CREATE TABLE `StorageDir` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`file` TEXT NOT NULL, " +
                "`size` INTEGER NOT NULL, " +
                "`countDir` INTEGER NOT NULL)",
        "CREATE TABLE `StorageItem` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`path` TEXT NOT NULL, " +
                "`sizeFull` INTEGER NOT NULL, " +
                "`sizeRead` INTEGER NOT NULL, " +
                "`isNew` INTEGER NOT NULL, " +
                "`catalogName` TEXT NOT NULL)"
    ),
    migrate(16, 17),
    migrate(
        17, 18,
        "DROP TABLE IF EXISTS `mainmenuitems`",
        "CREATE TABLE IF NOT EXISTS `mainmenuitems` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`isVisible` INTEGER NOT NULL, " +
                "`order` INTEGER NOT NULL, " +
                "`type` TEXT NOT NULL)"
    ),
    migrate(
        18, 19,
        "DROP TABLE IF EXISTS `sites`",
        "CREATE TABLE `sites` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`host` TEXT NOT NULL, " +
                "`catalogName` TEXT NOT NULL, " +
                "`volume` INTEGER NOT NULL, " +
                "`oldVolume` INTEGER NOT NULL, " +
                "`siteID` INTEGER NOT NULL)"
    ),
    migrate(
        19, 20,
        "DROP TABLE IF EXISTS `StorageDir`",
        "DROP TABLE IF EXISTS `StorageItem`",
        "CREATE TABLE `StorageItem` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`path` TEXT NOT NULL, " +
                "`sizeFull` REAL NOT NULL, " +
                "`sizeRead` REAL NOT NULL, " +
                "`isNew` INTEGER NOT NULL, " +
                "`catalogName` TEXT NOT NULL)"
    ),
    migrate(
        20, 21,
        "CREATE TABLE IF NOT EXISTS ${DownloadColumn.tableName} (" +
                "${DownloadColumn.id} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "${DownloadColumn.name} TEXT NOT NULL, " +
                "${DownloadColumn.link} TEXT NOT NULL, " +
                "${DownloadColumn.path} TEXT NOT NULL, " +
                "${DownloadColumn.totalPages} INTEGER NOT NULL, " +
                "${DownloadColumn.downloadPages} INTEGER NOT NULL, " +
                "${DownloadColumn.totalSize} INTEGER NOT NULL, " +
                "${DownloadColumn.downloadSize} INTEGER NOT NULL, " +
                "${DownloadColumn.totalTime} INTEGER NOT NULL, " +
                "${DownloadColumn.status} INTEGER NOT NULL, " +
                "${DownloadColumn.order} INTEGER NOT NULL)"
    ),
    migrate(
        21, 22,
        "DROP TABLE IF EXISTS ${DownloadColumn.tableName}",
        "CREATE TABLE IF NOT EXISTS ${DownloadColumn.tableName} (" +
                "${DownloadColumn.id} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "${DownloadColumn.name} TEXT NOT NULL, " +
                "${DownloadColumn.link} TEXT NOT NULL, " +
                "${DownloadColumn.path} TEXT NOT NULL, " +
                "${DownloadColumn.totalPages} INTEGER NOT NULL, " +
                "${DownloadColumn.downloadPages} INTEGER NOT NULL, " +
                "${DownloadColumn.totalSize} INTEGER NOT NULL, " +
                "${DownloadColumn.downloadSize} INTEGER NOT NULL, " +
                "${DownloadColumn.totalTime} INTEGER NOT NULL, " +
                "${DownloadColumn.status} INTEGER NOT NULL, " +
                "${DownloadColumn.order} INTEGER NOT NULL)"
    ),
    migrate(
        22, 23,
        "ALTER TABLE categories RENAME TO tmp_categories",
        "CREATE TABLE `categories` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`order` INTEGER NOT NULL, " +
                "`isVisible` INTEGER NOT NULL, " +
                "`typeSort` TEXT NOT NULL, " +
                "`isReverseSort` INTEGER NOT NULL, " +
                "`spanPortrait` INTEGER NOT NULL DEFAULT 2, " +
                "'spanLandscape' INTEGER NOT NULL DEFAULT 3, " +
                "`isListPortrait` INTEGER NOT NULL DEFAULT 1, " +
                "`isListLandscape` INTEGER NOT NULL DEFAULT 1)",
        "INSERT INTO `categories`(" +
                "`id`,`name`,`order`,`isVisible`,`typeSort`,`isReverseSort`) " +
                "SELECT " +
                "`id`,`name`,`order`,`isVisible`,`typeSort`,`isReverseSort` " +
                "FROM tmp_categories",
        "DROP TABLE tmp_categories"
    ),
    migrate(
        23, 24,
        "ALTER TABLE manga RENAME TO tmp_manga",
        "CREATE TABLE `manga` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`unic` TEXT NOT NULL, " +
                "`host` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`authors` TEXT NOT NULL, " +
                "`logo` TEXT NOT NULL, " +
                "`about` TEXT NOT NULL, " +
                "`categories` TEXT NOT NULL, " +
                "`genres` TEXT NOT NULL, " +
                "`path` TEXT NOT NULL, " +
                "`status` TEXT NOT NULL, " +
                "`site` TEXT NOT NULL, " +
                "`color` INTEGER NOT NULL, " +
                "`populate` INTEGER NOT NULL DEFAULT 0, " +
                "`order` INTEGER NOT NULL DEFAULT 0)",
        "INSERT INTO manga(" +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color) " +
                "SELECT " +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color " +
                "FROM tmp_manga",
        "DROP TABLE tmp_manga"
    ),
    migrate(
        24, 25,
        "ALTER TABLE manga RENAME TO tmp_manga",
        "CREATE TABLE `manga` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`unic` TEXT NOT NULL, " +
                "`host` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`authors` TEXT NOT NULL, " +
                "`logo` TEXT NOT NULL, " +
                "`about` TEXT NOT NULL, " +
                "`categories` TEXT NOT NULL, " +
                "`genres` TEXT NOT NULL, " +
                "`path` TEXT NOT NULL, " +
                "`status` TEXT NOT NULL, " +
                "`site` TEXT NOT NULL, " +
                "`color` INTEGER NOT NULL, " +
                "`populate` INTEGER NOT NULL DEFAULT 0, " +
                "`order` INTEGER NOT NULL DEFAULT 0, " +
                "`isAlternativeSort` INTEGER NOT NULL DEFAULT 1)",
        "INSERT INTO manga(" +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color, populate, `order`) " +
                "SELECT " +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color, populate, `order` " +
                "FROM tmp_manga",
        "DROP TABLE tmp_manga"
    ),
    migrate(
        25, 26,
        "ALTER TABLE ${DownloadColumn.tableName} RENAME TO tmp_${DownloadColumn.tableName}",
        "CREATE TABLE ${DownloadColumn.tableName} (" +
                "${DownloadColumn.id} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "${DownloadColumn.manga} TEXT NOT NULL DEFAULT ``, " +
                "${DownloadColumn.name} TEXT NOT NULL, " +
                "${DownloadColumn.link} TEXT NOT NULL, " +
                "${DownloadColumn.path} TEXT NOT NULL, " +
                "${DownloadColumn.totalPages} INTEGER NOT NULL, " +
                "${DownloadColumn.downloadPages} INTEGER NOT NULL, " +
                "${DownloadColumn.totalSize} INTEGER NOT NULL, " +
                "${DownloadColumn.downloadSize} INTEGER NOT NULL, " +
                "${DownloadColumn.totalTime} INTEGER NOT NULL, " +
                "${DownloadColumn.status} INTEGER NOT NULL, " +
                "`${DownloadColumn.order}` INTEGER NOT NULL)",
        "INSERT INTO ${DownloadColumn.tableName}(" +
                "${DownloadColumn.id}, ${DownloadColumn.name}, ${DownloadColumn.link}," +
                "${DownloadColumn.path}, ${DownloadColumn.totalPages}, ${DownloadColumn.downloadPages}," +
                "${DownloadColumn.totalSize}, ${DownloadColumn.downloadSize}, ${DownloadColumn.totalTime}," +
                "${DownloadColumn.status}, `${DownloadColumn.order}`) " +
                "SELECT " +
                "${DownloadColumn.id}, ${DownloadColumn.name}, ${DownloadColumn.link}," +
                "${DownloadColumn.path}, ${DownloadColumn.totalPages}, ${DownloadColumn.downloadPages}," +
                "${DownloadColumn.totalSize}, ${DownloadColumn.downloadSize}, ${DownloadColumn.totalTime}," +
                "${DownloadColumn.status}, `${DownloadColumn.order}` " +
                "FROM tmp_${DownloadColumn.tableName}",
        "DROP TABLE tmp_${DownloadColumn.tableName}"
    ),
    migrate(26, 27,
            "ALTER TABLE manga RENAME TO tmp_manga",
            "CREATE TABLE `manga` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`unic` TEXT NOT NULL, " +
                    "`host` TEXT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`authors` TEXT NOT NULL, " +
                    "`logo` TEXT NOT NULL, " +
                    "`about` TEXT NOT NULL, " +
                    "`categories` TEXT NOT NULL, " +
                    "`genres` TEXT NOT NULL, " +
                    "`path` TEXT NOT NULL, " +
                    "`status` TEXT NOT NULL, " +
                    "`site` TEXT NOT NULL, " +
                    "`color` INTEGER NOT NULL, " +
                    "`populate` INTEGER NOT NULL DEFAULT 0, " +
                    "`order` INTEGER NOT NULL DEFAULT 0, " +
                    "`isAlternativeSort` INTEGER NOT NULL DEFAULT 1, " +
                    "isUpdate INTEGER NOT NULL DEFAULT 1)",
            "INSERT INTO manga(" +
                    "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color, populate, `order`, isAlternativeSort) " +
                    "SELECT " +
                    "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color, populate, `order`, isAlternativeSort " +
                    "FROM tmp_manga",
            "DROP TABLE tmp_manga"
    ),
    migrate(27, 28,
            "CREATE TABLE IF NOT EXISTS `${PlannedTaskColumn.tableName}` (" +
                    "`${PlannedTaskColumn.id}` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`${PlannedTaskColumn.manga}` TEXT NOT NULL, " +
                    "`${PlannedTaskColumn.groupName}` TEXT NOT NULL, " +
                    "`${PlannedTaskColumn.groupContent}` TEXT NOT NULL, " +
                    "`${PlannedTaskColumn.category}` TEXT NOT NULL, " +
                    "`${PlannedTaskColumn.type}` INTEGER NOT NULL, " +
                    "`${PlannedTaskColumn.isEnabled}` INTEGER NOT NULL, " +
                    "`${PlannedTaskColumn.period}` INTEGER NOT NULL, " +
                    "`${PlannedTaskColumn.dayOfWeek}` INTEGER NOT NULL, " +
                    "`${PlannedTaskColumn.hour}` INTEGER NOT NULL, " +
                    "`${PlannedTaskColumn.minute}` INTEGER NOT NULL, " +
                    "`${PlannedTaskColumn.addedTime}` INTEGER NOT NULL, " +
                    "`${PlannedTaskColumn.errorMessage}` TEXT NOT NULL)"
    )
)
