package com.san.kir.manger.components.Category

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.san.kir.manger.dbflow.models.Category
import com.san.kir.manger.utils.ItemTouchHelperViewHolder

class CategoryViewHolder(val view: CategoryItemView, parent: ViewGroup) :
        RecyclerView.ViewHolder(view.createView(parent)), ItemTouchHelperViewHolder {

    fun bind(category: Category) {
        view.bind(category, this)
    }

    override fun onItemSelected() {
        itemView.setBackgroundColor(Color.LTGRAY)
    }

    override fun onItemClear() {
        itemView.setBackgroundColor(0)
    }
}
