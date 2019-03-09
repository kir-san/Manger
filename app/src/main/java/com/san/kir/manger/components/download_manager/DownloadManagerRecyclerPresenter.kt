package com.san.kir.manger.components.download_manager

import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun allAdapter(act: DownloadManagerActivity): DownloadManagerRecyclerPresenter {
    return DownloadManagerRecyclerPresenter(
        act
    )
}

class DownloadManagerRecyclerPresenter(
    private val act: DownloadManagerActivity
) :
    RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory
        .createSimple { DownloadManagerItemView(act) }

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recyclerView.adapter = adapter
        act.mViewModel.getDownloadItems().observe(act, Observer { items ->
            act.launch(act.coroutineContext) {
                items?.let {
                    adapter.items = it
                    withContext(Dispatchers.Main) {
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                val position = viewHolder.adapterPosition
                DownloadService.pause(act, adapter.items[position])
                act.mViewModel.downloadDelete(adapter.items[position])
            }

        }).attachToRecyclerView(recyclerView)
    }

}
