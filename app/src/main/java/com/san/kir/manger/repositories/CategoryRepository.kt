package com.san.kir.manger.repositories

import androidx.lifecycle.LiveData
import android.content.Context
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.utils.SortLibraryUtil
import com.san.kir.manger.utils.enums.MangaFilter
import com.san.kir.manger.utils.enums.SortLibrary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryRepository(context: Context) {
    private val db = getDatabase(context)
    private val mCategoryDao = db.categoryDao


    fun loadItem(catName: String) = mCategoryDao.loadItem(catName)
    fun loadItems() = mCategoryDao.loadItems()

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

    suspend fun items() = withContext(Dispatchers.Default) { mCategoryDao.getItems() }

    suspend fun insert(vararg category: Category) = mCategoryDao.insert(*category)
    suspend fun update(vararg category: Category) = mCategoryDao.update(*category)
    suspend fun delete(vararg category: Category) = mCategoryDao.delete(*category)
}

