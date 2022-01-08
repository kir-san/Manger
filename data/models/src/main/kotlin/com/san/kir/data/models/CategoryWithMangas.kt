package com.san.kir.data.models

import androidx.room.Embedded
import androidx.room.Relation
import com.san.kir.data.models.columns.CategoryColumn

data class CategoryWithMangas(
    @Embedded val category: Category = Category(),

    @Relation(
        parentColumn = CategoryColumn.name,
        entityColumn = Manga.Col.category,
        entity = Manga::class,
        projection = [
            Manga.Col.id,
            Manga.Col.name,
            Manga.Col.logo,
            Manga.Col.color,
            Manga.Col.populate,
            Manga.Col.category
        ]
    )
    var mangas: List<SimpleManga> = emptyList(),
)
