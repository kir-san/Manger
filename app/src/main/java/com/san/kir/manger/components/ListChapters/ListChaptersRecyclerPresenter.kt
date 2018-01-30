package com.san.kir.manger.components.ListChapters

import android.support.v7.widget.RecyclerView
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import com.san.kir.manger.R
import com.san.kir.manger.components.DownloadManager.DownloadService
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.ChapterFilter
import com.san.kir.manger.room.DAO.delete
import com.san.kir.manger.room.DAO.update
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.action
import com.san.kir.manger.room.models.toDownloadItem
import com.san.kir.manger.utils.CHAPTER_STATUS
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.delChapters
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.startService
import org.jetbrains.anko.toast


class ListChaptersRecyclerPresenter(val injector: KodeinInjector) : RecyclerPresenter() {
    private val act: ListChaptersActivity by injector.instance()
    private val dao = Main.db.chapterDao
    private var adapter = RecyclerViewAdapterFactory.createSimple { ListChaptersItemView(injector) }
    private var manga: Manga = Manga()
    private var backupCatalog = listOf<Chapter>()

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recycler.adapter = adapter
    }

    fun setManga(manga: Manga = this.manga, filter: ChapterFilter) = async {
        this@ListChaptersRecyclerPresenter.manga = manga
        adapter.items = async {
            dao.loadChapters(manga.unic)
        }.await()
        backupCatalog = adapter.items

        changeOrder(filter)
    }

    private var filter = ChapterFilter.NOT_READ_DESC
    fun changeOrder(filter: ChapterFilter) = async(UI) {
        this@ListChaptersRecyclerPresenter.filter = filter
        adapter.items = when (filter) {
            ChapterFilter.ALL_READ_ASC -> backupCatalog
            ChapterFilter.NOT_READ_ASC -> backupCatalog.filter { !it.isRead }
            ChapterFilter.IS_READ_ASC -> backupCatalog.filter { it.isRead }
            ChapterFilter.ALL_READ_DESC -> backupCatalog.reversed()
            ChapterFilter.NOT_READ_DESC -> backupCatalog.filter { !it.isRead }.reversed()
            ChapterFilter.IS_READ_DESC -> backupCatalog.filter { it.isRead }.reversed()
        }
        adapter.notifyDataSetChanged()
    }

    fun toggleSelection(position: Int) {
        adapter.selectedItems[position] = !adapter.selectedItems[position]
        adapter.notifyItemChanged(position)
    }

    val selectedCount get() = adapter.selectedItems.filter { it }.size

    fun deleteSelectedItems() = async(UI) {
        var count = 0
        forSelection { i ->
            val chapter = items[i]
            if (chapter.action == CHAPTER_STATUS.DELETE) {
                delChapters(chapter)
                count++
            }
        }
        if (count == 0) {
            act.toast(R.string.list_chapters_selection_del_error)
        } else {
            act.toast(R.string.list_chapters_selection_del_ok)
        }
    }

    fun downloadSelectedItems() = async(UI) {
        forSelection { i ->
            val chapter = items[i]
            // для каждого выделенный элемент
            if (chapter.action == CHAPTER_STATUS.DOWNLOADABLE) {
                val item = chapter.toDownloadItem()
                act.startService<DownloadService>("item" to item)
            }
        }
    }

    fun downloadNextNotReadChapter() = async(UI) {
        val job = async {
            val chapter = dao.loadChaptersNotReadAsc(manga.unic)
                .first { it.action == CHAPTER_STATUS.DOWNLOADABLE }
            val item = DownloadItem(
                name = chapter.manga + " " + chapter.name,
                link = chapter.site,
                path = chapter.path
            )
            act.startService<DownloadService>("item" to item)
        }

        job.join()

        changeOrder(filter)
    }

    fun downloadAllNotReadChapters() = async(UI) {
        val job = async {
            dao.loadChaptersNotReadAsc(manga.unic)
                .filter { it.action == CHAPTER_STATUS.DOWNLOADABLE }
                .onEach { chapter ->
                    val item = DownloadItem(
                        name = chapter.manga + " " + chapter.name,
                        link = chapter.site,
                        path = chapter.path
                    )
                    act.startService<DownloadService>("item" to item)
                }
                .size
        }

        val count = job.await()

        if (count == 0)
            act.toast(R.string.list_chapters_selection_load_error)
        else
            act.toast(act.getString(R.string.list_chapters_selection_load_ok, count))

        changeOrder(filter)
    }

    fun downloadAllChapters() = async(UI) {
        val job = async {
            dao.loadChaptersAllAsc(manga.unic)
                .filter { it.action == CHAPTER_STATUS.DOWNLOADABLE }
                .onEach { chapter ->
                    val item = DownloadItem(
                        name = chapter.manga + " " + chapter.name,
                        link = chapter.site,
                        path = chapter.path
                    )
                    act.startService<DownloadService>("item" to item)
                }
                .size
        }

        val count = job.await()
        if (count == 0)
            act.toast(R.string.list_chapters_selection_load_error)
        else
            act.toast(act.getString(R.string.list_chapters_selection_load_ok, count))
        changeOrder(filter)
    }

    fun setRead(isReading: Boolean) = async(UI) {
        forSelection { i ->
            // Для всех выделеных элементов
            items[i].let { chapter ->
                chapter.isRead = isReading
                dao.update(chapter)
            }
        }
        changeOrder(filter)
    }

    fun removeSelection() = async(UI) {
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

    fun fullDeleteSelectedItems() = async(UI) {
        forSelection { i ->
            items[i].let { chapter ->
                dao.delete(chapter)
            }
        }
        changeOrder(filter)
    }

    //* Приватные функции *//*
    private fun forSelection(block: RecyclerViewAdapterFactory.RecyclerViewAdapter<Chapter>.(Int) -> Unit) {
        val copySelectedItems = adapter.selectedItems.copyOf()
        copySelectedItems.forEachIndexed { index, b ->
            if (b) {
                adapter.block(index)
                adapter.notifyItemChanged(index)
            }
        }
    }


}
