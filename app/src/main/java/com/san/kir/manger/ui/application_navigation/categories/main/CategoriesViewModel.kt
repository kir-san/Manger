package com.san.kir.manger.ui.application_navigation.categories.main

import android.app.Application
import androidx.lifecycle.ViewModel
import com.san.kir.manger.room.dao.CategoryDao
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.utils.coroutines.defaultLaunchInVM
import com.san.kir.manger.workmanager.UpdateCategoryInMangaWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val context: Application,
    private val categoryDao: CategoryDao,
) : ViewModel() {

    val categories = categoryDao.loadItems()

    fun swapMenuItems(from: Int, to: Int) {
        defaultLaunchInVM {
            val items = categoryDao.getItems()
            Collections.swap(items, from, to)
            items.onEachIndexed { i, m -> m.order = i }
            categoryDao.update(*items.toTypedArray())
        }
    }

    fun update(category: Category, oldName: String = "") {
        defaultLaunchInVM {
            categoryDao.update(category)
            if (oldName.isNotEmpty())
                UpdateCategoryInMangaWorker.addTask(context, category, oldName)
        }
    }
}
