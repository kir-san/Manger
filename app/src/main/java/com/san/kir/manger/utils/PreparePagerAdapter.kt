package com.san.kir.manger.utils

import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.san.kir.manger.components.library.Page

abstract class PreparePagerAdapter: PagerAdapter() {
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
