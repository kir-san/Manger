package com.san.kir.manger.components.latest_chapters

import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.san.kir.manger.room.entities.toDownloadItem
import com.san.kir.manger.services.DownloadService
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LatestChaptersRecyclerPresenter(private val act: LatestChapterActivity) :
    RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory
        .createSimple { LatestChaptersItemView(act) }

    override fun into(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.into(recyclerView)
        recycler.adapter = adapter
        act.mViewModel
            .getLatestItems()
            .observe(act, Observer { items ->
                items?.let {
                    adapter.items = it
                    adapter.notifyDataSetChanged()
                }
            })

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                target: androidx.recyclerview.widget.RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                act.lifecycleScope.launch(Dispatchers.Default) {
                    act.mViewModel.delete(adapter.items[position])
                }
            }
        }).attachToRecyclerView(recyclerView)
    }

    suspend fun hasNewChapters() = withContext(Dispatchers.Default) { act.mViewModel.hasNewChapters() }

    fun downloadNewChapters() = act.lifecycleScope.launch(Dispatchers.Main) {
        withContext(Dispatchers.Default) {
            act.mViewModel.newChapters().onEach { chapter ->
                DownloadService.addOrStart(act, chapter.toDownloadItem())
            }
        }

        adapter.notifyDataSetChanged()
    }
}
