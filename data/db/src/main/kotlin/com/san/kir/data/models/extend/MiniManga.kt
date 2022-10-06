package com.san.kir.data.models.extend

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView(
    viewName = "mini_manga",
    value = "SELECT " +
            "manga.id, " +
            "manga.name, " +

            "(SELECT name FROM categories " +
            "WHERE manga.category_id = categories.id) " +
            "AS category, " +

            "manga.isUpdate " +

            "FROM manga"
)
data class MiniManga(
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "category") val category: String = "",
    @ColumnInfo(name = "isUpdate") val update: Boolean = false,
) {
}
