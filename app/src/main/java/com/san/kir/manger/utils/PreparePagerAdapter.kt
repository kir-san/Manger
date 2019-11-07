package com.san.kir.manger.utils

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.san.kir.manger.components.library.Page

abstract class PreparePagerAdapter: androidx.viewpager.widget.PagerAdapter() {
    var pagers = listOf<Page>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val v = pagers[position].view
        (container as ViewPager).addView(v, position)
        return v
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) =
        (container as ViewPager).removeView(`object` as View)

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    override fun getCount() = pagers.size

    override fun getPageTitle(position: Int) = pagers[position].name
}
