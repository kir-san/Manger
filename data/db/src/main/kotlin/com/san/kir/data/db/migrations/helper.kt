package com.san.kir.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.intellij.lang.annotations.Language

internal fun migrate(action: MigrateForm.() -> Unit): Migration {
    val form = MigrateForm()
    form.action()

    return object : Migration(form.from, form.to) {
        override fun migrate(database: SupportSQLiteDatabase) {
            form.queries.forEach { database.execSQL(it) }
        }
    }
}

internal class MigrateForm {
    var queries: List<String> = emptyList()
        private set

    var from: Int = -1
    var to: Int = -1

    var tmpTable: String = ""
        private set


    fun query(@Language("RoomSql") sql: String) {
        queries = queries.plus(sql)
    }

    fun renameTableToTmp(tableName: String) {
        tmpTable = "${tableName}_tmp"
        query("ALTER TABLE $tableName RENAME TO $tmpTable")
    }

    fun removeTmpTable() {
        query("DROP TABLE $tmpTable")
        tmpTable = ""
    }
}
