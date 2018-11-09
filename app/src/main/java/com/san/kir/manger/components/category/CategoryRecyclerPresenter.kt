package com.san.kir.manger.components.category

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.dao.loadMangaWhereCategory
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.SimpleItemTouchHelperCallback
import kotlinx.coroutines.launch
import java.util.*


class CategoryRecyclerPresenter(private val act: CategoryActivity) : RecyclerPresenter() {
    private val catDao = Main.db.categoryDao

    private val adapter = RecyclerViewAdapterFactory
        .createDraggable(view = { CategoryItemView(this) },
                         itemMove = { fromPosition, toPosition ->
                             Collections.swap(items, fromPosition, toPosition)
                             notifyItemMoved(fromPosition, toPosition)
                             items.forEachIndexed { index, category ->
                                 category.order = index
                             }
                             catDao.update(*items.toTypedArray())
                         })

    private val mItemTouchHelper: ItemTouchHelper by lazy {
        ItemTouchHelper(SimpleItemTouchHelperCallback(adapter))
    }

    private fun swapItems() = act.launch(act.coroutineContext) {
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

    fun add(category: Category) = act.launch(act.coroutineContext) {
        category.order = adapter.itemCount + 1
        catDao.insert(category)
        swapItems()
    }

    fun update(cat: Category, old: String = "") = act.launch(act.coroutineContext) {
        if (old.isEmpty())
            catDao.update(cat)
        else
            with(Main.db.mangaDao) {
                update(*loadMangaWhereCategory(old).onEach {
                    it.categories = cat.name
                }.toTypedArray())
                catDao.update(cat)
            }
//        swapItems()
    }

    fun remove(cat: Category) = act.launch(act.coroutineContext) {
        catDao.delete(cat)
        swapItems()
    }
}
