package com.san.kir.data.db.migrations

import com.san.kir.data.models.Chapter
import com.san.kir.data.models.Manga

internal val from40to41 = migrate {
    from = 40
    to = 41

    query("DROP TABLE `downloads`")

    with(Manga.Col) {
        renameTableToTmp(Manga.tableName)

        query("CREATE TABLE ${Manga.tableName} (" +
                "$id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "$host TEXT NOT NULL, " +
                "$name TEXT NOT NULL, " +
                "$logo TEXT NOT NULL, " +
                "$about TEXT NOT NULL, " +
                "$category TEXT NOT NULL, " +
                "$categoryId INTEGER NOT NULL DEFAULT 0, " +
                "$path TEXT NOT NULL, " +
                "$status TEXT NOT NULL, " +
                "$color INTEGER NOT NULL, " +
                "$populate INTEGER NOT NULL, " +
                "$order INTEGER NOT NULL, " +
                "$alternativeSort INTEGER NOT NULL, " +
                "$update INTEGER NOT NULL," +
                "$filter TEXT NOT NULL, " +
                "$alternativeSite INTEGER NOT NULL, " +
                "$authors TEXT NOT NULL, " +
                "$genres TEXT NOT NULL, " +
                "$link TEXT NOT NULL)")

        query("INSERT INTO ${Manga.tableName} (" +
                "$id, $host, $name, $authors, $logo, $about, $category, $genres, $path, $status, " +
                "$color, $populate, $order, $alternativeSort, $update, $filter, $alternativeSite, $link) " +
                "SELECT " +
                "$id, $host, $name, $authors, $logo, $about, categories, $genres, $path, $status, " +
                "$color, $populate, `order`, $alternativeSort, $update, $filter, $alternativeSite, " +
                "shortLink " +
                "FROM ${Manga.tableName}_tmp")

        removeTmpTable(Manga.tableName)
    }

    with(Chapter.Col) {
        renameTableToTmp(Chapter.tableName)

        query("CREATE TABLE `${Chapter.tableName}` (" +
                "$id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "$manga TEXT NOT NULL, " +
                "$mangaId INTEGER NOT NULL DEFAULT 0, " +
                "$name TEXT NOT NULL, " +
                "$date TEXT NOT NULL, " +
                "$path TEXT NOT NULL, " +
                "$isRead INTEGER NOT NULL, " +
                "$link TEXT NOT NULL, " +
                "$progress INTEGER NOT NULL, " +
                "$pages TEXT NOT NULL DEFAULT ``, " +
                "$isInUpdate INTEGER NOT NULL, " +
                "$totalPages INTEGER NOT NULL, " +
                "$downloadPages INTEGER NOT NULL, " +
                "$totalSize INTEGER NOT NULL, " +
                "$downloadSize INTEGER NOT NULL, " +
                "$totalTime INTEGER NOT NULL, " +
                "$status TEXT NOT NULL, " +
                "$order INTEGER NOT NULL, " +
                "$error INTEGER NOT NULL)")

        query("INSERT INTO ${Chapter.tableName} (" +
                "$id, $manga, $name, $date, $path, $isRead, $link, $progress, $pages, $isInUpdate, " +
                "$totalTime, $error, $totalSize, $downloadSize, $downloadPages, $totalPages, " +
                "$status, $order) " +
                "SELECT " +
                "$id, $manga, $name, $date, $path, $isRead, $link, $progress, $pages, $isInUpdate " +
                "$totalTime, $error, $totalSize, $downloadSize, $downloadPages, $totalPages, " +
                "$status, `order` " +
                "FROM ${Chapter.tableName}_tmp")

        removeTmpTable(Chapter.tableName)
    }
}


internal val from42to43 = migrate {
    from = 42
    to = 43

    query("DROP TABLE `downloads`")

    with(Manga.Col) {
        renameTableToTmp(Manga.tableName)

        query("CREATE TABLE ${Manga.tableName} (" +
                "$id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "$host TEXT NOT NULL, " +
                "$name TEXT NOT NULL, " +
                "$logo TEXT NOT NULL, " +
                "$about TEXT NOT NULL, " +
                "$category TEXT NOT NULL, " +
                "$categoryId INTEGER NOT NULL DEFAULT 0, " +
                "$path TEXT NOT NULL, " +
                "$status TEXT NOT NULL, " +
                "$color INTEGER NOT NULL, " +
                "$populate INTEGER NOT NULL, " +
                "$order INTEGER NOT NULL, " +
                "$alternativeSort INTEGER NOT NULL, " +
                "$update INTEGER NOT NULL," +
                "$filter TEXT NOT NULL, " +
                "$alternativeSite INTEGER NOT NULL, " +
                "$authors TEXT NOT NULL, " +
                "$genres TEXT NOT NULL, " +
                "$link TEXT NOT NULL)")

        query("INSERT INTO ${Manga.tableName} (" +
                "$id, $host, $name, $authors, $logo, $about, $category, $genres, $path, $status, " +
                "$color, $populate, $order, $alternativeSort, $update, $filter, $alternativeSite, $link) " +
                "SELECT " +
                "$id, $host, $name, $authors, $logo, $about, categories, $genres, $path, $status, " +
                "$color, $populate, `order`, $alternativeSort, $update, $filter, $alternativeSite, " +
                "shortLink " +
                "FROM ${Manga.tableName}_tmp")

        removeTmpTable(Manga.tableName)
    }

    with(Chapter.Col) {
        renameTableToTmp(Chapter.tableName)

        query("CREATE TABLE `${Chapter.tableName}` (" +
                "$id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "$manga TEXT NOT NULL, " +
                "$mangaId INTEGER NOT NULL DEFAULT 0, " +
                "$name TEXT NOT NULL, " +
                "$date TEXT NOT NULL, " +
                "$path TEXT NOT NULL, " +
                "$isRead INTEGER NOT NULL, " +
                "$link TEXT NOT NULL, " +
                "$progress INTEGER NOT NULL, " +
                "$pages TEXT NOT NULL DEFAULT ``, " +
                "$isInUpdate INTEGER NOT NULL, " +
                "$totalPages INTEGER NOT NULL, " +
                "$downloadPages INTEGER NOT NULL, " +
                "$totalSize INTEGER NOT NULL, " +
                "$downloadSize INTEGER NOT NULL, " +
                "$totalTime INTEGER NOT NULL, " +
                "$status TEXT NOT NULL, " +
                "$order INTEGER NOT NULL, " +
                "$error INTEGER NOT NULL)")

        query("INSERT INTO ${Chapter.tableName} (" +
                "$id, $manga, $name, $date, $path, $isRead, $link, $progress, $pages, $isInUpdate, " +
                "$totalTime, $error, $totalSize, $downloadSize, $downloadPages, $totalPages, " +
                "$status, $order) " +
                "SELECT " +
                "$id, $manga, $name, $date, $path, $isRead, $link, $progress, $pages, $isInUpdate, " +
                "$totalTime, $error, $totalSize, $downloadSize, $downloadPages, $totalPages, " +
                "$status, `order` " +
                "FROM ${Chapter.tableName}_tmp")

        removeTmpTable(Chapter.tableName)
    }
}
