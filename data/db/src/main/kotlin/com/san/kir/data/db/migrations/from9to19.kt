package com.san.kir.data.db.migrations

/*
Таблица manga
Пересоздание
*/
internal val from9to10 = migrate {
    from = 9
    to = 10

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
                "`color` INTEGER NOT NULL)"
    )
    query(
        "INSERT INTO manga(" +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color) " +
                "SELECT " +
                "id, unic, host, name, authors, logo, about, categories, genres, path, status, site, color " +
                "FROM tmp_manga"
    )
    query("DROP TABLE tmp_manga")
}

/*
Таблица chapters
Пересоздание
*/
internal val from10to11 = migrate {
    from = 10
    to = 11

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
                "`progress` INTEGER NOT NULL)"
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
Таблица categories
Пересоздание
*/
internal val from11to12 = migrate {
    from = 11
    to = 12

    query("ALTER TABLE categories RENAME TO tmp_categories")
    query(
        "CREATE TABLE `categories` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`order` INTEGER NOT NULL, " +
                "`isVisible` INTEGER NOT NULL, " +
                "`typeSort` TEXT NOT NULL, " +
                "`isReverseSort` INTEGER NOT NULL)"
    )
    query(
        "INSERT INTO `categories`(" +
                "`id`,`name`,`order`,`isVisible`,`typeSort`,`isReverseSort`) " +
                "SELECT " +
                "`id`,`name`,`order`,`isVisible`,`typeSort`,`isReverseSort` " +
                "FROM tmp_categories"
    )
    query("DROP TABLE tmp_categories")
}

/*
Таблица latestChapters
Пересоздание
*/
internal val from12to13 = migrate {
    from = 12
    to = 13

    query("ALTER TABLE latestChapters RENAME TO tmp_latestChapters")
    query(
        "CREATE TABLE `latestChapters` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`manga` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`date` TEXT NOT NULL, " +
                "`path` TEXT NOT NULL, " +
                "`site` TEXT NOT NULL)"
    )
    query(
        "INSERT INTO `latestChapters` (" +
                "`id`, `manga`, `name`, `date`, `path`, `site`) " +
                "SELECT " +
                "`id`, `manga`, `name`, `date`, `path`, `site` " +
                "FROM tmp_latestChapters"
    )
    query("DROP TABLE tmp_latestChapters")
}

/*
Таблица mainmenuitems
Пересоздание
*/
internal val from13to14 = migrate {
    from = 13
    to = 14

    query("DROP TABLE IF EXISTS `mainmenuitems`")
    query(
        "CREATE TABLE IF NOT EXISTS `mainmenuitems` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` INTEGER NOT NULL, " +
                "`isVisible` INTEGER NOT NULL, " +
                "`order` INTEGER NOT NULL)"
    )
}

/*
Таблица sites
Пересоздание
*/
internal val from14to15 = migrate {
    from = 14
    to = 15

    query("ALTER TABLE sites RENAME TO tmp_sites")
    query(
        "CREATE TABLE `sites` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`count` INTEGER NOT NULL)"
    )
    query(
        "INSERT INTO `sites` (`id` , `name`, `count`) " +
                "SELECT `id` , `name`, `count` " +
                "FROM tmp_sites"
    )
    query("DROP TABLE tmp_sites")
}

/*
Таблица StorageDir и StorageItem
Добавление в бд
*/
internal val from15to16 = migrate {
    from = 15
    to = 16

    query(
        "CREATE TABLE `StorageDir` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`file` TEXT NOT NULL, " +
                "`size` INTEGER NOT NULL, " +
                "`countDir` INTEGER NOT NULL)"
    )
    query(
        "CREATE TABLE `StorageItem` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`path` TEXT NOT NULL, " +
                "`sizeFull` INTEGER NOT NULL, " +
                "`sizeRead` INTEGER NOT NULL, " +
                "`isNew` INTEGER NOT NULL, " +
                "`catalogName` TEXT NOT NULL)"
    )
}

/*
Пустышка
*/
internal val from16to17 = migrate {
    from = 16
    to = 17
}

/*
Таблица mainmenuitems
Добавление поля type
*/
internal val from17to18 = migrate {
    from = 17
    to = 18

    query("DROP TABLE IF EXISTS `mainmenuitems`")
    query(
        "CREATE TABLE IF NOT EXISTS `mainmenuitems` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`isVisible` INTEGER NOT NULL, " +
                "`order` INTEGER NOT NULL, " +
                "`type` TEXT NOT NULL)"
    )
}

/*
Таблица sites
Добавление полей host, catalogName, volume, oldVolume, siteID
Удаление поля count
*/
internal val from18to19 = migrate {
    from = 18
    to = 19

    query("DROP TABLE IF EXISTS `sites`")
    query(
        "CREATE TABLE `sites` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`host` TEXT NOT NULL, " +
                "`catalogName` TEXT NOT NULL, " +
                "`volume` INTEGER NOT NULL, " +
                "`oldVolume` INTEGER NOT NULL, " +
                "`siteID` INTEGER NOT NULL)"
    )
}

/*
Таблица StorageItemб
Объединение таблиц StorageDir и StorageItem
*/
internal val from19to20 = migrate {
    from = 19
    to = 20

    query("DROP TABLE IF EXISTS `StorageDir`")
    query("DROP TABLE IF EXISTS `StorageItem`")
    query(
        "CREATE TABLE `StorageItem` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`path` TEXT NOT NULL, " +
                "`sizeFull` REAL NOT NULL, " +
                "`sizeRead` REAL NOT NULL, " +
                "`isNew` INTEGER NOT NULL, " +
                "`catalogName` TEXT NOT NULL)"
    )
}
