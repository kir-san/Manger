package com.san.kir.manger.components.latest_chapters

import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.san.kir.manger.room.entities.toDownloadItem
import com.san.kir.manger.services.DownloadService
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LatestChaptersRecyclerPresenter(private val act: LatestChapterActivity) :
    RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory
        .createPaging({ LatestChaptersItemView(act) },
                      { oldItem, newItem -> oldItem.id == newItem.id },
                      { oldItem, newItem -> oldItem == newItem })

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recycler.adapter = adapter

        act.lifecycleScope.launchWhenResumed {
            act.mViewModel
                .loadPagedItems()
                .collect { adapter.submitList(it) }
        }


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
                    adapter.currentList?.get(position)?.let { chapter ->
                        chapter.isInUpdate = false
                        act.mViewModel.update(chapter)
                    }
                }
            }
        }).attachToRecyclerView(recyclerView)
    }

    suspend fun hasNewChapters() =
        withContext(Dispatchers.Default) { act.mViewModel.hasNewChapters() }

    fun downloadNewChapters() = act.lifecycleScope.launch(Dispatchers.Main) {
        withContext(Dispatchers.Default) {
            act.mViewModel.newChapters().onEach { chapter ->
                DownloadService.addOrStart(act, chapter.toDownloadItem())
            }
        }

        adapter.notifyDataSetChanged()
    }
}
