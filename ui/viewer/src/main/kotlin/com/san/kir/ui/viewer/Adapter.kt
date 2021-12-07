package com.san.kir.ui.viewer

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.san.kir.core.utils.log

class Adapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private var items = listOf<Fragment>()

    fun updateItems(items: List<Page>) {
        this.items = listOf()

        items.forEachIndexed { index, page ->
            val fr = when (page) {
                is Page.Current -> PageFragment.newInstance(page)
                Page.Next -> NextFragment()
                Page.NoneNext -> NoneNextFragment()
                Page.NonePrev -> NonePrevFragment()
                Page.Prev -> PrevFragment()
            }

            this.items += fr
            notifyItemChanged(index)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun createFragment(position: Int): Fragment {
        return items[position]
    }
}
