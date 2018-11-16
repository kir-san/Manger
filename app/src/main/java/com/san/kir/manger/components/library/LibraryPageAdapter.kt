package com.san.kir.manger.components.library

import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.utils.PreparePagerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// адаптер страниц
class LibraryPageAdapter(private val act: LibraryActivity) : PreparePagerAdapter() {
    private lateinit var categories: List<Category>
    var adapters = listOf<LibraryItemsRecyclerPresenter>() // список адаптеров

    val init by lazy {
        act.launch(act.coroutineContext) {
            adapters = listOf()
            pagers = listOf()
            categories = Main.db.categoryDao.getItems()

            if (categories.isNotEmpty()) {
                val prepare = categories
                    .filter { it.isVisible }
                    .map { cat ->
                        val adapter = LibraryItemsRecyclerPresenter(cat, act)
                        val view = LibraryPageView(adapter, cat, act)
                        val page = Page(cat.name, view.createView(act))
                        adapter to page
                    }.toMap()


                withContext(Dispatchers.Main) {
                    adapters += prepare.keys.toList()
                    pagers += prepare.values.toList()

                    notifyDataSetChanged()
                }
            }
        }
    }
}
