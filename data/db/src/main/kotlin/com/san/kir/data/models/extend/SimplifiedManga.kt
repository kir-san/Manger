package com.san.kir.data.models.extend

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import com.san.kir.data.models.base.Category
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga

@DatabaseView(
    viewName = SimplifiedManga.viewName,
    value = "SELECT " +
            "${Manga.tableName}.${Manga.Col.id}, " +
            "${Manga.tableName}.${Manga.Col.name} AS ${SimplifiedManga.Col.mangaName}, " +
            "${Manga.tableName}.${Manga.Col.logo}, " +
            "${Manga.tableName}.${Manga.Col.color}, " +
            "${Manga.tableName}.${Manga.Col.populate}, " +
            "${Manga.tableName}.${Manga.Col.categoryId}, " +

            "(SELECT ${Category.Col.name} FROM ${Category.tableName} " +
            "WHERE ${Manga.tableName}.${Manga.Col.categoryId} = ${Category.tableName}.${Category.Col.id}) " +
            "AS ${Manga.Col.category}, " +

            "(SELECT COUNT(*) FROM ${Chapter.tableName} " +
            "WHERE ${Chapter.tableName}.${Chapter.Col.manga} IS ${Manga.tableName}.${Manga.Col.name} " +
            "AND ${Chapter.tableName}.${Chapter.Col.isRead} IS 0) " +
            "AS ${SimplifiedManga.Col.noReadChapters} " +

            "FROM ${Manga.tableName}"
)
@Immutable
data class SimplifiedManga(
    @ColumnInfo(name = Manga.Col.id) val id: Long = 0,
    @ColumnInfo(name = Col.mangaName) val name: String = "",
    @ColumnInfo(name = Manga.Col.logo) val logo: String = "",
    @ColumnInfo(name = Manga.Col.color) val color: Int = 0,
    @ColumnInfo(name = Manga.Col.populate) val populate: Int = 0,
    @ColumnInfo(name = Manga.Col.categoryId) val categoryId: Long = 0,
    @ColumnInfo(name = Manga.Col.category) val category: String = "",
    @ColumnInfo(name = Col.noReadChapters) val noRead: Int = 0
) {
    companion object {
        const val viewName = "simple_manga"
    }

    object Col {
        const val mangaName = "manga_name"
        const val noReadChapters = "no_read_chapters"
    }
}
