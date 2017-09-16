package com.san.kir.manger.components.Storage

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

class StorageMangaDirViewHolder(private val view: StorageMangaDirItemView,
                                parent: ViewGroup) : RecyclerView.ViewHolder(view.createView(parent)) {
    fun bind(storageDir: StorageItem) = view.bind(storageDir)
}
