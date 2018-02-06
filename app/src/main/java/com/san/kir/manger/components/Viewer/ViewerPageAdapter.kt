package com.san.kir.manger.components.Viewer

import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import java.io.File

class ViewerPageAdapter(fm: FragmentManager, private var list: List<File> = listOf()) :
        FragmentStatePagerAdapter(fm) {

    fun setList(list: List<File>) {
        this.list = list
        notifyDataSetChanged()
    }

    // Получение нужного элемента взависимости от позиции
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> // Если позиция равна нуля
                // Если есть предыдущая глава, то одно, иначе другое
                if (list[position].name == "prev") ViewerPagerPrevFragment()
                else ViewerPageNonePrevFragment()

            list.size - 1 -> // Если позиция последняя
                // Если есть следущая глава, то одно, иначе другое
                if (list[position].name == "next") ViewerPagerNextFragment()
                else ViewerPageNoneNextFragment()
        // Если нет ни первого, ни второго
            else -> ViewerPageFragment.newInitstate(list[position])
        }

    }

    override fun getCount(): Int {
        return list.size
    }

    override fun saveState(): Parcelable? {
        return null // Помогло избавиться от ошибки
    }
}
