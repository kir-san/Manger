package com.san.kir.manger.components.DownloadManager

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.DownloadDao
import com.san.kir.manger.room.DAO.delete
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory


fun loadingAdapter(injector: KodeinInjector): DownloadManagerRecyclerPresenter {
    return DownloadManagerRecyclerPresenter(injector,
                                            { loadLoadingDownloads() },
                                            { DownloadLoadingItemView(injector) },
                                            false)
}

fun pauseAdapter(injector: KodeinInjector): DownloadManagerRecyclerPresenter {
    return DownloadManagerRecyclerPresenter(injector,
                                            { loadPauseDownloads() },
                                            { DownloadPauseItemView() })
}

fun errorAdapter(injector: KodeinInjector): DownloadManagerRecyclerPresenter {
    return DownloadManagerRecyclerPresenter(injector,
                                            { loadErrorDownloads() },
                                            { DownloadErrorItemView() })
}

fun completeAdapter(injector: KodeinInjector): DownloadManagerRecyclerPresenter {
    return DownloadManagerRecyclerPresenter(injector,
                                            { loadCompleteDownloads() },
                                            { DownloadCompleteItemView() })
}


class DownloadManagerRecyclerPresenter(injector: KodeinInjector,
                                       private val pagedList: DownloadDao.() -> LiveData<List<DownloadItem>>,
                                       view: () -> RecyclerViewAdapterFactory.AnkoView<DownloadItem>,
                                       private val isSwiped: Boolean = true) :
        RecyclerPresenter(), DownloadListener {
    private val adapter = RecyclerViewAdapterFactory
            .createSimple(view = view)
    private val act: DownloadManagerActivity by injector.instance()
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
