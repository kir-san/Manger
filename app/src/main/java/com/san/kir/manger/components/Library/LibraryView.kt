package com.san.kir.manger.components.Library

import android.support.v4.view.PagerTabStrip
import android.support.v4.view.ViewPager
import com.san.kir.manger.R
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.include
import org.jetbrains.anko.support.v4.viewPager

class LibraryView(val adapter: LibraryPageAdapter) : AnkoComponent<LibraryFragment> {

    lateinit var viewPager: ViewPager

    override fun createView(ui: AnkoContext<LibraryFragment>) = with(ui) {
        viewPager {
            include<PagerTabStrip>(R.layout.page_tab_strip)
            adapter = this@LibraryView.adapter

            viewPager = this
        }
    }
}
