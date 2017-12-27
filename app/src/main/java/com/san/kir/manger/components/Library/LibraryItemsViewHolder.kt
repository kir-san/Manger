package com.san.kir.manger.components.Library

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.san.kir.manger.room.models.Manga

class LibraryItemsViewHolder(val view: LibraryPageItemView, parent: ViewGroup) :
        RecyclerView.ViewHolder(view.createView(parent)) {

    fun bind(manga: Manga, isSelect: Boolean) {
        view.bind(manga, isSelect, adapterPosition)
    }
}
