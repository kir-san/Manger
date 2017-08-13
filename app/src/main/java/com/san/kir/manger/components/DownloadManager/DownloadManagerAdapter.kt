package com.san.kir.manger.components.DownloadManager

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.san.kir.manger.components.ChaptersDownloader.ChaptersDownloader
import com.san.kir.manger.dbflow.models.DownloadItem

class DownloadManagerAdapter : RecyclerView.Adapter<DownloadManagerAdapter.ViewHolder>() {

    private val catalog: MutableList<DownloadItem> = mutableListOf()
    private var sizeChanged: ((Int) -> Unit)? = null

    init {
        catalog.clear()
        ///test\\\

        /*val item = DownloadItem("hello", "link", "")
        item.max.item = 23
        item.progress.item = 10
        catalog.add(item)*/

        ///end test\\\
        catalog.addAll(ChaptersDownloader.catalog)
        sizeChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DownloadManagerItemView(this), parent)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder!!.bind(catalog[position])
    }

    override fun getItemCount() = catalog.size

    fun removeItem(task: DownloadItem) {
        catalog.forEachIndexed { i, item ->
            if (item.link == task.link) {
                catalog.removeAt(i)
                notifyItemRemoved(i)
                sizeChanged()
                return
            }
        }
    }

    fun removeAll() {
        catalog.clear()
        notifyDataSetChanged()
        sizeChanged()
    }

    class ViewHolder(val view: DownloadManagerItemView, parent: ViewGroup) :
            RecyclerView.ViewHolder(view.createView(parent)) {
        fun bind(el: DownloadItem) {
            view.bind(el)
        }
    }

    private fun sizeChanged() {
        sizeChanged?.invoke(itemCount)
    }

    fun onSizeChanged(function: (Int) -> Unit) {
        sizeChanged = function
        sizeChanged()
    }
}
