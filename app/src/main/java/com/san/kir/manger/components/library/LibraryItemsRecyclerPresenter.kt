package com.san.kir.manger.components.library

import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory

class LibraryItemsRecyclerPresenter(val cat: Category, private val act: LibraryActivity) :
    RecyclerPresenter() {
    private lateinit var adapter: RecyclerViewAdapterFactory.RecyclerPagingAdapter<Manga>

    fun intoIsList(
        recyclerView: RecyclerView, isLarge: Boolean, listener: (PagedList<Manga>?) -> Unit
    ) {
        adapter = RecyclerViewAdapterFactory
            .createPaging({
                              if (isLarge) LibraryLargeItemView(act, cat)
                              else LibrarySmallItemView(act, cat)
                          },
                          { oldItem, newItem -> oldItem.id == newItem.id },
                          { oldItem, newItem -> oldItem == newItem })
        adapter.onListChanged(listener)
        into(recyclerView)
    }

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recycler.adapter = adapter
        act.mViewModel.loadMangas(cat, act.mViewModel.filterFromCategory(cat))
            .observe(act, Observer { list ->
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
}
