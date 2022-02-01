package com.san.kir.data.models.extend

import androidx.room.ColumnInfo
import androidx.room.Relation
import com.san.kir.core.support.SortLibraryUtil
import com.san.kir.data.models.base.Category
import com.san.kir.data.models.base.Manga

data class CategoryWithMangas(
    @ColumnInfo(name = Category.Col.name)
    var name: String = "",

    @ColumnInfo(name = Category.Col.typeSort)
    var typeSort: String = SortLibraryUtil.abc,

    @ColumnInfo(name = Category.Col.isReverseSort)
    var isReverseSort: Boolean = false,

    @ColumnInfo(name = Category.Col.spanPortrait)
    var spanPortrait: Int = 2,

    @ColumnInfo(name = Category.Col.spanLandscape)
    var spanLandscape: Int = 3,

    @ColumnInfo(name = Category.Col.isLargePortrait)
    var isLargePortrait: Boolean = true,

    @ColumnInfo(name = Category.Col.isLargeLandscape)
    var isLargeLandscape: Boolean = true,

    @Relation(
        parentColumn = Category.Col.name,
        entityColumn = Manga.Col.category,
        entity = Manga::class,
        projection = [
            Manga.Col.id,
            Manga.Col.name,
            Manga.Col.logo,
            Manga.Col.color,
            Manga.Col.populate,
            Manga.Col.categoryId,
            Manga.Col.category,
        ]
    )
    var mangas: List<SimplifiedManga> = emptyList(),
)
