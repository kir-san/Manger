package com.san.kir.manger.components.Storage

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

class StorageMainDirViewHolder(private val view: StorageMainDirItemView,
                               parent: ViewGroup) : RecyclerView.ViewHolder(view.createView(parent)) {
    fun bind(storageDir: StorageDir,
             storageFragment: StorageMainDirFragment) {
        view.bind(storageDir, storageFragment)
    }


}
