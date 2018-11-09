package com.san.kir.manger.components.latestChapters

import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.dao.clearHistory
import com.san.kir.manger.room.dao.clearHistoryDownload
import com.san.kir.manger.room.dao.clearHistoryRead
import com.san.kir.manger.room.dao.downloadNewChapters
import com.san.kir.manger.room.dao.hasNewChapters
import com.san.kir.manger.room.dao.loadPagedLatestChapters
import com.san.kir.manger.room.models.toDownloadItem
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class LatestChaptersRecyclerPresenter(private val act: LatestChapterActivity) :
    RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory
        .createPaging({ LatestChaptersItemView(act) },
                      { oldItem, newItem -> oldItem.id == newItem.id },
                      { oldItem, newItem -> oldItem == newItem })
    private val latestChapterDao = Main.db.latestChapterDao

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        act.launch(act.coroutineContext) {
            recycler.adapter = adapter
            latestChapterDao.loadPagedLatestChapters()
                .observe(act, Observer { act.launch(Dispatchers.Main) { adapter.submitList(it) } })
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
                latestChapterDao.delete(adapter.item(position))
            }
        }).attachToRecyclerView(recyclerView)
    }

    fun hasNewChapters() = act.async(act.coroutineContext) { latestChapterDao.hasNewChapters() }

    fun downloadNewChapters() = act.launch(act.coroutineContext) {
        latestChapterDao.downloadNewChapters().onEach { chapter ->
            act.downloadManager.addOrStart(chapter.toDownloadItem())
        }
        adapter.notifyDataSetChanged()
    }

    fun clearHistory() = latestChapterDao.clearHistory()

    fun clearHistoryRead() = latestChapterDao.clearHistoryRead()

    fun clearHistoryDownload() = latestChapterDao.clearHistoryDownload()
}
