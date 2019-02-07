package com.san.kir.manger.components.list_chapters

import android.support.v7.widget.RecyclerView
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.eventBus.negative
import com.san.kir.manger.eventBus.positive
import com.san.kir.manger.extending.launchCtx
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.action
import com.san.kir.manger.room.models.toDownloadItem
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.delChapters
import com.san.kir.manger.utils.enums.ChapterFilter
import com.san.kir.manger.utils.enums.ChapterStatus
import com.san.kir.manger.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import java.util.regex.Pattern


class ListChaptersRecyclerPresenter(val act: ListChaptersActivity) : RecyclerPresenter() {
    private var adapter = RecyclerViewAdapterFactory.createSimple { ListChaptersItemView(act) }
    private var mManga: Manga = Manga()
    private var mFilter = ChapterFilter.NOT_READ_DESC
    private var backupCatalog = listOf<Chapter>()

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recycler.adapter = adapter
    }

    fun setManga(manga: Manga = mManga, filter: ChapterFilter): Job {
        mManga = manga
        mFilter = filter
        return changeSort(manga.isAlternativeSort)
    }

    fun update() {
        changeSort(mManga.isAlternativeSort)
    }

    fun changeSort(alternative: Boolean) = act.launchCtx {
        try {
            val loadChapters = act.mViewModel.getChapters(mManga)
            log("loadChapters = ${loadChapters.size}")
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

    fun changeOrder(filter: ChapterFilter) = act.launchCtx {
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

    fun deleteSelectedItems() = act.launchCtx {
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

    fun downloadSelectedItems() = act.launchCtx {
        forSelection { i ->
            val chapter = items[i]
            // для каждого выделенный элемент
            if (chapter.action == ChapterStatus.DOWNLOADABLE) {
                act.downloadManager.addOrStart(chapter.toDownloadItem())
            }
        }
    }

    fun downloadNextNotReadChapter() = act.launchCtx {
        val chapter = act
            .mViewModel
            .getChaptersNotReadAsc(mManga)
            .first { it.action == ChapterStatus.DOWNLOADABLE }

        act.downloadManager.addOrStart(chapter.toDownloadItem())

        changeOrder(mFilter)
    }

    fun downloadAllNotReadChapters() = act.launchCtx {
        val count = act
            .mViewModel
            .getChaptersNotReadAsc(mManga)
            .filter { it.action == ChapterStatus.DOWNLOADABLE }
            .onEach { chapter ->
                act.downloadManager.addOrStart(chapter.toDownloadItem())
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

    fun downloadAllChapters() = act.launchCtx {
        val count = act
            .mViewModel
            .getChaptersAsc(mManga)
            .filter { it.action == ChapterStatus.DOWNLOADABLE }
            .onEach { chapter ->
                act.downloadManager.addOrStart(chapter.toDownloadItem())
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

    fun setRead(isReading: Boolean) = act.launchCtx {
        forSelection { i ->
            // Для всех выделеных элементов
            items[i].let { chapter ->
                chapter.isRead = isReading
                act.mViewModel.updateChapter(chapter)
            }
        }
        changeOrder(mFilter)
    }

    fun removeSelection() = act.launch(Dispatchers.Main) {
        repeat(adapter.itemCount) { i ->
            adapter.selectedItems[i] = false
            adapter.notifyItemChanged(i)
        }
    }

    fun selectAll() =
        repeat(adapter.itemCount) { i ->
            adapter.selectedItems[i] = true
            adapter.notifyItemChanged(i)
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
        act.view.isAction.positive()
        act.launchCtx {
            forSelection { i ->
                items[i].pages = ManageSites.pages(items[i])
                act.mViewModel.updateChapter(items[i])
            }
        }.invokeOnCompletion {
            act.view.isAction.negative()
        }
    }

    fun fullDeleteSelectedItems() = act.launchCtx {
        forSelection { i ->
            items[i].let { chapter ->
                act.mViewModel.deleteChapter(chapter)
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

class ChapterComparator : Comparator<Chapter> {
    override fun compare(o1: Chapter, o2: Chapter): Int {
        val reg = Pattern.compile("\\d+")
        val matcher1 = reg.matcher(o1.name)
        val matcher2 = reg.matcher(o2.name)

        var numbers1 = listOf<String>()
        var numbers2 = listOf<String>()

        while (matcher1.find()) {
            numbers1 = numbers1 + matcher1.group()
        }

        while (matcher2.find()) {
            numbers2 = numbers2 + matcher2.group()
        }

        val prepareNumber1 = when (numbers1.size) {
            2 -> numbers1[1].toInt(10)
            1 -> numbers1[0].toInt(10)
            else -> 0
        }

        val prepareNumber2 = when (numbers2.size) {
            2 -> numbers2[1].toInt(10)
            1 -> numbers2[0].toInt(10)
            else -> 0
        }

        val prepare1 = String.format("%04d", prepareNumber1)
        val prepare2 = String.format("%04d", prepareNumber2)

        val finishNumber1 = "${numbers1.first()}$prepare1".toInt(10)
        val finishNumber2 = "${numbers2.first()}$prepare2".toInt(10)

        return finishNumber1 - finishNumber2
    }

}
