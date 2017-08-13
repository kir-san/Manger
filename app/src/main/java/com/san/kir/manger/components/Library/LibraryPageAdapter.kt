package com.san.kir.manger.components.Library

import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.san.kir.manger.dbflow.models.Category

// адаптер страниц
class LibraryPageAdapter(private val fragment: LibraryFragment) : PagerAdapter() {
    val categories = mutableListOf<Category>()
    private var pagers = mutableListOf<LibraryPage>() // список страниц
    val adapters = mutableListOf<LibraryItemsAdapter>() // список адаптеров

    fun addPage(category: Category) {
        val view = LibraryPageView(category, fragment) // создаем страницу
        val page = LibraryPage(name = category.name, view = view.createView(fragment))
        // сохраняем ее в списке
        pagers.add(page)
        // адаптер храним отдельно
        adapters.add(view.adapter)
        categories.add(category)
//            fragment.mView.viewPager.addView(page.view)
        notifyDataSetChanged()
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

    override fun getPageTitle(position: Int): String {
        return pagers[position].name
    }
}
