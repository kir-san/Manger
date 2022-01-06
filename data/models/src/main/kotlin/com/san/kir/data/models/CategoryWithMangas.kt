package com.san.kir.data.models

import androidx.room.Embedded
import androidx.room.Relation
import com.san.kir.data.models.columns.CategoryColumn
import com.san.kir.data.models.columns.MangaColumn

data class CategoryWithMangas(
    @Embedded val category: Category = Category(),

    @Relation(
        parentColumn = CategoryColumn.name,
        entityColumn = MangaColumn.categories,
        entity = Manga::class,
        projection = [
            MangaColumn.id,
            MangaColumn.name,
            MangaColumn.logo,
            MangaColumn.color,
            MangaColumn.populate,
            MangaColumn.categories
        ]
    )
    var mangas: List<SimpleManga> = emptyList(),
)
