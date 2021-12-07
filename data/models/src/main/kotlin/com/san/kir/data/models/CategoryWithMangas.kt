package com.san.kir.data.models

import androidx.room.ColumnInfo
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

data class SimpleManga(
    @ColumnInfo(name = MangaColumn.id) var id: Long = 0,
    @ColumnInfo(name = MangaColumn.name) var name: String = "",
    @ColumnInfo(name = MangaColumn.logo) var logo: String = "",
    @ColumnInfo(name = MangaColumn.color) var color: Int = 0,
    @ColumnInfo(name = MangaColumn.populate) var populate: Int = 0,
    @ColumnInfo(name = MangaColumn.categories) var categories: String = ""
)