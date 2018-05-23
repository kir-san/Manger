package com.san.kir.manger.components.library

import com.san.kir.manger.components.main.Main
import com.san.kir.manger.utils.PreparePagerAdapter
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

// адаптер страниц
class LibraryPageAdapter(private val act: LibraryActivity) : PreparePagerAdapter() {
    private val categories by lazy { Main.db.categoryDao.loadCategories() }
    var adapters = listOf<LibraryItemsRecyclerPresenter>() // список адаптеров

    val init by lazy {
        launch(UI) {
            adapters = listOf()
            pagers = listOf()
            if (categories.isNotEmpty()) {
                val prepare = async {
                    categories
                        .filter { it.isVisible }
                        .map { cat ->
                            val adapter = LibraryItemsRecyclerPresenter(cat, act)
                            val view = LibraryPageView(adapter, cat, act)
                            val page = Page(cat.name, view.createView(act))
                            adapter to page
                        }.toMap()
                }

                adapters += prepare.await().keys.toList()
                pagers += prepare.await().values.toList()

                notifyDataSetChanged()
            }
        }
    }
}
