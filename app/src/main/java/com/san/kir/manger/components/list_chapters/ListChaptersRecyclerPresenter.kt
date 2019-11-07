package com.san.kir.manger.components.list_chapters

import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.dialogs.toast
import com.san.kir.ankofork.negative
import com.san.kir.ankofork.positive
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.action
import com.san.kir.manger.room.entities.toDownloadItem
import com.san.kir.manger.services.DownloadService
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.enums.ChapterFilter
import com.san.kir.manger.utils.enums.ChapterStatus
import com.san.kir.manger.utils.extensions.delChapters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ListChaptersRecyclerPresenter(val act: ListChaptersActivity) : RecyclerPresenter() {
    private var adapter = RecyclerViewAdapterFactory.createSimple { ListChaptersItemView(act) }
    private var mManga: Manga = Manga()
    private var mFilter = ChapterFilter.NOT_READ_DESC
    private var backupCatalog = listOf<Chapter>()

    override fun into(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.into(recyclerView)
        recycler.adapter = adapter
        act.mViewModel.filter.bind {
            changeOrder(it)
        }
    }

    fun setManga(manga: Manga = mManga, filter: ChapterFilter): Job {
        mManga = manga
        mFilter = filter
        return changeSort(manga.isAlternativeSort)
    }

    fun update() {
        changeSort(mManga.isAlternativeSort)
    }

    fun changeSort(alternative: Boolean) = act.lifecycleScope.launch(Dispatchers.Default) {
        try {
            val loadChapters = act.mViewModel.chapters(mManga)
            backupCatalog = if (alternative) {
                loadChapters.sortedWith(ChapterComparator())
            } else {
                loadChapters
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        changeOrder(mFilter)
    }

    private fun changeOrder(filter: ChapterFilter) = act.lifecycleScope.launch(Dispatchers.Default) {
        runCatching {
            this@ListChaptersRecyclerPresenter.mFilter = filter
            adapter.items = when (filter) {
                ChapterFilter.ALL_READ_ASC -> backupCatalog
                ChapterFilter.NOT_READ_ASC -> backupCatalog.filter { !it.isRead }
                ChapterFilter.IS_READ_ASC -> backupCatalog.filter { it.isRead }
                ChapterFilter.ALL_READ_DESC -> backupCatalog.reversed()
                ChapterFilter.NOT_READ_DESC -> backupCatalog.filter { !it.isRead }.reversed()
                ChapterFilter.IS_READ_DESC -> backupCatalog.filter { it.isRead }.reversed()
            }
        }.onFailure { ex ->
            ex.printStackTrace()
        }
        withContext(Dispatchers.Main) {
            adapter.notifyDataSetChanged()
        }
    }

    fun toggleSelection(position: Int) {
        adapter.selectedItems[position] = !adapter.selectedItems[position]
        adapter.notifyItemChanged(position)
    }

    fun getSelectedCount() = adapter.selectedItems.filter { it }.size

    fun deleteSelectedItems() = act.lifecycleScope.launch(Dispatchers.Default) {
        var count = 0
        forSelection { i ->
            val chapter = items[i]
            if (chapter.action == ChapterStatus.DELETE) {
                delChapters(chapter)
                count++
            }
        }
        withContext(Dispatchers.Main) {
            if (count == 0) {
                act.toast(R.string.list_chapters_selection_del_error)
            } else {
                act.toast(R.string.list_chapters_selection_del_ok)
            }
        }
    }

    fun downloadSelectedItems() = act.lifecycleScope.launch(Dispatchers.Default) {
        forSelection { i ->
            val chapter = items[i]
            // для каждого выделенный элемент
            if (chapter.action == ChapterStatus.DOWNLOADABLE) {
                DownloadService.addOrStart(act, chapter.toDownloadItem())
            }
        }
    }

    fun downloadNextNotReadChapter() = act.lifecycleScope.launch(Dispatchers.Default) {
        val chapter = act
            .mViewModel
            .chaptersNotReadAsc(mManga)
            .first { it.action == ChapterStatus.DOWNLOADABLE }

        DownloadService.addOrStart(act, chapter.toDownloadItem())

        changeOrder(mFilter)
    }

    fun downloadAllNotReadChapters() = act.lifecycleScope.launch(Dispatchers.Default) {
        val count = act
            .mViewModel
            .chaptersNotReadAsc(mManga)
            .filter { it.action == ChapterStatus.DOWNLOADABLE }
            .onEach { chapter ->
                DownloadService.addOrStart(act, chapter.toDownloadItem())
            }
            .size

        withContext(Dispatchers.Main) {
            if (count == 0)
                act.toast(R.string.list_chapters_selection_load_error)
            else
                act.toast(act.getString(R.string.list_chapters_selection_load_ok, count))
        }

        changeOrder(mFilter)
    }

    fun downloadAllChapters() = act.lifecycleScope.launch(Dispatchers.Default) {
        val count = act
            .mViewModel
            .chaptersAsc(mManga)
            .filter { it.action == ChapterStatus.DOWNLOADABLE }
            .onEach { chapter ->
                DownloadService.addOrStart(act, chapter.toDownloadItem())
            }
            .size
        withContext(Dispatchers.Main) {
            if (count == 0)
                act.toast(R.string.list_chapters_selection_load_error)
            else
                act.toast(act.getString(R.string.list_chapters_selection_load_ok, count))
        }
        changeOrder(mFilter)
    }

    fun setRead(isReading: Boolean) = act.lifecycleScope.launch(Dispatchers.Default) {
        forSelection { i ->
            // Для всех выделеных элементов
            items[i].let { chapter ->
                chapter.isRead = isReading
                act.mViewModel.update(chapter)
            }
        }
        changeOrder(mFilter)
    }

    fun removeSelection() = act.lifecycleScope.launch(Dispatchers.Main) {
        repeat(adapter.itemCount) { i ->
            adapter.selectedItems[i] = false
            adapter.notifyItemChanged(i)
        }
    }

    fun selectAll() = act.lifecycleScope.launch(Dispatchers.Main) {
        repeat(adapter.itemCount) { i ->
            adapter.selectedItems[i] = true
            adapter.notifyItemChanged(i)
        }
    }

    fun selectPrev() {
        // Выделить предидущие элементы
        val selectedSize = adapter.selectedItems.filter { it }.size
        if (selectedSize == 1) { // Работает только для одного выделенного элемента
            adapter.selectedItems.forEachIndexed { index, b ->
                if (b) {
                    repeat(adapter.selectedItems.size) { i ->
                        if (i > index) {
                            adapter.selectedItems[i] = true
                            adapter.notifyItemChanged(i)
                        }
                    }
                }
            }
        }
    }

    fun selectNext() {
        val selectedSize = adapter.selectedItems.filter { it }.size
        if (selectedSize == 1) { // Работает только для одного выделенного элемента
            adapter.selectedItems.forEachIndexed { index, b ->
                if (b) {
                    repeat(adapter.selectedItems.size) { i ->
                        if (i < index) {
                            adapter.selectedItems[i] = true
                            adapter.notifyItemChanged(i)
                        }
                    }
                }
            }
        }
    }

    fun updatePages() {
        act.mViewModel.isAction.positive()
        act.lifecycleScope.launch(Dispatchers.Default) {
            forSelection { i ->
                items[i].pages = ManageSites.pages(items[i])
                act.mViewModel.update(items[i])
            }
        }.invokeOnCompletion {
            act.mViewModel.isAction.negative()
        }
    }

    fun fullDeleteSelectedItems() = act.lifecycleScope.launch(Dispatchers.Default) {
        forSelection { i ->
            items[i].let { chapter ->
                act.mViewModel.delete(chapter)
                backupCatalog = backupCatalog - chapter
            }
        }
        changeOrder(mFilter)
    }

    //* Приватные функции *//*
    private suspend fun forSelection(block: suspend RecyclerViewAdapterFactory.RecyclerViewAdapter<Chapter>.(Int) -> Unit) {
        val copySelectedItems = adapter.selectedItems.copyOf()
        copySelectedItems.forEachIndexed { index, b ->
            if (b) {
                adapter.block(index)
            }
        }
    }
}

