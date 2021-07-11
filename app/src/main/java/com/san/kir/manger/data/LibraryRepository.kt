package com.san.kir.manger.data

import android.content.Context
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.SortLibraryUtil
import com.san.kir.manger.utils.enums.MangaFilter
import com.san.kir.manger.utils.enums.SortLibrary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class LibraryRepository(context: Context) {
    private val categoryDao = getDatabase(context).categoryDao
    private val mangaDao = getDatabase(context).mangaDao

    fun loadVisibleManga(): Flow<List<List<Manga>>> {
        return combine(categoryDao.loadItems(), mangaDao.flowItems()) { cats, mangas ->
            cats.filter { it.isVisible }.map { cat -> mangas.loadMangas(cat) }
        }
    }

    private fun toFilter(category: Category): MangaFilter {
        return when (SortLibraryUtil.toType(category.typeSort)) {
            SortLibrary.AddTime -> if (category.isReverseSort) MangaFilter.ADD_TIME_DESC
            else MangaFilter.ADD_TIME_ASC

            SortLibrary.AbcSort -> if (category.isReverseSort) MangaFilter.ABC_SORT_DESC
            else MangaFilter.ABC_SORT_ASC

            SortLibrary.Populate -> if (category.isReverseSort) MangaFilter.POPULATE_DESC
            else MangaFilter.POPULATE_ASC
        }
    }

    private fun List<Manga>.loadMangas(cat: Category): List<Manga> {
        val filter = toFilter(cat)
        if (cat.name != CATEGORY_ALL) filter { it.name == cat.name }

        return when (filter) {
            MangaFilter.ADD_TIME_ASC -> sortedBy { it.id }
            MangaFilter.ADD_TIME_DESC -> sortedByDescending { it.id }
            MangaFilter.ABC_SORT_ASC -> sortedBy { it.name }
            MangaFilter.ABC_SORT_DESC -> sortedByDescending { it.name }
            MangaFilter.POPULATE_ASC -> sortedBy { it.populate }
            MangaFilter.POPULATE_DESC -> sortedByDescending { it.populate }
        }
    }
}
