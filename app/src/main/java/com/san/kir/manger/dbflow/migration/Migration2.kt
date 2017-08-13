package com.san.kir.manger.dbflow.migration

import com.raizlabs.android.dbflow.annotation.Migration
import com.raizlabs.android.dbflow.sql.SQLiteType
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration
import com.san.kir.manger.dbflow.AppDatabase
import com.san.kir.manger.dbflow.models.Category

@Migration(version = 8, database = AppDatabase::class)
class Migration2(table: Class<Category>?) : AlterTableMigration<Category>(table) {
    override fun onPreMigrate() {
        super.onPreMigrate()
        addColumn(SQLiteType.TEXT, "typeSort")
        addColumn(SQLiteType.INTEGER, "isReverseSort")
    }
}
