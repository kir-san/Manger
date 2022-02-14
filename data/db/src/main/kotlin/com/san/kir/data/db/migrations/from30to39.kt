package com.san.kir.data.db.migrations

import com.san.kir.core.support.ChapterFilter
import com.san.kir.core.support.DownloadState
import com.san.kir.data.models.base.Category
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.ShikiManga
import com.san.kir.data.models.columns.DownloadColumn
import com.san.kir.data.models.columns.MangaStatisticColumn
import com.san.kir.data.models.extend.SimplifiedManga
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts

/*
Таблица chapters
Добавление поля pages
Храние списка страниц
*/
internal val from30to31 = migrate {
    from = 30
    to = 31

    query("ALTER TABLE chapters RENAME TO tmp_chapters")
    query(
        "CREATE TABLE `chapters` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`manga` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`date` TEXT NOT NULL, " +
                "`path` TEXT NOT NULL, " +
                "`isRead` INTEGER NOT NULL, " +
                "`site` TEXT NOT NULL, " +
                "`progress` INTEGER NOT NULL, " +
                "`pages` TEXT NOT NULL DEFAULT ``)"
    )
    query(
        "INSERT INTO chapters(" +
                "id, manga, name, date, path, isRead, site, progress) " +
                "SELECT " +
                "id, manga, name, date, path, isRead, site, progress " +
                "FROM tmp_chapters"
    )
    query("DROP TABLE tmp_chapters")
}

/*
Таблица manga
Добавление поля chapterFilter
Индивидуальная сортировка и фильтрация списка глав
*/
internal val from31to32 = migrate {
    from = 31
    to = 32

    query("ALTER TABLE manga RENAME TO tmp_manga")
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
                "isUpdate INTEGER NOT NULL DEFAULT 1," +
                "chapterFilter TEXT NOT NULL DEFAULT ${ChapterFilter.ALL_READ_ASC.name})"
    )
    query(
        "INSERT INTO manga(" +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color, populate, `order`, isAlternativeSort, isUpdate) " +
                "SELECT " +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color, populate, `order`, isAlternativeSort, isUpdate " +
                "FROM tmp_manga"
    )
    query("DROP TABLE tmp_manga")
}

/*
Таблица manga
Добавление поля isAlternativeSite
*/
internal val from32to33 = migrate {
    from = 32
    to = 33

    query("ALTER TABLE manga RENAME TO tmp_manga")

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
                "isUpdate INTEGER NOT NULL DEFAULT 1," +
                "chapterFilter TEXT NOT NULL DEFAULT ${ChapterFilter.ALL_READ_ASC.name}," +
                "isAlternativeSite INTEGER NOT NULL DEFAULT 0)"
    )
    query(
        "INSERT INTO manga(" +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, " +
                "site, color, populate, `order`, isAlternativeSort, isUpdate, chapterFilter) " +
                "SELECT " +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, " +
                "site, color, populate, `order`, isAlternativeSort, isUpdate, chapterFilter " +
                "FROM tmp_manga"
    )
    query("DROP TABLE tmp_manga")
}

/*
Таблица downloads
Добавление поля error
Индикация ошибки
*/
internal val from33to34 = migrate {
    from = 33
    to = 34

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
                    "`$order` INTEGER NOT NULL, " +
                    "$error INTEGER NOT NULL DEFAULT 0)",
        )

        query(
            "INSERT INTO $tableName(" +
                    "$id, $name, $link, $path, $totalPages, $downloadPages, $totalSize, " +
                    "$downloadSize, $totalTime, $status, `$order`) " +
                    "SELECT " +
                    "$id, $name, $link, $path, $totalPages, $downloadPages, $totalSize, " +
                    "$downloadSize, $totalTime, $status, `$order` " +
                    "FROM $tmpTable",
        )

        removeTmpTable()
    }
}

/*
Таблица manga
Добавление поля link
Ссылка на страницу в интернете
*/
internal val from34to35 = migrate {
    from = 34
    to = 35

    with(Manga.Col) {
        renameTableToTmp(Manga.tableName)

        query(
            "CREATE TABLE ${Manga.tableName} (" +
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
                    "$link TEXT NOT NULL DEFAULT ``)"
        )

        query(
            "INSERT INTO ${Manga.tableName} (" +
                    "$id, unic, $host, $name, $authors, $logo, $about, $category, $genres, $path, " +
                    "$status, site, $color, $populate, `order`, $alternativeSort, $update, " +
                    "$filter, $alternativeSite) " +
                    "SELECT " +
                    "$id, unic, $host, $name, $authors, $logo, $about, $category, $genres, $path, " +
                    "$status, site, $color, $populate, `order`, $alternativeSort, $update, " +
                    "$filter, $alternativeSite " +
                    "FROM $tmpTable"
        )

        removeTmpTable()
    }
}

/*
Таблица chapters
Добавление поля isInUpdate
Отображение в обновлениях, на замену отдельной таблице LatestChapterss
*/
internal val from35to36 = migrate {
    from = 35
    to = 36

    with(Chapter.Col) {
        renameTableToTmp(Chapter.tableName)

        query(
            "CREATE TABLE `${Chapter.tableName}` (" +
                    "$id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "$manga TEXT NOT NULL, " +
                    "$name TEXT NOT NULL, " +
                    "$date TEXT NOT NULL, " +
                    "$path TEXT NOT NULL, " +
                    "$isRead INTEGER NOT NULL, " +
                    "site TEXT NOT NULL, " +
                    "$progress INTEGER NOT NULL, " +
                    "$pages TEXT NOT NULL DEFAULT ``, " +
                    "$isInUpdate INTEGER NOT NULL DEFAULT 0)"
        )

        query(
            "INSERT INTO ${Chapter.tableName} (" +
                    "$id, $manga, $name, $date, $path, $isRead, site, $progress, $pages, $isInUpdate) " +
                    "SELECT " +
                    "$id, $manga, $name, $date, $path, $isRead, site, $progress, $pages, $isInUpdate " +
                    "FROM $tmpTable"
        )

        removeTmpTable()
    }
}

/*
Таблица chapters
Добавление полей totalPages, downloadPages, totalSize, downloadSize, totalTime, status
Объединение с таблицей downloads
*/
internal val from36to37 = migrate {
    from = 36
    to = 37
    with(Chapter.Col) {
        renameTableToTmp(Chapter.tableName)

        query(
            "CREATE TABLE `${Chapter.tableName}` (" +
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
                    "$error INTEGER NOT NULL DEFAULT 0)"
        )

        query(
            "INSERT INTO ${Chapter.tableName} (" +
                    "$id, $manga, $name, $date, $path, $isRead, site, $progress, $pages, $isInUpdate) " +
                    "SELECT " +
                    "$id, $manga, $name, $date, $path, $isRead, site, $progress, $pages, $isInUpdate " +
                    "FROM $tmpTable"
        )

        removeTmpTable()
    }
}

/*
Таблица statistic
Добавление полей lastDownloadSize, lastDownloadTime
Расширение статистики
*/
internal val from37to38 = migrate {
    from = 37
    to = 38

    with(MangaStatisticColumn) {
        renameTableToTmp(tableName)

        query(
            "CREATE TABLE `$tableName` (" +
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
                    "$lastDownloadTime INTEGER NOT NULL DEFAULT 0)"
        )

        query(
            "INSERT INTO `$tableName`(" +
                    "$id, $manga, $allChapters, $lastChapters, $allPages, $lastPages, $allTime, " +
                    "$lastTime, $maxSpeed, $downloadSize, $downloadTime, $openedTimes) " +
                    "SELECT " +
                    "$id, $manga, $allChapters, $lastChapters, $allPages, $lastPages, $allTime, " +
                    "$lastTime, $maxSpeed, $downloadSize, $downloadTime, $openedTimes " +
                    "FROM $tmpTable"
        )

        removeTmpTable()
    }
}

/*
Удаление таблицы downloads

Таблица manga
Добавление поля categoryId (для более удобной работы с категориями)
Переименовывание поля categories в category, order в ordering

Таблица chapters
Добавление поля mangaId (для более удобной работы с мангой)
Переименовывание поля order в ordering
*/
internal val from38to39 = migrate {
    from = 38
    to = 39

    query("DROP TABLE `downloads`")

    with(Manga.Col) {
        renameTableToTmp(Manga.tableName)

        query(
            "CREATE TABLE ${Manga.tableName} (" +
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
                    "$link TEXT NOT NULL)"
        )

        query(
            "INSERT INTO ${Manga.tableName} (" +
                    "$id, $host, $name, $authors, $logo, $about, $category, $genres, $path, $status, " +
                    "$color, $populate, $order, $alternativeSort, $update, $filter, $alternativeSite, $link) " +
                    "SELECT " +
                    "$id, $host, $name, $authors, $logo, $about, categories, $genres, $path, $status, " +
                    "$color, $populate, `order`, $alternativeSort, $update, $filter, $alternativeSite, " +
                    "shortLink " +
                    "FROM $tmpTable"
        )

        removeTmpTable()
    }

    with(Chapter.Col) {
        renameTableToTmp(Chapter.tableName)

        query(
            "CREATE TABLE `${Chapter.tableName}` (" +
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
                    "$error INTEGER NOT NULL)"
        )

        query(
            "INSERT INTO ${Chapter.tableName} (" +
                    "$id, $manga, $name, $date, $path, $isRead, $link, $progress, $pages, $isInUpdate, " +
                    "$totalTime, $error, $totalSize, $downloadSize, $downloadPages, $totalPages, " +
                    "$status, $order) " +
                    "SELECT " +
                    "$id, $manga, $name, $date, $path, $isRead, $link, $progress, $pages, $isInUpdate, " +
                    "$totalTime, $error, $totalSize, $downloadSize, $downloadPages, $totalPages, " +
                    "$status, `order` " +
                    "FROM $tmpTable"
        )

        removeTmpTable()
    }

    with(ShikiManga.Col) {
        query(
            "CREATE TABLE IF NOT EXISTS `${ShikiManga.tableName}` (" +
                    "$id INTEGER PRIMARY KEY NOT NULL, " +
                    "$libMangaId INTEGER NOT NULL, " +
                    "$rate TEXT NOT NULL, " +
                    "$data TEXT NOT NULL)"
        )
    }

    query(
        "CREATE VIEW `${SimplifiedManga.viewName}` " +
                "AS SELECT " +
                "${Manga.Col.id}, " +
                "${Manga.Col.name}, " +
                "${Manga.Col.logo}, " +
                "${Manga.Col.color}, " +
                "${Manga.Col.populate}, " +
                "${Manga.Col.category} " +
                "FROM `${Manga.tableName}`"
    )

    query(
        "CREATE VIEW `${SimplifiedMangaWithChapterCounts.viewName}` " +
                "AS SELECT " +
                "${Manga.tableName}.${Manga.Col.id}, " +
                "${Manga.tableName}.${Manga.Col.name}, " +
                "${Manga.tableName}.${Manga.Col.logo}, " +
                "${Manga.tableName}.${Manga.Col.about}, " +
                "${Manga.tableName}.${Manga.Col.alternativeSort}, " +

                "(SELECT COUNT(*) FROM ${Chapter.tableName} " +
                "WHERE ${Chapter.tableName}.${Chapter.Col.manga} IS " +
                "${Manga.tableName}.${Manga.Col.name} " +
                "AND ${Chapter.tableName}.${Chapter.Col.isRead} IS 1) AS ${SimplifiedMangaWithChapterCounts.Col.readChapters}, " +

                "(SELECT COUNT(*) FROM ${Chapter.tableName} " +
                "WHERE ${Chapter.tableName}.${Chapter.Col.manga} IS " +
                "${Manga.tableName}.${Manga.Col.name}) AS ${SimplifiedMangaWithChapterCounts.Col.allChapters} " +

                "FROM ${Manga.tableName}"
    )
}

/*
Таблица Category
Переименовывание столбца "order" в "ordering"
AutoMigration с этим справится не может
*/
internal val from39to40 = migrate {
    from = 39
    to = 40

    with(Category.Col) {
        renameTableToTmp(Category.tableName)

        query(
            "CREATE TABLE ${Category.tableName} (" +
                    "$id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "$name TEXT NOT NULL, " +
                    "$order INTEGER NOT NULL, " +
                    "$isVisible INTEGER NOT NULL, " +
                    "$typeSort TEXT NOT NULL, " +
                    "$isReverseSort INTEGER NOT NULL, " +
                    "$spanPortrait INTEGER NOT NULL DEFAULT 2, " +
                    "$spanLandscape INTEGER NOT NULL DEFAULT 3, " +
                    "$isLargePortrait INTEGER NOT NULL DEFAULT 1, " +
                    "$isLargeLandscape INTEGER NOT NULL DEFAULT 1)"
        )
        query(
            "INSERT INTO ${Category.tableName}(" +
                    "$id, $name, $order, $isVisible, $typeSort, $isReverseSort, $spanPortrait, " +
                    "$spanLandscape, $isLargePortrait, $isLargeLandscape) " +
                    "SELECT " +
                    "$id, $name, `order`, $isVisible, $typeSort, $isReverseSort, $spanPortrait, " +
                    "$spanLandscape, $isLargePortrait, $isLargeLandscape " +
                    "FROM $tmpTable"
        )
        removeTmpTable()
    }
}
