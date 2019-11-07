@file:JvmName("DesignListenersListenersKt")
package com.san.kir.ankofork.design

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout


fun TabLayout.onTabSelectedListener(init: __TabLayout_OnTabSelectedListener.() -> Unit) {
    val listener = __TabLayout_OnTabSelectedListener()
    listener.init()
    addOnTabSelectedListener(listener)
}

@Suppress("ClassName")
class __TabLayout_OnTabSelectedListener : TabLayout.OnTabSelectedListener {

    private var _onTabSelected: ((TabLayout.Tab?) -> Unit)? = null

    override fun onTabSelected(tab: TabLayout.Tab?) {
        _onTabSelected?.invoke(tab)
    }

    fun onTabSelected(listener: (TabLayout.Tab?) -> Unit) {
        _onTabSelected = listener
    }

    private var _onTabUnselected: ((TabLayout.Tab?) -> Unit)? = null

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        _onTabUnselected?.invoke(tab)
    }

    fun onTabUnselected(listener: (TabLayout.Tab?) -> Unit) {
        _onTabUnselected = listener
    }

    private var _onTabReselected: ((TabLayout.Tab?) -> Unit)? = null

    override fun onTabReselected(tab: TabLayout.Tab?) {
        _onTabReselected?.invoke(tab)
    }

    fun onTabReselected(listener: (TabLayout.Tab?) -> Unit) {
        _onTabReselected = listener
    }

}

inline fun BottomNavigationView.onNavigationItemSelected(noinline l: (item: android.view.MenuItem?) -> Boolean) {
    setOnNavigationItemSelectedListener(l)
}

