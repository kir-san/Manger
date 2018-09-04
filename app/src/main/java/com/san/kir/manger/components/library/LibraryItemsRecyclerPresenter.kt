package com.san.kir.manger.components.library

import android.arch.lifecycle.Observer
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.dao.MangaFilter
import com.san.kir.manger.room.dao.loadMangas
import com.san.kir.manger.room.dao.removeWithChapters
import com.san.kir.manger.room.dao.toFilter
import com.san.kir.manger.room.dao.updateAsync
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.ItemMove
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.util.*

class LibraryItemsRecyclerPresenter(val cat: Category, private val act: LibraryActivity) :
    RecyclerPresenter() {
    private val mangaDao = Main.db.mangaDao
    private val categories = Main.db.categoryDao
    private lateinit var adapter: RecyclerViewAdapterFactory.DraggableRecyclerViewAdapter<Manga>
    private val itemMove: ItemMove<Manga> = { fromPosition, toPosition ->
        Collections.swap(items, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        items.forEachIndexed { index, manga ->
            manga.order = index
        }
        mangaDao.updateAsync(*items.toTypedArray())
    }

    fun intoIsList(recyclerView: RecyclerView, isLarge: Boolean) {
        adapter = if (isLarge) {
            RecyclerViewAdapterFactory
                .createDraggable(view = { LibraryLargeItemView(act, cat) }, itemMove = itemMove)
        } else {
            RecyclerViewAdapterFactory
                .createDraggable(
                    view = { LibrarySmallItemView(act, cat) }, itemMove = itemMove
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

    private var mFilter = MangaFilter.ADD_TIME_ASC
    private fun changeOrder(filter: MangaFilter) {
        mFilter = filter
        mangaDao.loadMangas(cat, filter).removeObservers(act)
        mangaDao.loadMangas(cat, filter).observe(act, Observer {
            launch {
                it?.let {
                    val old = adapter.items
                    val new = it
                    val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                            old[oldItemPosition].id == new[newItemPosition].id

                        override fun areContentsTheSame(
                            oldItemPosition: Int,
                            newItemPosition: Int
                        ) =
                            old[oldItemPosition] == new[newItemPosition]

                        override fun getOldListSize() = old.size

                        override fun getNewListSize() = new.size
                    })

                    adapter.items = new
                    launch(UI) {
                        result.dispatchUpdatesTo(adapter)
                    }
                }
            }
        })
    }

    val catalog: List<Manga> get() = adapter.items

    val itemCount: Int?
        get() = try {
            adapter.itemCount
        } catch (ex: UninitializedPropertyAccessException) {
            null
        }

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
            mangaDao.updateAsync(manga)
        }
    }

    fun remove(withFiles: Boolean = false) {
        forSelection { i ->
            mangaDao.removeWithChapters(adapter.items[i], withFiles)
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
