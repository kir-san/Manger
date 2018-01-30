package com.san.kir.manger.components.Library

import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import com.san.kir.manger.components.Main.Main

// адаптер страниц
class LibraryPageAdapter(private val injector: KodeinInjector) : PagerAdapter() {
    private val categories by lazy { Main.db.categoryDao.loadCategories() }
    private var pagers = listOf<LibraryPage>() // список страниц
    var adapters = listOf<LibraryItemsRecyclerPresenter>() // список адаптеров
    private val act: LibraryActivity by injector.instance()

    init {
        if (categories.isNotEmpty())
            categories.forEach { cat ->
                if (cat.isVisible) {
                    val view = LibraryPageView(cat, injector) // создаем страницу
                    // адаптер храним отдельно
                    adapters += view.adapter
                    pagers += LibraryPage(name = cat.name, view = view.createView(act))
                    notifyDataSetChanged()
                }
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
