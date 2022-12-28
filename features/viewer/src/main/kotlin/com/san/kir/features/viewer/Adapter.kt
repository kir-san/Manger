@file:Suppress("DEPRECATION")

package com.san.kir.features.viewer

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.san.kir.features.viewer.utils.Page

class Adapter(fragmentActivity: FragmentActivity) :
    FragmentStatePagerAdapter(fragmentActivity.supportFragmentManager) {

    var items: List<Page> = listOf()

    fun setList(list: List<Page>) {
        if (items.containsAll(list).not()) {
            items = list
            notifyDataSetChanged()
        }
    }

    // Получение нужного элемента взависимости от позиции
    override fun getItem(position: Int): Fragment {
        return when (val page = items[position]) {
            is Page.Current -> PageFragment.newInstance(page)
            Page.Next -> NextFragment()
            Page.NoneNext -> NoneNextFragment()
            Page.NonePrev -> NonePrevFragment()
            Page.Prev -> PrevFragment()
        }
    }

    override fun getCount() = items.size

    override fun saveState(): Parcelable? {
        return null // Помогло избавиться от ошибки
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }
}
