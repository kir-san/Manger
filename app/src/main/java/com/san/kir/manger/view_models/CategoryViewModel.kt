package com.san.kir.manger.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.san.kir.manger.repositories.CategoryRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.models.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CategoryViewModel(app: Application) : AndroidViewModel(app) {
    private val mCategoryRepository = CategoryRepository(app)
    private val mMangaRepository = MangaRepository(app)

    fun getCategoryNames(): List<String> {
        return mCategoryRepository.categoryNames()
    }

    fun categoryUpdate(vararg category: Category) {
        mCategoryRepository.update(*category)
    }

    fun getCategoryItems(): List<Category> {
        return mCategoryRepository.getItems()
    }

    fun categoryInsert(category: Category): Job {
        return mCategoryRepository.insert(category)
    }

    fun categoryDelete(category: Category): Job {
        return mCategoryRepository.delete(category)
    }

    fun categoryUpdateWithManga(category: Category, old: String = "") {
        GlobalScope.launch(Dispatchers.Default) {
            categoryUpdate(category)

            if (category.name != old) {
                mMangaRepository.update(
                    *mMangaRepository
                        .getItemsWhere(old)
                        .onEach {
                            it.categories = category.name
                        }
                        .toTypedArray()
                )
            }
        }
    }
}

