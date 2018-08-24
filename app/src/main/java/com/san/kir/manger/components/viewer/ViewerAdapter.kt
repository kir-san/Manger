package com.san.kir.manger.components.viewer

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import org.jetbrains.anko.AnkoContext
import java.io.File

open class ViewerAdapter(private val act: ViewerActivity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var items: List<File> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.firstWithPrev -> OtherViewHolder(FirstWithPrevItemView(act), parent)
            ItemType.firstNonePrev -> OtherViewHolder(FirstNonePrevItemView(act), parent)
            ItemType.lastWithNext -> OtherViewHolder(LastWithNextItemView(act), parent)
            ItemType.lastNoneNext -> OtherViewHolder(LastNoneNextItemView(act), parent)
            else -> ViewerAdapter.PageViewHolder(PageItemView(act), parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? PageViewHolder)?.view?.bind(items[position])
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 ->
                if (items.first().name == "prev")
                    ItemType.firstWithPrev
                else
                    ItemType.firstNonePrev
            items.lastIndex ->
                if (items.last().name == "next")
                    ItemType.lastWithNext
                else
                    ItemType.lastNoneNext

            else -> ItemType.page
        }
    }

    class PageViewHolder(val view: PageItemView, parent: ViewGroup) :
        RecyclerView.ViewHolder(view.createView(AnkoContext.create(parent.context, parent)))

    class OtherViewHolder(view: OtherItemView, parent: ViewGroup) :
        RecyclerView.ViewHolder(view.createView(AnkoContext.create(parent.context, parent)))
}

private object ItemType {
    const val firstWithPrev = 0
    const val firstNonePrev = 1
    const val lastWithNext = 2
    const val lastNoneNext = 3
    const val page = 4
}

