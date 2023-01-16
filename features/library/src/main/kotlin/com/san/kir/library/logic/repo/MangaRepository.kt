package com.san.kir.library.logic.repo

import android.app.Application
import android.content.Context
import com.san.kir.core.support.CATEGORY_ALL
import com.san.kir.core.support.SortLibraryUtil
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.core.utils.mapP
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.base.Category
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.extend.CategoryWithMangas
import com.san.kir.data.models.extend.SimplifiedManga
import com.san.kir.library.ui.library.ItemsState
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

internal class MangaRepository @Inject constructor(
    private val context: Application,
    private val mangaDao: MangaDao,
    private val categoryDao: CategoryDao,
) {
    //    Все категории
    private val _categories = categoryDao.loadItems()
    private val _mangas = mangaDao.loadSimpleItems().distinctUntilChanged()

    val itemsState = combine(_categories, _mangas) { cats, mangas ->
        if (cats.isEmpty())
            ItemsState.Empty
        else
            ItemsState.Ok(
                items = cats
                    .filter { it.isVisible }
                    .mapP { context.transform(it, mangas) },
                categories = cats.associate { it.id to it.name }.toPersistentMap()
            )
    }

    suspend fun item(mangaId: Long) = withIoContext { mangaDao.itemById(mangaId) }
    suspend fun categoryName(categoryId: Long) =
        withIoContext { categoryDao.itemById(categoryId).name }

    suspend fun update(manga: Manga) = withIoContext { mangaDao.update(manga) }

    suspend fun changeCategory(mangaId: Long, newCategoryId: Long) = withIoContext {
        mangaDao.update(mangaId, newCategoryId)
    }

    private fun Context.transform(
        cat: Category,
        mangas: List<SimplifiedManga>,
    ): CategoryWithMangas {
        var prepareMangas = mangas
            .filter { cat.name == CATEGORY_ALL || it.categoryId == cat.id }

        when (cat.typeSort) {
            SortLibraryUtil.add -> prepareMangas = prepareMangas.sortedBy { it.id }
            SortLibraryUtil.abc -> prepareMangas = prepareMangas.sortedBy { it.name }
            SortLibraryUtil.pop -> prepareMangas = prepareMangas.sortedBy { it.populate }
        }

        return CategoryWithMangas(
            id = cat.id,
            name = cat.name,
            typeSort = cat.typeSort,
            isReverseSort = cat.isReverseSort,
            spanPortrait = cat.spanPortrait,
            spanLandscape = cat.spanLandscape,
            isLargePortrait = cat.isLargePortrait,
            isLargeLandscape = cat.isLargeLandscape,
            mangas = if (cat.isReverseSort) prepareMangas.reversed() else prepareMangas
        )
    }
}
