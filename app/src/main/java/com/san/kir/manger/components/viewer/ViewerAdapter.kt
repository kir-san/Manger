package com.san.kir.manger.components.viewer

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter


class ViewerAdapter(fm: FragmentManager) :
    androidx.fragment.app.FragmentStatePagerAdapter(fm) {

    var items: List<Fragment> = listOf()

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
    override fun getItem(position: Int) = items[position]

    override fun getCount() = items.size

    override fun saveState(): Parcelable? {
        return null // Помогло избавиться от ошибки
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }
}

