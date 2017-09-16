package com.san.kir.manger.components.Library

import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.dbflow.wrapers.CategoryWrapper
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

// адаптер страниц
class LibraryPageAdapter @Inject constructor(private val fragment: LibraryFragment) : PagerAdapter() {
    private val categories by lazy { CategoryWrapper.getCategories() }
    private var pagers = listOf<LibraryPage>() // список страниц
    var adapters = listOf<LibraryItemsAdapter>() // список адаптеров

    init {
        launch(UI) {
            if (categories.isNotEmpty())
                categories.forEach { cat ->
                    // то каждой категории, которая видима создаем страницу в адаптере страниц
                    if (cat.isVisible) {
                        val view = LibraryPageView(cat, fragment) // создаем страницу
                        // адаптер храним отдельно
                        adapters += view.adapter
                        pagers += LibraryPage(name = cat.name, view = view.createView(fragment))
                        notifyDataSetChanged()
                    }
                }
        }
    }

    fun delete(manga: Manga) {
        manga.delete()
        update()
    }

    fun update() {
        adapters.forEach {
            it.update()
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any? {
        val v = pagers[position].view
        (container as ViewPager).addView(v, position)
        return v
    }

    override fun destroyItem(container: ViewGroup,
                             position: Int,
                             `object`: Any?) = (container as ViewPager).removeView(`object` as View?)

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    override fun getCount() = pagers.size

    override fun getPageTitle(position: Int) = pagers[position].name
}
