package com.san.kir.manger.components.category

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.SimpleItemTouchHelperCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class CategoryRecyclerPresenter(private val act: CategoryActivity) : RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory
        .createDraggable(view = { CategoryItemView(act, this) },
                         itemMove = { fromPosition, toPosition ->
                             Collections.swap(items, fromPosition, toPosition)
                             notifyItemMoved(fromPosition, toPosition)
                             items.forEachIndexed { index, category ->
                                 category.order = index
                             }
                             act.mViewModel.categoryUpdate(*items.toTypedArray())
                         })

    private val mItemTouchHelper: ItemTouchHelper by lazy {
        ItemTouchHelper(SimpleItemTouchHelperCallback(adapter))
    }

    private fun swapItems() = act.lifecycleScope.launch(Dispatchers.Default) {
        val old = adapter.items
        val new = act.mViewModel.getCategoryItems()
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

    fun add(category: Category) = act.lifecycleScope.launch {
        category.order = adapter.itemCount + 1
        act.mViewModel.categoryInsert(category).invokeOnCompletion {
            swapItems()
        }
    }

    fun remove(cat: Category) = act.lifecycleScope.launch {
        act.mViewModel.categoryDelete(cat).invokeOnCompletion {
            swapItems()
        }
    }
}
