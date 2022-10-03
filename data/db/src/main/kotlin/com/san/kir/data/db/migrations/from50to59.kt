package com.san.kir.data.db.migrations

/*
Таблица Statistic
Замена поля manga с названием манги на поле manga_id с id манги
*/
internal val from49to50 = migrate {
    from = 49
    to = 50

    renameTableToTmp("statistic")
    query(
        "CREATE TABLE `statistic` (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "manga_id INTEGER NOT NULL, " +
                "all_chapters INTEGER NOT NULL, " +
                "last_chapters INTEGER NOT NULL, " +
                "all_pages INTEGER NOT NULL, " +
                "last_pages INTEGER NOT NULL, " +
                "all_time INTEGER NOT NULL, " +
                "last_time INTEGER NOT NULL, " +
                "max_speed INTEGER NOT NULL, " +
                "download_size INTEGER NOT NULL, " +
                "download_time INTEGER NOT NULL, " +
                "opened_times INTEGER NOT NULL, " +
                "last_download_size INTEGER NOT NULL DEFAULT 0, " +
                "last_download_time INTEGER NOT NULL DEFAULT 0)"
    )
    query(
        "INSERT INTO `statistic`(" +
                "id, manga_id, all_chapters, last_chapters, all_pages, last_pages, all_time, " +
                "last_time, max_speed, download_size, download_time, opened_times) " +
                "SELECT " +
                "$tmpTable.id, manga.id, $tmpTable.all_chapters, $tmpTable.last_chapters, " +
                "$tmpTable.all_pages, $tmpTable.last_pages, $tmpTable.all_time, $tmpTable.last_time, " +
                "$tmpTable.max_speed, $tmpTable.download_size, $tmpTable.download_time, " +
                "$tmpTable.opened_times " +
                "FROM $tmpTable JOIN manga ON $tmpTable.manga=manga.name"
    )
    removeTmpTable()

    query(
        "CREATE VIEW `simple_statistic` AS SELECT " +
                "statistic.id AS id, manga.name AS manga_name, " +
                "manga.logo AS manga_logo, statistic.all_time AS all_time " +
                "FROM statistic JOIN manga ON statistic.manga_id=manga.id"
    )
}
