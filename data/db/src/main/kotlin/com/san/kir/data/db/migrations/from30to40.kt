package com.san.kir.data.db.migrations

import com.san.kir.core.support.ChapterFilter
import com.san.kir.core.support.DownloadState
import com.san.kir.data.models.Chapter
import com.san.kir.data.models.Manga
import com.san.kir.data.models.columns.MangaStatisticColumn

internal val from34to35 = migrate {
    from = 34
    to = 35

    with(Manga.Col) {
        renameTableToTmp(Manga.tableName)

        query("CREATE TABLE ${Manga.tableName} (" +
                "$id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "unic TEXT NOT NULL, " +
                "$host TEXT NOT NULL, " +
                "$name TEXT NOT NULL, " +
                "$authors TEXT NOT NULL, " +
                "$logo TEXT NOT NULL, " +
                "$about TEXT NOT NULL, " +
                "$category TEXT NOT NULL, " +
                "$genres TEXT NOT NULL, " +
                "$path TEXT NOT NULL, " +
                "$status TEXT NOT NULL, " +
                "site TEXT NOT NULL, " +
                "$color INTEGER NOT NULL, " +
                "$populate INTEGER NOT NULL DEFAULT 0, " +
                "`order` INTEGER NOT NULL DEFAULT 0, " +
                "$alternativeSort INTEGER NOT NULL DEFAULT 1, " +
                "$update INTEGER NOT NULL DEFAULT 1," +
                "$filter TEXT NOT NULL DEFAULT ${ChapterFilter.ALL_READ_ASC.name}, " +
                "$alternativeSite INTEGER NOT NULL DEFAULT 0, " +
                "$link TEXT NOT NULL DEFAULT ``)")

        query("INSERT INTO ${Manga.tableName} (" +
                "$id, unic, $host, $name, $authors, $logo, $about, $category, $genres, $path, " +
                "$status, site, $color, $populate, `order`, $alternativeSort, $update, " +
                "$filter, $alternativeSite) " +
                "SELECT " +
                "$id, unic, $host, $name, $authors, $logo, $about, $category, $genres, $path, " +
                "$status, site, $color, $populate, `order`, $alternativeSort, $update, " +
                "$filter, $alternativeSite " +
                "FROM ${Manga.tableName}_tmp")

        removeTmpTable(Manga.tableName)
    }
}

internal val from35to36 = migrate {
    from = 35
    to = 36

    with(Chapter.Col) {
        renameTableToTmp(Chapter.tableName)

        query("CREATE TABLE `${Chapter.tableName}` (" +
                "$id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "$manga TEXT NOT NULL, " +
                "$name TEXT NOT NULL, " +
                "$date TEXT NOT NULL, " +
                "$path TEXT NOT NULL, " +
                "$isRead INTEGER NOT NULL, " +
                "site TEXT NOT NULL, " +
                "$progress INTEGER NOT NULL, " +
                "$pages TEXT NOT NULL DEFAULT ``, " +
                "$isInUpdate INTEGER NOT NULL DEFAULT 0)")

        query("INSERT INTO ${Chapter.tableName} (" +
                "$id, $manga, $name, $date, $path, $isRead, site, $progress, $pages, $isInUpdate) " +
                "SELECT " +
                "$id, $manga, $name, $date, $path, $isRead, site, $progress, $pages, $isInUpdate " +
                "FROM ${Chapter.tableName}_tmp")

        removeTmpTable(Chapter.tableName)
    }
}

internal val from36to37 = migrate {
    from = 36
    to = 37
    with(Chapter.Col) {
        renameTableToTmp(Chapter.tableName)

        query("CREATE TABLE `${Chapter.tableName}` (" +
                "$id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "$manga TEXT NOT NULL, " +
                "$name TEXT NOT NULL, " +
                "$date TEXT NOT NULL, " +
                "$path TEXT NOT NULL, " +
                "$isRead INTEGER NOT NULL, " +
                "site TEXT NOT NULL, " +
                "$progress INTEGER NOT NULL, " +
                "$pages TEXT NOT NULL DEFAULT ``, " +
                "$isInUpdate INTEGER NOT NULL DEFAULT 0, " +
                "$totalPages INTEGER NOT NULL DEFAULT 0, " +
                "$downloadPages INTEGER NOT NULL DEFAULT 0, " +
                "$totalSize INTEGER NOT NULL DEFAULT 0, " +
                "$downloadSize INTEGER NOT NULL DEFAULT 0, " +
                "$totalTime INTEGER NOT NULL DEFAULT 0, " +
                "$status TEXT NOT NULL DEFAULT ${DownloadState.UNKNOWN.name}, " +
                "`order` INTEGER NOT NULL DEFAULT 0, " +
                "$error INTEGER NOT NULL DEFAULT 0)")

        query("INSERT INTO ${Chapter.tableName} (" +
                "$id, $manga, $name, $date, $path, $isRead, site, $progress, $pages, $isInUpdate) " +
                "SELECT " +
                "$id, $manga, $name, $date, $path, $isRead, site, $progress, $pages, $isInUpdate " +
                "FROM ${Chapter.tableName}_tmp")

        removeTmpTable(Chapter.tableName)
    }
}

internal val from37to38 = migrate {
    from = 37
    to = 38

    with(MangaStatisticColumn) {
        renameTableToTmp(tableName)

        query("CREATE TABLE `$tableName` (" +
                "$id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "$manga TEXT NOT NULL, " +
                "$allChapters INTEGER NOT NULL, " +
                "$lastChapters INTEGER NOT NULL, " +
                "$allPages INTEGER NOT NULL, " +
                "$lastPages INTEGER NOT NULL, " +
                "$allTime INTEGER NOT NULL, " +
                "$lastTime INTEGER NOT NULL, " +
                "$maxSpeed INTEGER NOT NULL, " +
                "$downloadSize INTEGER NOT NULL, " +
                "$downloadTime INTEGER NOT NULL, " +
                "$openedTimes INTEGER NOT NULL, " +
                "$lastDownloadSize INTEGER NOT NULL DEFAULT 0, " +
                "$lastDownloadTime INTEGER NOT NULL DEFAULT 0)")

        query("INSERT INTO `$tableName`(" +
                "$id, $manga, $allChapters, $lastChapters, $allPages, $lastPages, $allTime, " +
                "$lastTime, $maxSpeed, $downloadSize, $downloadTime, $openedTimes) " +
                "SELECT " +
                "$id, $manga, $allChapters, $lastChapters, $allPages, $lastPages, $allTime, " +
                "$lastTime, $maxSpeed, $downloadSize, $downloadTime, $openedTimes " +
                "FROM ${tableName}_tmp")

        removeTmpTable(tableName)
    }
}
