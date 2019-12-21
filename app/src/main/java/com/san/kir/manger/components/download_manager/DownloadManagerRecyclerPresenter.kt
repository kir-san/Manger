package com.san.kir.manger.components.download_manager

import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.san.kir.manger.services.DownloadService
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun allAdapter(act: DownloadManagerActivity) = DownloadManagerRecyclerPresenter(act)

class DownloadManagerRecyclerPresenter(private val act: DownloadManagerActivity) :
    RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory
        .createSimple { DownloadManagerItemView(act) }

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recyclerView.adapter = adapter
        act.mViewModel.getDownloadItems().observe(act, Observer { items ->
            items?.let {
                adapter.items = it
                adapter.notifyDataSetChanged()
            }
        })
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                act.lifecycleScope.launch(Dispatchers.Default) {
                    DownloadService.pause(act, adapter.items[position])
                    act.mViewModel.delete(adapter.items[position])
                    adapter.items = adapter.items - adapter.items[position]
                    withContext(Dispatchers.Main) {
                        adapter.notifyItemRemoved(position)
                    }
                }
            }

        }).attachToRecyclerView(recyclerView)
    }

}
