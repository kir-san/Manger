package com.san.kir.manger.components.library

import androidx.lifecycle.lifecycleScope
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.utils.PreparePagerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// адаптер страниц
class LibraryPageAdapter(act: LibraryActivity) : PreparePagerAdapter() {
    private lateinit var categories: List<Category>
    var adapters = listOf<LibraryItemsRecyclerPresenter>() // список адаптеров

    val init by lazy {
        act.lifecycleScope.launch(Dispatchers.Default) {
            adapters = listOf()
            pagers = listOf()
            categories = act.mViewModel.getCategoryItems()

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
                    adapters = adapters + prepare.keys.toList()
                    pagers = pagers + prepare.values.toList()

                    notifyDataSetChanged()
                }
            }
        }
    }
}
