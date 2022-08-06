package com.san.kir.data.models.extend

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import com.san.kir.data.models.base.Category
import com.san.kir.data.models.base.Manga

@DatabaseView(
    viewName = SimplifiedManga.viewName,
    value = "SELECT " +
            "${Manga.tableName}.${Manga.Col.id}, " +
            "${Manga.tableName}.${Manga.Col.name}, " +
            "${Manga.tableName}.${Manga.Col.logo}, " +
            "${Manga.tableName}.${Manga.Col.color}, " +
            "${Manga.tableName}.${Manga.Col.populate}, " +
            "${Manga.tableName}.${Manga.Col.categoryId}, " +

            "(SELECT ${Category.Col.name} FROM ${Category.tableName} " +
            "WHERE ${Manga.tableName}.${Manga.Col.categoryId} = ${Category.tableName}.${Category.Col.id}) " +
            "AS ${Manga.Col.category} " +

            "FROM ${Manga.tableName}"
)
data class SimplifiedManga(
    @ColumnInfo(name = Manga.Col.id) var id: Long = 0,
    @ColumnInfo(name = Manga.Col.name) var name: String = "",
    @ColumnInfo(name = Manga.Col.logo) var logo: String = "",
    @ColumnInfo(name = Manga.Col.color) var color: Int = 0,
    @ColumnInfo(name = Manga.Col.populate) var populate: Int = 0,
    @ColumnInfo(name = Manga.Col.categoryId) var categoryId: Long = 0,
    @ColumnInfo(name = Manga.Col.category) var category: String = "",
) {
    companion object {
        const val viewName = "simple_manga"
    }
}
