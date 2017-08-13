package com.san.kir.manger.components.LatestChapters

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.util.SparseBooleanArray
import android.view.ViewGroup
import com.san.kir.manger.components.ChaptersDownloader.ChaptersDownloader
import com.san.kir.manger.components.LatestChapters.LatestChaptersAdapter.ViewHolder
import com.san.kir.manger.dbflow.models.LatestChapter
import com.san.kir.manger.dbflow.wrapers.ChapterWrapper
import com.san.kir.manger.utils.CHAPTER_STATUS

class LatestChaptersAdapter : Adapter<ViewHolder>() {
    private var mChapters = listOf<LatestChapter>()
    //    private var dChapters = listOf<LatestChapter>()
    private val selectedItems = SparseBooleanArray()

    init {
        setHasStableIds(true)
        update()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        return ViewHolder(LatestChaptersViewModel(), parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mChapters[position])
    }

    override fun getItemCount() = mChapters.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    fun getCatalog() = mChapters

    fun update() {
        mChapters = emptyList()
        mChapters = ChapterWrapper.getLatestChapters().reversed()
        for (index in mChapters.indices) {
            mChapters[index].isRead =
                    ChapterWrapper
                            .getChapters(mChapters[index].manga)
                            .filter { it.name == mChapters[index].name }
                            .first()
                            .isRead
        }
//            dChapters = mChapters

        notifyDataSetChanged()
    }

    fun hasNewChapters(): Boolean {
        return mChapters
                .filter { !it.isRead }
                .filter { it.action == CHAPTER_STATUS.DOWNLOADABLE }
                .isNotEmpty()
    }

    fun clearHistory() {
        mChapters.forEach(LatestChapter::delete)
        update()
    }

    fun downloadNewChapters() {
        mChapters
                .filter { !it.isRead }
                .filter { it.action == CHAPTER_STATUS.DOWNLOADABLE }
                .map { ChaptersDownloader.addTask(it) }
        notifyDataSetChanged()
    }

    // Переключение методов выбора
    fun toggleSelection(position: Int) {
        // Добавление или удаление позиции в selectedItems
        val value = !selectedItems.get(position)
        if (value)
            selectedItems.put(position, value)
        else
            selectedItems.delete(position)
        notifyItemChanged(position)
    }

    fun selectAll() {
        repeat(mChapters.size) { i ->
            selectedItems.put(i, true)
            notifyItemChanged(i)
        }
    }

    // Очистить выделение
    fun removeSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    // Получение количества выбранных элементов
    fun getSelectedCount(): Int {
        return selectedItems.size()
    }

    class ViewHolder(val vm: LatestChaptersViewModel, parent: ViewGroup) :
            RecyclerView.ViewHolder(LatestChaptersItemView(vm).createView(parent)) {

        fun bind(chapter: LatestChapter) {
            vm.bind(chapter)
        }
    }
}
