package com.san.kir.manger.room.entities

import androidx.room.Embedded
import androidx.room.Relation
import com.san.kir.manger.room.columns.CategoryColumn

data class CategoryWithMangas(
    @Embedded val category: Category = Category(),

    @Relation(
        parentColumn = CategoryColumn.name,
        entityColumn = MangaColumn.categories,
    )
    var mangas: List<Manga> = emptyList(),
)
