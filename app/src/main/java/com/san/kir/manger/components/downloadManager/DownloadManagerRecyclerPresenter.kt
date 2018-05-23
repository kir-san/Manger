package com.san.kir.manger.components.downloadManager

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.dao.DownloadDao
import com.san.kir.manger.room.dao.deleteAsync
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch


fun loadingAdapter(act: DownloadManagerActivity): DownloadManagerRecyclerPresenter {
    return DownloadManagerRecyclerPresenter(
        act,
        { loadLoadingDownloads() },
        { DownloadManagerItemView(act) },
        false
    )
}


fun otherAdapter(act: DownloadManagerActivity): DownloadManagerRecyclerPresenter {
    return DownloadManagerRecyclerPresenter(
        act,
        { loadOtherDownloads() },
        { DownloadManagerItemView(act) },
        true
    )
}


class DownloadManagerRecyclerPresenter(
    private val act: DownloadManagerActivity,
    private val pagedList: DownloadDao.() -> LiveData<List<DownloadItem>>,
    view: () -> RecyclerViewAdapterFactory.AnkoView<DownloadItem>,
    private val isSwiped: Boolean = true
) :
    RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory
        .createSimple(view = view)
    private val dao = Main.db.downloadDao

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recyclerView.adapter = adapter
        dao.pagedList().observe(act, Observer {
            launch {
                it?.let {
                    adapter.items = it
                    async(UI) {
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
        if (isSwiped) {
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView?,
                    viewHolder: RecyclerView.ViewHolder?,
                    target: RecyclerView.ViewHolder?
                ): Boolean {
                    return false
                }

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {
                    val position = viewHolder.adapterPosition
                    dao.deleteAsync(adapter.items[position])
                }

            }).attachToRecyclerView(recyclerView)
        }
    }

}