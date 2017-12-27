package com.san.kir.manger.components.Library

import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.delete
import com.san.kir.manger.room.models.Manga

// адаптер страниц
class LibraryPageAdapter(private val act: LibraryActivity) : PagerAdapter() {
    private val categoryDao = Main.db.categoryDao
    private val categories by lazy { categoryDao.loadCategories() }
    private var pagers = listOf<LibraryPage>() // список страниц
    var adapters = listOf<LibraryItemsAdapter>() // список адаптеров

    init {
        if (categories.isNotEmpty())
            categories.forEach { cat ->
                // то каждой категории, которая видима создаем страницу в адаптере страниц
                if (cat.isVisible) {
                    val view = LibraryPageView(cat, act) // создаем страницу
                    // адаптер храним отдельно
                    adapters += view.adapter
                    pagers += LibraryPage(name = cat.name, view = view.createView(act))
                    notifyDataSetChanged()
                }
            }
    }

    fun delete(manga: Manga) {
        with(Main.db) {
            chapterDao.delete(*chapterDao.loadChapters(manga.unic).toTypedArray())
            mangaDao.delete(manga)
        }
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
