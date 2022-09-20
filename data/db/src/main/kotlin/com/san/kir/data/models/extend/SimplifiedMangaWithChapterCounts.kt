package com.san.kir.data.models.extend

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.ShikimoriMangaItem

@DatabaseView(
    viewName = SimplifiedMangaWithChapterCounts.viewName,
    value = "SELECT " +
            "${Manga.tableName}.${Manga.Col.id}, " +
            "${Manga.tableName}.${Manga.Col.name}, " +
            "${Manga.tableName}.${Manga.Col.logo}, " +
            "${Manga.tableName}.${Manga.Col.about}, " +
            "${Manga.tableName}.${Manga.Col.alternativeSort}, " +

            "(SELECT COUNT(*) FROM ${Chapter.tableName} " +
            "WHERE ${Chapter.tableName}.${Chapter.Col.manga} IS " +
            "${Manga.tableName}.${Manga.Col.name} " +
            "AND ${Chapter.tableName}.${Chapter.Col.isRead} IS 1) AS " +
            "${SimplifiedMangaWithChapterCounts.Col.readChapters}, " +

            "(SELECT COUNT(*) FROM ${Chapter.tableName} " +
            "WHERE ${Chapter.tableName}.${Chapter.Col.manga} IS " +
            "${Manga.tableName}.${Manga.Col.name}) AS ${SimplifiedMangaWithChapterCounts.Col.allChapters} " +

            "FROM ${Manga.tableName}"
)
data class SimplifiedMangaWithChapterCounts(
    @ColumnInfo(name = Manga.Col.id) override val id: Long = 0,
    @ColumnInfo(name = Manga.Col.name) override val name: String = "",
    @ColumnInfo(name = Manga.Col.logo) override val logo: String = "",
    @ColumnInfo(name = Manga.Col.about) override val description: String = "",
    @ColumnInfo(name = Manga.Col.alternativeSort) val sort: Boolean = false,
    @ColumnInfo(name = Col.readChapters) override val read: Long = 0,
    @ColumnInfo(name = Col.allChapters) override val all: Long = 0,
) : ShikimoriMangaItem {

    companion object {
        const val viewName = "libarary_manga"
    }

    object Col {
        const val readChapters = "read_chapters"
        const val allChapters = "all_chapters"
    }
}
