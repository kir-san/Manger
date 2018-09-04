package com.san.kir.manger.components.viewer

import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import java.io.File

class ViewerAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm) {

    private var items: List<File> = listOf()

    fun setList(list: List<File>) {
        this.items = list
        notifyDataSetChanged()
    }

    // Получение нужного элемента взависимости от позиции
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 ->
                if (items.first().name == "prev")
                    ViewerPagerPrevFragment()
                else
                    ViewerPageNonePrevFragment()
            items.lastIndex ->
                if (items.last().name == "next")
                    ViewerPagerNextFragment()
                else
                    ViewerPageNoneNextFragment()

            else -> ViewerPageFragment.newInstance(items[position])
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

