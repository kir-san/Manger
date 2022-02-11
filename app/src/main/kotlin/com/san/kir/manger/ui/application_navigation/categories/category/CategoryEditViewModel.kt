package com.san.kir.manger.ui.application_navigation.categories.category

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.support.CATEGORY_ALL
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.models.base.Category
import com.san.kir.manger.foreground_work.workmanager.RemoveCategoryWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CategoryEditViewModel @Inject constructor(
    private val context: Application,
    private val categoryDao: CategoryDao,
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryEditState())
    val state = _state.asStateFlow()

    private val _hasCreatedNew = MutableStateFlow(false)
    private val _currentCategory = MutableStateFlow(Category())
    private val _oldCategoryName = MutableStateFlow("")
    private val _hasChanges = MutableStateFlow(false)

    init {
        viewModelScope.defaultLaunch {
            combine(
                categoryDao.loadItems(),
                _currentCategory,
                _hasCreatedNew,
                _oldCategoryName,
                _hasChanges,
            ) { items, cat, hasCreatedNew, oldName, changes ->
                CategoryEditState(
                    category = cat,
                    hasCreatedNew = hasCreatedNew,
                    categoryNames = items.map { it.name },
                    oldCategoryName = oldName,
                    hasChanges = changes,
                    hasAll = oldName == context.CATEGORY_ALL
                )
            }
                .catch { t -> throw t }
                .collect { item -> _state.value = item }
        }
    }

    fun setCategory(categoryName: String) = viewModelScope.defaultLaunch {
        if (categoryName.isNotEmpty()) {
            _currentCategory.value = categoryDao.itemByName(categoryName)
        } else {
            _hasCreatedNew.value = true
            _currentCategory.value = createNewCategory()
        }
        _oldCategoryName.value = categoryName
    }

    private suspend fun createNewCategory(): Category {
        return Category(order = categoryDao.items().count() + 1)
    }

    fun update(action: Category.() -> Unit) {
        _currentCategory.update { it.apply { action() } }
    }

    fun nullChanges() {
        _hasChanges.value = false
    }

    fun newChanges() {
        _hasChanges.value = true
    }

    fun save() = viewModelScope.defaultLaunch {
        _state.value.apply {
            if (hasCreatedNew) {
                categoryDao.insert(category)
            } else {
                categoryDao.update(category)
            }
        }
    }

    fun delete() {
        RemoveCategoryWorker.addTask(context, _currentCategory.value)
    }
}

data class CategoryEditState(
    val category: Category = Category(),
    val categoryNames: List<String> = emptyList(),
    val hasCreatedNew: Boolean = false,
    val oldCategoryName: String = "",
    val hasAll: Boolean = false,
    val hasChanges: Boolean = false,
)
