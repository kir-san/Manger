package com.san.kir.manger.components.ListChapters

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.san.kir.manger.dbflow.models.Chapter

class ListChaptersViewHolder(val view: ListChaptersItemView, parent: ViewGroup) :
        RecyclerView.ViewHolder(view.createView(parent)) {

    fun bind(chapter: Chapter, isSelect: Boolean) {
        view.bind(chapter, isSelect, adapterPosition)
    }
}
