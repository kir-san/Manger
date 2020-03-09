package com.san.kir.manger.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.repositories.CategoryRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.workmanager.UpdateCategoryInMangaWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CategoryViewModel(app: Application) : AndroidViewModel(app) {
    private val mCategoryRepository = CategoryRepository(app)
    private val mMangaRepository = MangaRepository(app)

    suspend fun categoryNames() = mCategoryRepository.categoryNames()
    suspend fun items() = mCategoryRepository.items()
    suspend fun insert(category: Category) = mCategoryRepository.insert(category)
    suspend fun update(vararg category: Category) = mCategoryRepository.update(*category)
    suspend fun delete(category: Category) = mCategoryRepository.delete(category)

    fun categoryUpdateWithManga(category: Category, old: String = "") {
        viewModelScope.launch(Dispatchers.Default) {
            update(category)
            UpdateCategoryInMangaWorker.addTask(getApplication(), category, old)
        }
    }
}

