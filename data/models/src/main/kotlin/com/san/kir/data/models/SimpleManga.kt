package com.san.kir.data.models

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import com.san.kir.data.models.columns.MangaColumn

@DatabaseView(
    viewName = SimpleManga.viewName,
    value = "SELECT " +
            "${MangaColumn.id}, " +
            "${MangaColumn.name}, " +
            "${MangaColumn.logo}, " +
            "${MangaColumn.color}, " +
            "${MangaColumn.populate}, " +
            "${MangaColumn.categories} " +
            "FROM `${MangaColumn.tableName}`")
data class SimpleManga(
    @ColumnInfo(name = MangaColumn.id) var id: Long = 0,
    @ColumnInfo(name = MangaColumn.name) var name: String = "",
    @ColumnInfo(name = MangaColumn.logo) var logo: String = "",
    @ColumnInfo(name = MangaColumn.color) var color: Int = 0,
    @ColumnInfo(name = MangaColumn.populate) var populate: Int = 0,
    @ColumnInfo(name = MangaColumn.categories) var categories: String = "",
) {
    companion object {
        const val viewName = "simple_manga"
    }
}
