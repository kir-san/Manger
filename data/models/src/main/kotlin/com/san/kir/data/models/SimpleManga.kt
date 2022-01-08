package com.san.kir.data.models

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView(
    viewName = SimpleManga.viewName,
    value = "SELECT " +
            "${Manga.Col.id}, " +
            "${Manga.Col.name}, " +
            "${Manga.Col.logo}, " +
            "${Manga.Col.color}, " +
            "${Manga.Col.populate}, " +
            "${Manga.Col.category} " +
            "FROM `${Manga.tableName}`")
data class SimpleManga(
    @ColumnInfo(name = Manga.Col.id) var id: Long = 0,
    @ColumnInfo(name = Manga.Col.name) var name: String = "",
    @ColumnInfo(name = Manga.Col.logo) var logo: String = "",
    @ColumnInfo(name = Manga.Col.color) var color: Int = 0,
    @ColumnInfo(name = Manga.Col.populate) var populate: Int = 0,
    @ColumnInfo(name = Manga.Col.category) var categories: String = "",
) {
    companion object {
        const val viewName = "simple_manga"
    }
}
