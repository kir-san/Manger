package com.san.kir.manger.components.library

import com.san.kir.manger.components.main.Main
import com.san.kir.manger.utils.PreparePagerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// адаптер страниц
class LibraryPageAdapter(private val act: LibraryActivity) : PreparePagerAdapter() {
    private val categories by lazy { Main.db.categoryDao.loadCategories() }
    var adapters = listOf<LibraryItemsRecyclerPresenter>() // список адаптеров

    val init by lazy {
        GlobalScope.launch(Dispatchers.Main) {
            adapters = listOf()
            pagers = listOf()
            if (categories.isNotEmpty()) {
                val prepare = withContext(Dispatchers.Default) {
                    categories
                        .filter { it.isVisible }
                        .map { cat ->
                            val adapter = LibraryItemsRecyclerPresenter(cat, act)
                            val view = LibraryPageView(adapter, cat, act)
                            val page = Page(cat.name, view.createView(act))
                            adapter to page
                        }.toMap()
                }

                adapters += prepare.keys.toList()
                pagers += prepare.values.toList()

                notifyDataSetChanged()
            }
        }
    }
}
