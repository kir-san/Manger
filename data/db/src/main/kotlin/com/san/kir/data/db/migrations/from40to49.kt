package com.san.kir.data.db.migrations

import com.san.kir.data.models.base.Category
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.PlannedTask

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
                    "(SELECT ${Category.Col.id} FROM ${Category.tableName} " +
                    "WHERE ${Manga.tableName}.$category = ${Category.Col.name})"
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
                    "(SELECT ${Category.Col.id} FROM ${Category.tableName} " +
                    "WHERE ${Manga.tableName}.$category = ${Category.Col.name})"
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
                    "(SELECT ${Category.Col.id} FROM ${Category.tableName} " +
                    "WHERE ${PlannedTask.tableName}.${category} = ${Category.Col.name}) " +

                    "WHERE EXISTS " +
                    "(SELECT ${Category.Col.id} FROM ${Category.tableName} " +
                    "WHERE ${PlannedTask.tableName}.${category} = ${Category.Col.name})"
        )
    }
}
