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

internal val from50to51 = migrate {
    from = 50
    to = 51

    query("UPDATE chapters SET manga_id=(SELECT id FROM manga WHERE name=chapters.manga)")
}

/*
Таблица PlannedTask
Переименование таблицы во временную и созданние новой таблицы (было разнесено из-за проблем с миграцией)
*/
internal val from54to55 = migrate {
    from = 54
    to = 55

    renameTableToTmp("planned_task")
    query(
        "CREATE TABLE IF NOT EXISTS `planned_task` (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "manga_id INTEGER NOT NULL DEFAULT 0, " +
                "group_name TEXT NOT NULL, " +
                "group_content TEXT NOT NULL, " +
                "category_id INTEGER NOT NULL DEFAULT 0 , " +
                "catalog TEXT NOT NULL DEFAULT '', " +
                "type INTEGER NOT NULL, " +
                "is_enabled INTEGER NOT NULL, " +
                "period INTEGER NOT NULL, " +
                "day_of_week INTEGER NOT NULL, " +
                "hour INTEGER NOT NULL, " +
                "minute INTEGER NOT NULL, " +
                "added_time INTEGER NOT NULL, " +
                "error_message TEXT NOT NULL)"
    )
}

/*
Таблица PlannedTask
Заполение таблицы данными из временной, созданной в предыдущую миграцию
Удалению view planned_task_ext за ненадобностью
*/
internal val from55to56 = migrate {
    from = 55
    to = 56

    query(
        "INSERT INTO planned_task(" +
                "id, group_name, group_content, category_id, catalog, type, " +
                "is_enabled, period, day_of_week, hour, minute, added_time, error_message, manga_id) " +
                "SELECT " +
                "id, group_name, group_content, category_id, catalog, type, " +
                "is_enabled, period, day_of_week, hour, minute, added_time, error_message,  " +
                "IFNULL((SELECT id FROM manga WHERE planned_task_tmp.manga=manga.name), -1)" +
                "FROM planned_task_tmp"
    )

    query("DROP TABLE planned_task_tmp")

    query("DROP VIEW planned_task_ext")
}
