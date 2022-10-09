package com.san.kir.categories.logic.repo

import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.models.base.Category
import java.util.Collections
import javax.inject.Inject

internal class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {

    val items = categoryDao.loadItems()
    val names = categoryDao.loadNames()

    suspend fun swap(from: Int, to: Int) = withDefaultContext {
        val items = categoryDao.items()
        Collections.swap(items, from, to)
        categoryDao.update(
            *items
                .mapIndexed { i, m -> m.copy(order = i) }
                .toTypedArray()
        )
    }

    suspend fun update(category: Category) = withDefaultContext { categoryDao.update(category) }
    suspend fun insert(category: Category) = withDefaultContext { categoryDao.insert(category) }

    suspend fun item(categoryName: String) = withDefaultContext {
        if (categoryName.isNotEmpty()) {
            categoryDao.itemByName(categoryName)
        } else {
            createNewCategory()
        }
    }

    private suspend fun createNewCategory(): Category {
        return Category(order = categoryDao.items().count() + 1)
    }
}
