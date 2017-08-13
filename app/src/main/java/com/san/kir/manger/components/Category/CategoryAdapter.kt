package com.san.kir.manger.components.Category

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.san.kir.manger.dbflow.models.Category
import com.san.kir.manger.dbflow.wrapers.CategoryWrapper
import com.san.kir.manger.utils.ItemTouchHelperAdapter
import com.san.kir.manger.utils.OnStartDragListener
import java.util.*

class CategoryAdapter(dragStartListener: OnStartDragListener) :
        RecyclerView.Adapter<CategoryViewHolder>(), ItemTouchHelperAdapter {

    var mCategories: MutableList<Category> = CategoryWrapper.getCategories().toMutableList()
    var mDragStartListener: OnStartDragListener = dragStartListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(CategoryItemView(this), parent)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(mCategories[position])
    }

    override fun getItemCount(): Int {
        return mCategories.size
    }

    fun isLastItem(): Boolean = mCategories.size <= 1

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(mCategories, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        CategoryWrapper.updateCategories(mCategories)
        return true
    }

    fun addCategory(name: String) {
        val cat = Category(name, mCategories.size)
        cat.insert()
        mCategories.add(cat)
        notifyItemInserted(mCategories.size - 1)
    }
}
