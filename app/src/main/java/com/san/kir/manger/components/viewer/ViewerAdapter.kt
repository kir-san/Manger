package com.san.kir.manger.components.viewer

import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter

class ViewerAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm) {

    var items : List<Fragment> = listOf()

    fun setList(list: List<Page>) {
        items = listOf()
        list.forEachIndexed { position, page ->
            items = items + when (position) {
                0 -> {
                    if (page.link == "prev")
                        ViewerPagerPrevFragment()
                    else
                        ViewerPageNonePrevFragment()
                }
                list.lastIndex -> {
                    if (page.link == "next")
                        ViewerPagerNextFragment()
                    else
                        ViewerPageNoneNextFragment()
                }

                else -> ViewerPageFragment.newInstance(page)
            }
        }

        notifyDataSetChanged()
    }

    // Получение нужного элемента взависимости от позиции
    override fun getItem(position: Int): Fragment {
        return items[position]
    }

    override fun getCount() = items.size

    override fun saveState(): Parcelable? {
        return null // Помогло избавиться от ошибки
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }
}

