package com.san.kir.data.models.extend

import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView(
    viewName = "simple_manga",
    value = "SELECT " +
            "manga.id, " +
            "manga.name AS ${SimplifiedManga.Col.mangaName}, " +
            "manga.logo, " +
            "manga.color, " +
            "manga.populate, " +
            "manga.category_id, " +

            "(SELECT name FROM categories " +
            "WHERE manga.category_id = categories.id) " +
            "AS category, " +

            "(SELECT COUNT(*) FROM chapters " +
            "WHERE chapters.manga IS manga.name " +
            "AND chapters.isRead IS 0) " +
            "AS ${SimplifiedManga.Col.noReadChapters} " +

            "FROM manga"
)
@Stable
data class SimplifiedManga(
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = Col.mangaName) val name: String = "",
    @ColumnInfo(name = "logo") val logo: String = "",
    @ColumnInfo(name = "color") val color: Int = 0,
    @ColumnInfo(name = "populate") val populate: Int = 0,
    @ColumnInfo(name = "category_id") val categoryId: Long = 0,
    @ColumnInfo(name = "category") val category: String = "",
    @ColumnInfo(name = Col.noReadChapters) val noRead: Int = 0
) {

    object Col {
        const val mangaName = "manga_name"
        const val noReadChapters = "no_read_chapters"
    }
}
