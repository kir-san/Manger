package com.san.kir.manger.ui.application_navigation.categories.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.models.base.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
) : ViewModel() {

    val categories = categoryDao.loadItems()

    fun swapMenuItems(from: Int, to: Int) {
        viewModelScope.defaultLaunch {
            val items = categoryDao.getItems()
            Collections.swap(items, from, to)
            items.onEachIndexed { i, m -> m.order = i }
            categoryDao.update(*items.toTypedArray())
        }
    }

    fun update(category: Category) = viewModelScope.defaultLaunch {
        categoryDao.update(category)
    }
}
