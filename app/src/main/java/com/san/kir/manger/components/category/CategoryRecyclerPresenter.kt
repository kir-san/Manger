package com.san.kir.manger.components.category

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.dao.insertAsync
import com.san.kir.manger.room.dao.loadMangaWhereCategory
import com.san.kir.manger.room.dao.updateAsync
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.SimpleItemTouchHelperCallback
import java.util.*


class CategoryRecyclerPresenter : RecyclerPresenter() {
    private val catDao = Main.db.categoryDao

    private val adapter = RecyclerViewAdapterFactory
            .createDraggable(view = { CategoryItemView(this) },
                             itemMove = { fromPosition, toPosition ->
                                Collections.swap(items, fromPosition, toPosition)
                                notifyItemMoved(fromPosition, toPosition)
                                items.forEachIndexed { index, category ->
                                    category.order = index
                                }
                                catDao.updateAsync(*items.toTypedArray())
                            })

    private val mItemTouchHelper: ItemTouchHelper by lazy {
        ItemTouchHelper(SimpleItemTouchHelperCallback(adapter))
    }

    private fun swapItems() {
        val old = adapter.items
        val new = catDao.loadCategories()
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    old[oldItemPosition].id == new[newItemPosition].id

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    old[oldItemPosition] == new[newItemPosition]

            override fun getOldListSize() = old.size

            override fun getNewListSize() = new.size
        })
        adapter.items = new
        result.dispatchUpdatesTo(adapter)
    }

    override fun into(recyclerView: RecyclerView) {
        recyclerView.adapter = adapter
        mItemTouchHelper.attachToRecyclerView(recyclerView)
        swapItems()
    }

    fun add(category: Category) {
        category.order = adapter.itemCount + 1
        catDao.insertAsync(category)
        swapItems()
    }

    fun update(cat: Category, old: String = "") {
        if (old.isEmpty())
            catDao.updateAsync(cat)
        else
            with(Main.db.mangaDao) {
                updateAsync(*loadMangaWhereCategory(old).onEach { it.categories = cat.name }.toTypedArray())
                catDao.updateAsync(cat)
            }
//        swapItems()
    }

    fun remove(cat: Category) {
        catDao.delete(cat)
        swapItems()
    }

}
