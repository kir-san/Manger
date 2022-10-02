package com.san.kir.data.models.extend

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import com.san.kir.data.models.base.Manga

@DatabaseView(
    viewName = MiniManga.viewName,
    value = "SELECT " +
            "${Manga.tableName}.${Manga.Col.id}, " +
            "${Manga.tableName}.${Manga.Col.name}, " +

            "(SELECT name FROM categories " +
            "WHERE ${Manga.tableName}.${Manga.Col.categoryId} = categories.id) " +
            "AS ${Manga.Col.category}, " +

            "${Manga.tableName}.${Manga.Col.update} " +

            "FROM ${Manga.tableName}"
)
data class MiniManga(
    @ColumnInfo(name = Manga.Col.id) val id: Long = 0,
    @ColumnInfo(name = Manga.Col.name) val name: String = "",
    @ColumnInfo(name = Manga.Col.category) val category: String = "",
    @ColumnInfo(name = Manga.Col.update) val update: Boolean = false,
) {
    companion object {
        const val viewName = "mini_manga"
    }
}
