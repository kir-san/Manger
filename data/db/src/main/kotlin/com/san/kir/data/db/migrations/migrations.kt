package com.san.kir.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.san.kir.core.support.ChapterFilter
import com.san.kir.data.models.base.Category
import com.san.kir.data.models.base.PlannedTask
import com.san.kir.data.models.columns.DownloadColumn
import com.san.kir.data.models.columns.MangaStatisticColumn


internal fun migrate(from: Int, to: Int, vararg sql: String) =
    object : Migration(from, to) {
        override fun migrate(database: SupportSQLiteDatabase) {
            sql.forEach { database.execSQL(it) }
        }
    }

internal val migrations: Array<Migration> = arrayOf(
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
        "CREATE TABLE ${Category.tableName} (" +
                "${Category.Col.id} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "${Category.Col.name} TEXT NOT NULL, " +
                "`order` INTEGER NOT NULL, " +
                "${Category.Col.isVisible} INTEGER NOT NULL, " +
                "${Category.Col.typeSort} TEXT NOT NULL, " +
                "${Category.Col.isReverseSort} INTEGER NOT NULL, " +
                "${Category.Col.spanPortrait} INTEGER NOT NULL DEFAULT 2, " +
                "${Category.Col.spanLandscape} INTEGER NOT NULL DEFAULT 3, " +
                "${Category.Col.isLargePortrait} INTEGER NOT NULL DEFAULT 1, " +
                "${Category.Col.isLargeLandscape} INTEGER NOT NULL DEFAULT 1)",
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
    migrate(
        26, 27,
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
    migrate(
        27, 28,
        "CREATE TABLE IF NOT EXISTS `${PlannedTask.tableName}` (" +
                "`${PlannedTask.Col.id}` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`${PlannedTask.Col.manga}` TEXT NOT NULL, " +
                "`${PlannedTask.Col.groupName}` TEXT NOT NULL, " +
                "`${PlannedTask.Col.groupContent}` TEXT NOT NULL, " +
                "`${PlannedTask.Col.category}` TEXT NOT NULL, " +
                "`${PlannedTask.Col.type}` INTEGER NOT NULL, " +
                "`${PlannedTask.Col.isEnabled}` INTEGER NOT NULL, " +
                "`${PlannedTask.Col.period}` INTEGER NOT NULL, " +
                "`${PlannedTask.Col.dayOfWeek}` INTEGER NOT NULL, " +
                "`${PlannedTask.Col.hour}` INTEGER NOT NULL, " +
                "`${PlannedTask.Col.minute}` INTEGER NOT NULL, " +
                "`${PlannedTask.Col.addedTime}` INTEGER NOT NULL, " +
                "`${PlannedTask.Col.errorMessage}` TEXT NOT NULL)"
    ),
    migrate(
        28, 29,
        "ALTER TABLE ${PlannedTask.tableName} RENAME TO tmp_${PlannedTask.tableName}",
        "CREATE TABLE IF NOT EXISTS `${PlannedTask.tableName}` (" +
                "`${PlannedTask.Col.id}` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`${PlannedTask.Col.manga}` TEXT NOT NULL, " +
                "`${PlannedTask.Col.groupName}` TEXT NOT NULL, " +
                "`${PlannedTask.Col.groupContent}` TEXT NOT NULL, " +
                "`${PlannedTask.Col.category}` TEXT NOT NULL, " +
                "`${PlannedTask.Col.catalog}` TEXT NOT NULL DEFAULT ``, " +
                "`${PlannedTask.Col.type}` INTEGER NOT NULL, " +
                "`${PlannedTask.Col.isEnabled}` INTEGER NOT NULL, " +
                "`${PlannedTask.Col.period}` INTEGER NOT NULL, " +
                "`${PlannedTask.Col.dayOfWeek}` INTEGER NOT NULL, " +
                "`${PlannedTask.Col.hour}` INTEGER NOT NULL, " +
                "`${PlannedTask.Col.minute}` INTEGER NOT NULL, " +
                "`${PlannedTask.Col.addedTime}` INTEGER NOT NULL, " +
                "`${PlannedTask.Col.errorMessage}` TEXT NOT NULL)",
        "INSERT INTO ${PlannedTask.tableName}(" +
                "`${PlannedTask.Col.id}`, " +
                "`${PlannedTask.Col.manga}`, " +
                "`${PlannedTask.Col.groupName}`, " +
                "`${PlannedTask.Col.groupContent}`, " +
                "`${PlannedTask.Col.category}`, " +
                "`${PlannedTask.Col.type}`, " +
                "`${PlannedTask.Col.isEnabled}`, " +
                "`${PlannedTask.Col.period}`, " +
                "`${PlannedTask.Col.dayOfWeek}`, " +
                "`${PlannedTask.Col.hour}`, " +
                "`${PlannedTask.Col.minute}`, " +
                "`${PlannedTask.Col.addedTime}`, " +
                "`${PlannedTask.Col.errorMessage}`) " +
                "SELECT " +
                "`${PlannedTask.Col.id}`, " +
                "`${PlannedTask.Col.manga}`, " +
                "`${PlannedTask.Col.groupName}`, " +
                "`${PlannedTask.Col.groupContent}`, " +
                "`${PlannedTask.Col.category}`, " +
                "`${PlannedTask.Col.type}`, " +
                "`${PlannedTask.Col.isEnabled}`, " +
                "`${PlannedTask.Col.period}`, " +
                "`${PlannedTask.Col.dayOfWeek}`, " +
                "`${PlannedTask.Col.hour}`, " +
                "`${PlannedTask.Col.minute}`, " +
                "`${PlannedTask.Col.addedTime}`, " +
                "`${PlannedTask.Col.errorMessage}` " +
                "FROM tmp_${PlannedTask.tableName}",
        "DROP TABLE tmp_${PlannedTask.tableName}"
    ),
    migrate(
        29, 30,
        "CREATE TABLE IF NOT EXISTS `${MangaStatisticColumn.tableName}` (" +
                "`${MangaStatisticColumn.id}` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`${MangaStatisticColumn.manga}` TEXT NOT NULL, " +
                "`${MangaStatisticColumn.allChapters}` INTEGER NOT NULL, " +
                "`${MangaStatisticColumn.lastChapters}` INTEGER NOT NULL, " +
                "`${MangaStatisticColumn.allPages}` INTEGER NOT NULL, " +
                "`${MangaStatisticColumn.lastPages}` INTEGER NOT NULL, " +
                "`${MangaStatisticColumn.allTime}` INTEGER NOT NULL, " +
                "`${MangaStatisticColumn.lastTime}` INTEGER NOT NULL, " +
                "`${MangaStatisticColumn.maxSpeed}` INTEGER NOT NULL, " +
                "`${MangaStatisticColumn.downloadSize}` INTEGER NOT NULL, " +
                "`${MangaStatisticColumn.downloadTime}` INTEGER NOT NULL, " +
                "`${MangaStatisticColumn.openedTimes}` INTEGER NOT NULL)"
    ),
    migrate(
        30, 31,
        "ALTER TABLE chapters RENAME TO tmp_chapters",
        "CREATE TABLE `chapters` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`manga` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`date` TEXT NOT NULL, " +
                "`path` TEXT NOT NULL, " +
                "`isRead` INTEGER NOT NULL, " +
                "`site` TEXT NOT NULL, " +
                "`progress` INTEGER NOT NULL, " +
                "`pages` TEXT NOT NULL DEFAULT ``)",
        "INSERT INTO chapters(" +
                "id, manga, name, date, path, isRead, site, progress) " +
                "SELECT " +
                "id, manga, name, date, path, isRead, site, progress " +
                "FROM tmp_chapters",
        "DROP TABLE tmp_chapters"
    ),
    migrate(
        31, 32,
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
                "isUpdate INTEGER NOT NULL DEFAULT 1," +
                "chapterFilter TEXT NOT NULL DEFAULT ${ChapterFilter.ALL_READ_ASC.name})",
        "INSERT INTO manga(" +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color, populate, `order`, isAlternativeSort, isUpdate) " +
                "SELECT " +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color, populate, `order`, isAlternativeSort, isUpdate " +
                "FROM tmp_manga",
        "DROP TABLE tmp_manga"
    ),
    migrate(
        32, 33,
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
                "isUpdate INTEGER NOT NULL DEFAULT 1," +
                "chapterFilter TEXT NOT NULL DEFAULT ${ChapterFilter.ALL_READ_ASC.name}," +
                "isAlternativeSite INTEGER NOT NULL DEFAULT 0)",
        "INSERT INTO manga(" +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color, populate, `order`, isAlternativeSort, isUpdate, chapterFilter) " +
                "SELECT " +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color, populate, `order`, isAlternativeSort, isUpdate, chapterFilter " +
                "FROM tmp_manga",
        "DROP TABLE tmp_manga"
    ),
    migrate(
        33, 34,
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
                "`${DownloadColumn.order}` INTEGER NOT NULL, " +
                "${DownloadColumn.error} INTEGER NOT NULL DEFAULT 0)",
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
    from34to35,
    from35to36,
    from36to37,
    from37to38,
    from38to39,
    from39to40, // For Category table (order -> ordering)
    from40to41, // For Manga table (fill categoryId)
    from42to43, // For PlannedTask table (add & fill categoryId)
)

internal fun migrate(action: MigrateForm.() -> Unit): Migration {
    val form = MigrateForm()
    form.action()

    return object : Migration(form.from, form.to) {
        override fun migrate(database: SupportSQLiteDatabase) {
            form.queries.forEach { database.execSQL(it) }
        }
    }
}

internal class MigrateForm {
    var queries: List<String> = emptyList()
        private set

    var from: Int = -1
    var to: Int = -1

    var tmpTable: String = ""
        private set

    fun query(sql: String) {
        queries = queries.plus(sql)
    }

    fun renameTableToTmp(tableName: String) {
        tmpTable = "${tableName}_tmp"
        query("ALTER TABLE $tableName RENAME TO $tmpTable")
    }

    fun removeTmpTable() {
        query("DROP TABLE $tmpTable")
    }
}
