package com.san.kir.manger.repositories

import androidx.lifecycle.LiveData
import android.content.Context
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.utils.SortLibraryUtil
import com.san.kir.manger.utils.enums.MangaFilter
import com.san.kir.manger.utils.enums.SortLibrary
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CategoryRepository(context: Context) {
    private val db = getDatabase(context)
    private val mCategoryDao = db.categoryDao

    fun getItems(): List<Category> {
        return mCategoryDao.getItems()
    }

    fun loadItem(catName: String): LiveData<Category> {
        return mCategoryDao.loadItem(catName)
    }

    fun loadItems(): LiveData<List<Category>> {
        return mCategoryDao.loadItems()
    }

    fun toFilter(category: Category): MangaFilter {
        return when (SortLibraryUtil.toType(category.typeSort)) {
            SortLibrary.AddTime ->
                if (category.isReverseSort) {
                    MangaFilter.ADD_TIME_DESC
                } else {
                    MangaFilter.ADD_TIME_ASC
                }
            SortLibrary.AbcSort ->
                if (category.isReverseSort) {
                    MangaFilter.ABC_SORT_DESC
                } else {
                    MangaFilter.ABC_SORT_ASC
                }
            SortLibrary.Populate ->
                if (category.isReverseSort) {
                    MangaFilter.POPULATE_DESC
                } else {
                    MangaFilter.POPULATE_ASC
                }
        }
    }

    fun categoryNames(): List<String> {
        return getItems().map { it.name }
    }

    fun insert(vararg category: Category) = GlobalScope.launch { mCategoryDao.insert(*category) }
    fun update(vararg category: Category) = GlobalScope.launch { mCategoryDao.update(*category) }
    fun delete(vararg category: Category) = GlobalScope.launch { mCategoryDao.delete(*category) }
}

