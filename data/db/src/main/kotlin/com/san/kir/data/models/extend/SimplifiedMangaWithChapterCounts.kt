package com.san.kir.data.models.extend

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import com.san.kir.data.models.base.ShikimoriMangaItem

@DatabaseView(
    viewName = "libarary_manga",
    value = "SELECT " +
            "manga.id, " +
            "manga.name, " +
            "manga.logo, " +
            "manga.about, " +
            "manga.isAlternativeSort, " +
            "(SELECT COUNT(*) FROM chapters WHERE chapters.manga_id IS manga.id AND chapters.isRead IS 1) AS read_chapters, " +
            "(SELECT COUNT(*) FROM chapters WHERE chapters.manga_id IS manga.id) AS all_chapters " +
            "FROM manga"
)
data class SimplifiedMangaWithChapterCounts(
    @ColumnInfo(name = "id") override val id: Long = 0,
    @ColumnInfo(name = "name") override val name: String = "",
    @ColumnInfo(name = "logo") override val logo: String = "",
    @ColumnInfo(name = "about") override val description: String = "",
    @ColumnInfo(name = "isAlternativeSort") val sort: Boolean = false,
    @ColumnInfo(name = "read_chapters") override val read: Long = 0,
    @ColumnInfo(name = "all_chapters") override val all: Long = 0,
) : ShikimoriMangaItem
