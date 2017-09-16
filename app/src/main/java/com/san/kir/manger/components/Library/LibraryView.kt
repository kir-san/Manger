package com.san.kir.manger.components.Library

import android.support.v4.view.PagerTabStrip
import android.support.v4.view.ViewPager
import android.view.View
import com.san.kir.manger.R
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.include
import org.jetbrains.anko.support.v4.viewPager
import javax.inject.Inject

class LibraryView @Inject constructor(val _adapter: LibraryPageAdapter) : AnkoComponent<LibraryFragment> {

    lateinit var viewPager: ViewPager

    fun createView(parent: LibraryFragment): View {
        return createView(AnkoContext.create(parent.context, parent))
    }

    override fun createView(ui: AnkoContext<LibraryFragment>) = with(ui) {
        viewPager {
            include<PagerTabStrip>(R.layout.page_tab_strip)
            adapter = _adapter
            viewPager = this
        }
    }
}
