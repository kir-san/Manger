package com.san.kir.data.models.extend

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.ShikimoriMangaItem

@DatabaseView(
    viewName = SimplifiedMangaWithChapterCounts.viewName,
    value = "SELECT " +
            "manga.id, " +
            "manga.name, " +
            "manga.logo, " +
            "manga.about, " +
            "manga.isAlternativeSort, " +

            "(SELECT COUNT(*) FROM chapters " +
            "WHERE chapters.manga IS " +
            "manga.name " +
            "AND chapters.${Chapter.Col.isRead} IS 1) AS " +
            "${SimplifiedMangaWithChapterCounts.Col.readChapters}, " +

            "(SELECT COUNT(*) FROM chapters " +
            "WHERE chapters.manga IS " +
            "manga.name) AS ${SimplifiedMangaWithChapterCounts.Col.allChapters} " +

            "FROM manga"
)
data class SimplifiedMangaWithChapterCounts(
    @ColumnInfo(name = "id") override val id: Long = 0,
    @ColumnInfo(name = "name") override val name: String = "",
    @ColumnInfo(name = "logo") override val logo: String = "",
    @ColumnInfo(name = "about") override val description: String = "",
    @ColumnInfo(name = "isAlternativeSort") val sort: Boolean = false,
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
