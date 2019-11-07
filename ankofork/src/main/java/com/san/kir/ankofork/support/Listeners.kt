package com.san.kir.ankofork.support

import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentTabHost
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager


fun ViewPager.onPageChangeListener(init: __ViewPager_OnPageChangeListener.() -> Unit) {
    val listener = __ViewPager_OnPageChangeListener()
    listener.init()
    addOnPageChangeListener(listener)
}

@Suppress("ClassName", "unused")
class __ViewPager_OnPageChangeListener : ViewPager.OnPageChangeListener {
    private var _onPageScrolled: ((Int, Float, Int) -> Unit)? = null
    private var _onPageSelected: ((Int) -> Unit)? = null
    private var _onPageScrollStateChanged: ((Int) -> Unit)? = null

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        _onPageScrolled?.invoke(position, positionOffset, positionOffsetPixels)
    }

    fun onPageScrolled(listener: (Int, Float, Int) -> Unit) {
        _onPageScrolled = listener
    }

    override fun onPageSelected(position: Int) {
        _onPageSelected?.invoke(position)
    }

    fun onPageSelected(listener: (Int) -> Unit) {
        _onPageSelected = listener
    }

    override fun onPageScrollStateChanged(state: Int) {
        _onPageScrollStateChanged?.invoke(state)
    }

    fun onPageScrollStateChanged(listener: (Int) -> Unit) {
        _onPageScrollStateChanged = listener
    }

}

@Suppress("unused")
inline fun FragmentTabHost.onTabChanged(noinline l: (tabId: String?) -> Unit) {
    setOnTabChangedListener(l)
}

@Suppress("unused")
inline fun NestedScrollView.onScrollChange(noinline l: (v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) -> Unit) {
    setOnScrollChangeListener(l)
}

inline fun SwipeRefreshLayout.onRefresh(noinline l: () -> Unit) {
    setOnRefreshListener(l)
}

