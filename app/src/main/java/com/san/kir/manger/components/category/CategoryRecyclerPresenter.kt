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
import kotlinx.coroutines.withContext
import java.util.*


class CategoryRecyclerPresenter(private val act: CategoryActivity) : RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory
        .createDraggable(
            view = { CategoryItemView(act, this) },
            itemMove = { fromPosition, toPosition -> onItemMoveFun(fromPosition, toPosition) })

    private val mItemTouchHelper: ItemTouchHelper by lazy {
        ItemTouchHelper(SimpleItemTouchHelperCallback(adapter))
    }


    override fun into(recyclerView: RecyclerView) {
        recyclerView.adapter = adapter
        mItemTouchHelper.attachToRecyclerView(recyclerView)
        swapItems()
    }

    fun add(category: Category) = act.lifecycleScope.launch {
        category.order = adapter.itemCount + 1
        act.mViewModel.insert(category)
        swapItems()
    }

    fun remove(cat: Category) = act.lifecycleScope.launch {
        act.mViewModel.delete(cat)
        swapItems()
    }

    private fun swapItems() = act.lifecycleScope.launch(Dispatchers.Default) {
        val old = adapter.items
        val new = act.mViewModel.items()
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

    private fun RecyclerViewAdapterFactory.DraggableRecyclerViewAdapter<Category>.onItemMoveFun(
        from: Int,
        to: Int
    ) {
        act.lifecycleScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) {
                Collections.swap(items, from, to)
            }
            notifyItemMoved(from, to)
            withContext(Dispatchers.Default) {
                items.forEachIndexed { index, category ->
                    category.order = index
                }
                act.mViewModel.update(*items.toTypedArray())
            }
        }
    }
}
