package com.san.kir.manger.components.Storage

import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.loadPagedStorageItems
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory

class StorageRecyclerPresenter(private val act: StorageActivity) : RecyclerPresenter() {
    private var adapter = RecyclerViewAdapterFactory
            .createPaging({ StorageItemView(act) },
                          { oldItem, newItem -> oldItem.id == newItem.id },
                          { oldItem, newItem -> oldItem == newItem })
    private val storage = Main.db.storageDao

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recyclerView.adapter = this.adapter
        storage.loadPagedStorageItems().observe(act, Observer(adapter::setList) )
    }
}
