package com.san.kir.manger.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.paging.PagedList
import com.san.kir.manger.repositories.CategoryRepository
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.LatestChapterRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.enums.MangaFilter
import com.san.kir.manger.utils.getFullPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LibraryViewModel(app: Application) : AndroidViewModel(app) {
    private val mMangaRepository = MangaRepository(app)
    private val mCategoryRepository = CategoryRepository(app)
    private val mChapterRepository = ChapterRepository(app)
    private val mLatestChapterRepository = LatestChapterRepository(app)

    private lateinit var mCategories: List<Category>
    private val mCategoriesMap = hashMapOf<String, LiveData<Category>>()

    fun getMangas(): List<Manga> {
        return mMangaRepository.getItems()
    }

    fun getCategoryItems(): List<Category> {
        if (!::mCategories.isInitialized) {
            mCategories = mCategoryRepository.getItems()
        }

        return mCategories
    }

    fun loadCategory(catName: String): LiveData<Category> {
        val cat = mCategoriesMap[catName]
        return if (cat == null) {
            mCategoriesMap[catName] = mCategoryRepository.loadItem(catName)
            mCategoriesMap[catName]!!
        } else {
            cat
        }
    }

    fun filterFromCategory(category: Category): MangaFilter {
        return mCategoryRepository.toFilter(category)
    }

    fun loadMangas(cat: Category, filter: MangaFilter): LiveData<PagedList<Manga>> {
        return mMangaRepository.loadMangas(cat, filter)
    }

    fun countNotReadChapters(manga: Manga): Int {
        return mChapterRepository.countNotRead(manga.unic)
    }

    fun mangaUpdate(manga: Manga) {
        mMangaRepository.update(manga)
    }

    fun removeWithChapters(manga: Manga, withFiles: Boolean = false) =
        GlobalScope.launch(Dispatchers.Default) {
            mMangaRepository.delete(manga)

            mChapterRepository.deleteItems(manga.unic)

            mLatestChapterRepository.deleteItems(manga.unic)

            if (withFiles) {
                getFullPath(manga.path).deleteRecursively()
            }
        }
}

