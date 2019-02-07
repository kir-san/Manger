package com.san.kir.manger.components.library

import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.enums.MangaFilter

class LibraryItemsRecyclerPresenter(val cat: Category, private val act: LibraryActivity) :
    RecyclerPresenter() {
    private lateinit var adapter: RecyclerViewAdapterFactory.RecyclerPagingAdapter<Manga>

    fun intoIsList(recyclerView: RecyclerView, isLarge: Boolean) {
        adapter = RecyclerViewAdapterFactory
            .createPaging({
                              if (isLarge) LibraryLargeItemView(act, cat)
                              else LibrarySmallItemView(act, cat)
                          },
                          { oldItem, newItem -> oldItem.id == newItem.id },
                          { oldItem, newItem -> oldItem == newItem })

        into(recyclerView)
    }

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recycler.adapter = adapter
        act.mViewModel.loadCategory(cat.name)
            .observe(act, Observer { category ->
                category?.let {
                    changeOrder(act.mViewModel.filterFromCategory(it))
                }
            })
    }

    private fun changeOrder(filter: MangaFilter) {
        act.mViewModel.loadMangas(cat, filter).observe(act, Observer { list ->
                adapter.submitList(list)
        })
    }

    val catalog: List<Manga>? get() = adapter.currentList

    val itemCount: Int?
        get() = try {
            adapter.itemCount
        } catch (ex: UninitializedPropertyAccessException) {
            null
        }

    /*fun toggleSelection(position: Int) {
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
            mangaDao.update(manga)
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
    }*/
}
