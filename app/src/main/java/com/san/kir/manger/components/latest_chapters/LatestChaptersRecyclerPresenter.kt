package com.san.kir.manger.components.latest_chapters

import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.san.kir.manger.components.download_manager.DownloadService
import com.san.kir.manger.room.models.toDownloadItem
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LatestChaptersRecyclerPresenter(private val act: LatestChapterActivity) :
    RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory
        .createSimple { LatestChaptersItemView(act) }

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        act.launch(act.coroutineContext) {
            recycler.adapter = adapter
            act.mViewModel
                .getLatestItems()
                .observe(act, Observer { items ->
                    act.launch(act.coroutineContext) {
                        items?.let {
                            adapter.items = it
                            withContext(Dispatchers.Main) {
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                })
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                act.mViewModel.latestDelete(adapter.items[position])
            }
        }).attachToRecyclerView(recyclerView)
    }

    fun hasNewChapters() = act.async(act.coroutineContext) { act.mViewModel.latestHasNewChapters() }

    fun downloadNewChapters() = act.launch(act.coroutineContext) {
        act.mViewModel.getLatestNewChapters().onEach { chapter ->
            DownloadService.addOrStart(act, chapter.toDownloadItem())
        }
        adapter.notifyDataSetChanged()
    }

    fun clearHistory() = act.mViewModel.latestClearAll()

    fun clearHistoryRead() = act.mViewModel.latestClearRead()

    fun clearHistoryDownload() = act.mViewModel.latestClearDownloaded()
}
