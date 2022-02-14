package com.san.kir.data.db.migrations

import com.san.kir.data.models.base.Category
import com.san.kir.data.models.base.PlannedTask
import com.san.kir.data.models.columns.DownloadColumn
import com.san.kir.data.models.columns.MangaStatisticColumn

/*
Таблица downloads
создание
*/
internal val from20to21 = migrate {
    from = 20
    to = 21

    query(
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
                "`${DownloadColumn.order}` INTEGER NOT NULL)"
    )
}

/*
Таблица downloads
пересоздание
*/
internal val from21to22 = migrate {
    from = 21
    to = 22

    query("DROP TABLE IF EXISTS ${DownloadColumn.tableName}")
    query(
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
                "`${DownloadColumn.order}` INTEGER NOT NULL)"
    )
}

/*
Таблица categories
Добавление полей spanPortrait, spanLandscape, isLargePortrait, isLargeLandscape
Используются для индивидуальной настройки отображения манги в библиотеке
*/
internal val from22to23 = migrate {
    from = 22
    to = 23

    query("ALTER TABLE categories RENAME TO tmp_categories")
    query(
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
                "${Category.Col.isLargeLandscape} INTEGER NOT NULL DEFAULT 1)"
    )
    query(
        "INSERT INTO `categories`(" +
                "`id`,`name`,`order`,`isVisible`,`typeSort`,`isReverseSort`) " +
                "SELECT " +
                "`id`,`name`,`order`,`isVisible`,`typeSort`,`isReverseSort` " +
                "FROM tmp_categories"
    )
    query(
        "DROP TABLE tmp_categories"
    )
}

/*
Таблица manga
Добавление полей populate, order
Используются для сортирвки в библиотеке
*/
internal val from23to24 = migrate {
    from = 23
    to = 24

    renameTableToTmp("manga")

    query(
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
                "`order` INTEGER NOT NULL DEFAULT 0)"
    )

    query(
        "INSERT INTO manga(" +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color) " +
                "SELECT " +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color " +
                "FROM $tmpTable"
    )

    removeTmpTable()
}

/*
Таблица manga
Добавление поля isAlternativeSort
Используется для альтернативной сортировки глав
*/
internal val from24to25 = migrate {
    from = 24
    to = 25

    renameTableToTmp("manga")

    query(
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
                "`isAlternativeSort` INTEGER NOT NULL DEFAULT 1)"
    )

    query(
        "INSERT INTO manga(" +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color, populate, `order`) " +
                "SELECT " +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color, populate, `order` " +
                "FROM $tmpTable"
    )

    removeTmpTable()
}

/*
Таблица downloads
Добавление поля manga
*/
internal val from25to26 = migrate {
    from = 25
    to = 26

    with(DownloadColumn) {
        renameTableToTmp(tableName)

        query(
            "CREATE TABLE $tableName (" +
                    "$id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "$manga TEXT NOT NULL DEFAULT ``, " +
                    "$name TEXT NOT NULL, " +
                    "$link TEXT NOT NULL, " +
                    "$path TEXT NOT NULL, " +
                    "$totalPages INTEGER NOT NULL, " +
                    "$downloadPages INTEGER NOT NULL, " +
                    "$totalSize INTEGER NOT NULL, " +
                    "$downloadSize INTEGER NOT NULL, " +
                    "$totalTime INTEGER NOT NULL, " +
                    "$status INTEGER NOT NULL, " +
                    "`$order` INTEGER NOT NULL)"
        )

        query(
            "INSERT INTO $tableName(" +
                    "$id, $name, $link,$path, $totalPages, $downloadPages,$totalSize, " +
                    "$downloadSize, $totalTime,$status, `$order`) " +
                    "SELECT " +
                    "$id, $name, $link,$path, $totalPages, $downloadPages,$totalSize, " +
                    "$downloadSize, $totalTime,$status, `$order` " +
                    "FROM $tmpTable"
        )

        removeTmpTable()
    }
}

/*
Таблица manga
Добавление поля isUpdate
Используется для запрета обновления глав у манги
*/
internal val from26to27 = migrate {
    from = 26
    to = 27

    renameTableToTmp("manga")

    query(
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
                "isUpdate INTEGER NOT NULL DEFAULT 1)"
    )

    query(
        "INSERT INTO manga(" +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color, populate, `order`, isAlternativeSort) " +
                "SELECT " +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color, populate, `order`, isAlternativeSort " +
                "FROM $tmpTable"
    )

    removeTmpTable()
}

/*
Таблица planned_task
Создание
Используется для создания автоматических обновлений
*/
internal val from27to28 = migrate {
    from = 27
    to = 28

    with(PlannedTask.Col) {
        query(
            "CREATE TABLE IF NOT EXISTS `${PlannedTask.tableName}` (" +
                    "$id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "$manga TEXT NOT NULL, " +
                    "$groupName TEXT NOT NULL, " +
                    "$groupContent TEXT NOT NULL, " +
                    "$category TEXT NOT NULL, " +
                    "$type INTEGER NOT NULL, " +
                    "$isEnabled INTEGER NOT NULL, " +
                    "$period INTEGER NOT NULL, " +
                    "$dayOfWeek INTEGER NOT NULL, " +
                    "$hour INTEGER NOT NULL, " +
                    "$minute INTEGER NOT NULL, " +
                    "$addedTime INTEGER NOT NULL, " +
                    "$errorMessage TEXT NOT NULL)"
        )
    }
}

/*
Таблица planned_task
Добавление поля catalog
Для обновления каталогов манги
*/
internal val from28to29 = migrate {
    from = 28
    to = 29

    with(PlannedTask.Col) {
        renameTableToTmp(PlannedTask.tableName)

        query(
            "CREATE TABLE IF NOT EXISTS ${PlannedTask.tableName} (" +
                    "$id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "$manga TEXT NOT NULL, " +
                    "$groupName TEXT NOT NULL, " +
                    "$groupContent TEXT NOT NULL, " +
                    "$category TEXT NOT NULL, " +
                    "$catalog TEXT NOT NULL DEFAULT ``, " +
                    "$type INTEGER NOT NULL, " +
                    "$isEnabled INTEGER NOT NULL, " +
                    "$period INTEGER NOT NULL, " +
                    "$dayOfWeek INTEGER NOT NULL, " +
                    "$hour INTEGER NOT NULL, " +
                    "$minute INTEGER NOT NULL, " +
                    "$addedTime INTEGER NOT NULL, " +
                    "$errorMessage TEXT NOT NULL)"
        )

        query(
            "INSERT INTO ${PlannedTask.tableName}(" +
                    "$id, $manga, $groupName, $groupContent, $category, $type, $isEnabled, " +
                    "$period, $dayOfWeek, $hour, $minute, $addedTime, $errorMessage) " +
                    "SELECT " +
                    "$id, $manga, $groupName, $groupContent, $category, $type, $isEnabled, " +
                    "$period, $dayOfWeek, $hour, $minute, $addedTime, $errorMessage " +
                    "FROM $tmpTable"
        )

        removeTmpTable()
    }
}

/*
Таблица statistic
Создание
Сбор статистики чтения
*/
internal val from29to30 = migrate {
    from = 29
    to = 30

    query(
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
    )
}
