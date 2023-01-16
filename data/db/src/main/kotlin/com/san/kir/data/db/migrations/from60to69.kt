package com.san.kir.data.db.migrations

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

/*
Таблица Settings
Добавлено поле scrollbars, которое переключает видимость скроллов во Viewwer
*/
internal val from61to62 = migrate {
    from = 61
    to = 62

    renameTableToTmp("settings")
    query(
        "CREATE TABLE `settings` (" +
                "id INTEGER PRIMARY KEY NOT NULL, " +
                "isFirstLaunch INTEGER NOT NULL, " +
                "isIndividual INTEGER NOT NULL, " +
                "isTitle INTEGER NOT NULL, " +
                "filterStatus TEXT NOT NULL, " +
                "concurrent INTEGER NOT NULL, " +
                "retry INTEGER NOT NULL, " +
                "wifi INTEGER NOT NULL, " +
                "theme INTEGER NOT NULL, " +
                "isShowCategory INTEGER NOT NULL, " +
                "orientation TEXT NOT NULL, " +
                "cutOut INTEGER NOT NULL, " +
                "withoutSaveFiles INTEGER NOT NULL, " +
                "taps INTEGER NOT NULL, " +
                "swipes INTEGER NOT NULL, " +
                "keys INTEGER NOT NULL, " +
                "editMenu INTEGER NOT NULL DEFAULT 0, " +
                "isLogin INTEGER NOT NULL, " +
                "access_token TEXT NOT NULL, " +
                "token_type TEXT NOT NULL, " +
                "expires_in INTEGER NOT NULL, " +
                "refresh_token TEXT NOT NULL, " +
                "scope TEXT NOT NULL, " +
                "created_at INTEGER NOT NULL, " +
                "shikimori_whoami_id INTEGER NOT NULL, " +
                "nickname TEXT NOT NULL, " +
                "avatar TEXT NOT NULL, " +
                "scrollbars INTEGER NOT NULL DEFAULT 1)"
    )
    query(
        "INSERT INTO `settings`( " +
                "id, isFirstLaunch, isIndividual, isTitle, filterStatus, concurrent, retry, wifi, " +
                "theme, isShowCategory, orientation, cutOut, withoutSaveFiles, taps, swipes, " +
                "keys, isLogin, access_token, token_type, expires_in, refresh_token, scope, " +
                "created_at, shikimori_whoami_id, nickname, avatar) " +
                "SELECT " +
                "id, isFirstLaunch, isIndividual, isTitle, filterStatus, concurrent, retry, wifi, " +
                "theme, isShowCategory, orientation, cutOut, withoutSaveFiles, taps, swipes, " +
                "keys, isLogin, access_token, token_type, expires_in, refresh_token, scope, " +
                "created_at, shikimori_whoami_id, nickname, avatar " +
                "FROM $tmpTable"
    )
    removeTmpTable()
}

/*
Таблица Chpaters
Удаление полей error, totalSize, totalPages, manga
*/

@DeleteColumn.Entries(
    value = [
        DeleteColumn(tableName = "chapters", columnName = "error"),
        DeleteColumn(tableName = "chapters", columnName = "totalSize"),
        DeleteColumn(tableName = "chapters", columnName = "totalPages"),
        DeleteColumn(tableName = "chapters", columnName = "manga"),
    ]
)
internal class From62to63 : AutoMigrationSpec
