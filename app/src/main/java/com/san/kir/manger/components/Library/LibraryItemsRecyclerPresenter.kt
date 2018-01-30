package com.san.kir.manger.components.Library

import android.arch.lifecycle.Observer
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.MangaFilter
import com.san.kir.manger.room.DAO.loadMangas
import com.san.kir.manger.room.DAO.removeWithChapters
import com.san.kir.manger.room.DAO.toFilter
import com.san.kir.manger.room.DAO.update
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.ItemMove
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.SimpleItemTouchHelperCallback
import java.util.*

class LibraryItemsRecyclerPresenter(val cat: Category, private val injector: KodeinInjector) :
    RecyclerPresenter() {
    private val act: LibraryActivity by injector.instance()
    private val mangas = Main.db.mangaDao
    private val categories = Main.db.categoryDao
    private lateinit var adapter: RecyclerViewAdapterFactory.DragableRecyclerViewAdapter<Manga>
    private val itemMove: ItemMove<Manga> = { fromPosition, toPosition ->
        Collections.swap(items, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        items.forEachIndexed { index, manga ->
            manga.order = index
        }
        mangas.update(*items.toTypedArray())
    }
    private lateinit var mItemTouchHelper: ItemTouchHelper

    fun intoIsList(recyclerView: RecyclerView, isLarge: Boolean) {
        adapter = if (isLarge) {
            RecyclerViewAdapterFactory
                .createDragable(view = { LibraryLargeItemView(injector, cat) }, itemMove = itemMove)
        } else {
            RecyclerViewAdapterFactory
                .createDragable(
                    view = { LibrarySmallItemView(injector, cat) }, itemMove = itemMove
                )
        }
        into(recyclerView)
    }

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recycler.adapter = adapter
        categories.loadLiveCategory(cat.name)
            .observe(act, Observer {
                it?.let {
                    changeOrder(it.toFilter())
                }
            })
    }

    fun customizeOn() {
        mItemTouchHelper = ItemTouchHelper(SimpleItemTouchHelperCallback(adapter))
        mItemTouchHelper.attachToRecyclerView(recycler)
        mangas.loadMangas(cat, mFilter).removeObservers(act)
    }

    fun customizeOff() {
        mItemTouchHelper.attachToRecyclerView(null)
        changeOrder(mFilter)
    }

    private var mFilter = MangaFilter.ADD_TIME_ASC
    private fun changeOrder(filter: MangaFilter) {
        mFilter = filter
        mangas.loadMangas(cat, filter).removeObservers(act)
        mangas.loadMangas(cat, filter).observe(act, Observer {
            it?.let {
                val old = adapter.items
                val new = it
                val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                        old[oldItemPosition].id == new[newItemPosition].id

                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                        old[oldItemPosition] == new[newItemPosition]

                    override fun getOldListSize() = old.size

                    override fun getNewListSize() = new.size
                })

                adapter.items = new
                result.dispatchUpdatesTo(adapter)
            }
        })
    }

    val catalog: List<Manga> get() = adapter.items

    val itemCount: Int
        get() = adapter.itemCount

    fun toggleSelection(position: Int) {
        // Переключение выделения
        adapter.selectedItems[position] = !adapter.selectedItems[position]
        adapter.notifyItemChanged(position)
    }

    fun selectAll() =
        repeat(adapter.itemCount) { i ->
            adapter.selectedItems[i] = true
            adapter.notifyItemChanged(i)
        }


    fun removeSelection() =
        repeat(adapter.itemCount) { i ->
            adapter.selectedItems[i] = false
            adapter.notifyItemChanged(i)
        }

    val selectedCount get() = adapter.selectedItems.filter { it }.size

    fun moveToCategory(newCategory: String) {
        forSelection { i ->
            val manga = adapter.items[i]
            manga.categories = newCategory
            mangas.update(manga)
        }
    }

    fun remove(withFiles: Boolean = false) {
        forSelection { i ->
            mangas.removeWithChapters(adapter.items[i], withFiles)
        }
    }

    private fun forSelection(block: (Int) -> Unit) {
        val copySelectedItems = adapter.selectedItems.copyOf()
        copySelectedItems.forEachIndexed { index, b ->
            if (b) {
                block(index)
            }
        }
    }
}
