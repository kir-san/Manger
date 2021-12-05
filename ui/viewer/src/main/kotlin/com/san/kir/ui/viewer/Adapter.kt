package com.san.kir.ui.viewer

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class Adapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private var items = listOf<Page>()

    override fun getItemCount(): Int {
        return items.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (val item = items[position]) {
            is Page.Current -> PageFragment.newInstance(item)
            Page.Next -> NextFragment()
            Page.NoneNext -> NoneNextFragment()
            Page.NonePrev -> NonePrevFragment()
            Page.Prev -> PrevFragment()
        }
    }
}

