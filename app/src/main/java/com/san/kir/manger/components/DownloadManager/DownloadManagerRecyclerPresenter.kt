package com.san.kir.manger.components.DownloadManager

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.DownloadDao
import com.san.kir.manger.room.DAO.delete
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory


fun loadingAdapter(act: DownloadManagerActivity): DownloadManagerRecyclerPresenter {
    return DownloadManagerRecyclerPresenter(act,
                                            { loadLoadingDownloads() },
                                            { DownloadLoadingItemView(act) },
                                            false)
}

fun pauseAdapter(act: DownloadManagerActivity): DownloadManagerRecyclerPresenter {
    return DownloadManagerRecyclerPresenter(act,
                                            { loadPauseDownloads() },
                                            { DownloadPauseItemView(act) })
}

fun errorAdapter(act: DownloadManagerActivity): DownloadManagerRecyclerPresenter {
    return DownloadManagerRecyclerPresenter(act,
                                            { loadErrorDownloads() },
                                            { DownloadErrorItemView(act) })
}

fun completeAdapter(act: DownloadManagerActivity): DownloadManagerRecyclerPresenter {
    return DownloadManagerRecyclerPresenter(act,
                                            { loadCompleteDownloads() },
                                            { DownloadCompleteItemView() })
}


class DownloadManagerRecyclerPresenter(private val act: DownloadManagerActivity,
                                       private val pagedList: DownloadDao.() -> LiveData<List<DownloadItem>>,
                                       view: () -> RecyclerViewAdapterFactory.AnkoView<DownloadItem>,
                                       private val isSwiped: Boolean = true) :
        RecyclerPresenter(), DownloadListener {
    private val adapter = RecyclerViewAdapterFactory
            .createSimple(view = view)
    private val dao = Main.db.downloadDao

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recyclerView.adapter = adapter
        dao.pagedList().observe(act, Observer {
            it?.let {
                adapter.items = it
                adapter.notifyDataSetChanged()
            }
        })
        if (isSwiped) {
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
                                                                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(recyclerView: RecyclerView?,
                                    viewHolder: RecyclerView.ViewHolder?,
                                    target: RecyclerView.ViewHolder?): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                                      direction: Int) {
                    val position = viewHolder.adapterPosition
                    dao.delete(adapter.items[position])
                }

            }).attachToRecyclerView(recyclerView)
        }
    }
}
