package com.san.kir.manger.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.san.kir.manger.repositories.CategoryRepository
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.utils.enums.MangaFilter

class LibraryViewModel(app: Application) : AndroidViewModel(app) {
    private val mMangaRepository = MangaRepository(app)
    private val mCategoryRepository = CategoryRepository(app)
    private val mChapterRepository = ChapterRepository(app)

    private lateinit var mCategories: List<Category>

    fun getMangas() = mMangaRepository.getItems()

    suspend fun getCategoryItems(): List<Category> {
        if (!::mCategories.isInitialized) {
            mCategories = mCategoryRepository.items()
        }

        return mCategories
    }

    fun filterFromCategory(category: Category) = mCategoryRepository.toFilter(category)
    fun loadMangas(cat: Category, filter: MangaFilter) = mMangaRepository.loadMangas(cat, filter)

    suspend fun countNotReadChapters(manga: Manga) = mChapterRepository.countNotRead(manga.unic)
    suspend fun update(manga: Manga) = mMangaRepository.update(manga)
}

