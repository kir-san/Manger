package com.san.kir.data.db.migrations

import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.PlannedTask



/*
Таблица Manga
Заполнение Столбца categoryId
*/
internal val from40to41 = migrate {
    from = 40
    to = 41

    with(Manga.Col) {
        query(
            "UPDATE ${Manga.tableName} " +
                    "SET $categoryId = " +
                    "(SELECT id FROM categories " +
                    "WHERE ${Manga.tableName}.$category = name)"
        )
    }
}

/*
Таблица PlannedTask
Добавление столбца categoryId
Заполнение столбца categoryId
*/
internal val from42to43 = migrate {
    from = 42
    to = 43

    with(Manga.Col) {
        query(
            "UPDATE ${Manga.tableName} " +
                    "SET $categoryId = " +
                    "(SELECT id FROM categories " +
                    "WHERE ${Manga.tableName}.$category = name)"
        )
    }

    with(PlannedTask.Col) {
        renameTableToTmp(PlannedTask.tableName)
        query(
            "CREATE TABLE IF NOT EXISTS `${PlannedTask.tableName}` (" +
                    "$id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "$manga TEXT NOT NULL, " +
                    "$groupName TEXT NOT NULL, " +
                    "$groupContent TEXT NOT NULL, " +
                    "$category TEXT NOT NULL, " +
                    "$categoryId INTEGER NOT NULL DEFAULT 0 , " +
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
                    "$id, $manga, $groupName, $groupContent, $category, $catalog, $type, " +
                    "$isEnabled, $period, $dayOfWeek, $hour, $minute, $addedTime, $errorMessage) " +
                    "SELECT " +
                    "$id, $manga, $groupName, $groupContent, $category, $catalog, $type, " +
                    "$isEnabled, $period, $dayOfWeek, $hour, $minute, $addedTime, $errorMessage " +
                    "FROM $tmpTable"
        )

        removeTmpTable()

        query(
            "UPDATE ${PlannedTask.tableName} " +
                    "SET $categoryId = " +
                    "(SELECT id FROM categories " +
                    "WHERE ${PlannedTask.tableName}.${category} = name) " +

                    "WHERE EXISTS " +
                    "(SELECT id FROM categories " +
                    "WHERE ${PlannedTask.tableName}.${category} = name)"
        )
    }
}

/*
Удаление таблицы LatestChapters
Автоматическая миграция заменена на ручную, так как автматическая пыталась дважды удалить
дважды одну и туже view.
*/
internal val from45to46 = migrate {
    from = 45
    to = 46

    query("DROP VIEW simple_manga")
    query("DROP VIEW libarary_manga")
    query("DROP VIEW planned_task_ext")
    query("DROP VIEW mini_manga")
    query("DROP TABLE `latestChapters`")
    query(
        "CREATE VIEW `simple_manga` AS SELECT " +
                "manga.id, manga.name, manga.logo, manga.color, manga.populate, manga.category_id, " +
                "(SELECT name FROM categories WHERE manga.category_id = categories.id) AS category " +
                "FROM manga"
    )
    query(
        "CREATE VIEW `libarary_manga` AS SELECT " +
                "manga.id, manga.name, manga.logo, manga.about, manga.isAlternativeSort, " +
                "(SELECT COUNT(*) FROM chapters WHERE chapters.manga IS manga.name AND " +
                "chapters.isRead IS 1) AS read_chapters, " +
                "(SELECT COUNT(*) FROM chapters WHERE chapters.manga IS manga.name) AS all_chapters " +
                "FROM manga"
    )
    query(
        "CREATE VIEW `planned_task_ext` AS SELECT " +
                "planned_task.id, planned_task.manga, planned_task.group_name, planned_task.group_content, " +
                "(SELECT name FROM categories WHERE planned_task.category_id = categories.id) AS category, " +
                "planned_task.category_id, planned_task.catalog, planned_task.type, planned_task.is_enabled, " +
                "planned_task.period, planned_task.day_of_week, planned_task.hour, planned_task.minute, " +
                "planned_task.added_time " +
                "FROM planned_task"
    )
    query(
        "CREATE VIEW `mini_manga` AS SELECT " +
                "manga.id, manga.name, " +
                "(SELECT name FROM categories WHERE manga.category_id = categories.id) AS category, " +
                "manga.isUpdate " +
                "FROM manga"
    )
}
