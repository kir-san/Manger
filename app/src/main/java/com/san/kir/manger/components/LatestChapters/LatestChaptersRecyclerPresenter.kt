package com.san.kir.manger.components.LatestChapters

import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.clearHistory
import com.san.kir.manger.room.DAO.clearHistoryDownload
import com.san.kir.manger.room.DAO.clearHistoryRead
import com.san.kir.manger.room.DAO.downloadNewChapters
import com.san.kir.manger.room.DAO.hasNewChapters
import com.san.kir.manger.room.DAO.loadPagedLatestChapters
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.experimental.launch

class LatestChaptersRecyclerPresenter(private val act: LatestChapterActivity) :
    RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory
        .createPaging({ LatestChaptersItemView(act) },
                      { oldItem, newItem -> oldItem.id == newItem.id },
                      { oldItem, newItem -> oldItem == newItem })
    private val latest = Main.db.latestChapterDao

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recycler.adapter = adapter
        latest.loadPagedLatestChapters().observe(act, Observer(adapter::setList))
    }

    suspend fun hasNewChapters() = latest.hasNewChapters().await()

    fun downloadNewChapters() = launch {
        latest.downloadNewChapters().await().onEach { chapter ->
            val item = DownloadItem(
                name = chapter.manga + " " + chapter.name,
                link = chapter.site,
                path = chapter.path
            )
            act.downloadManager.addOrStart(item)
        }
        adapter.notifyDataSetChanged()
    }

    fun clearHistory() = latest.clearHistory()

    fun clearHistoryRead() = latest.clearHistoryRead()

    fun clearHistoryDownload() = latest.clearHistoryDownload()
}
