package com.san.kir.manger.ui.application_navigation.library.viewer

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter

class ViewerAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

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

/* TODO
These Fragments that were added with fragmentAdapter.addFragment() will be ignored entirely. If we assume similarly to #2 that we can safely communicate with fragments we created at our runtime, these fragments will definitely be unattached, and the app will crash.

Fatal Exception: java.lang.IllegalStateException
Fragment has not been attached yet

In simpler cases, the solution is to instantiate the Fragment directly inside the getItem() call.
* class MyFragmentPagerAdapter(
    private val context: Context,
    fragmentManager: FragmentManager
) : FragmentPagerAdapter(fragmentManager) {
    override fun getCount() = 2

    override fun getItem(position: Int) = when(position) {
        0 -> FirstFragment()
        1 -> SecondFragment()
        else -> throw IllegalStateException("Unexpected position $position")
    }

    override fun getPageTitle(position: Int): CharSequence = when(position) {
        0 -> context.getString(R.string.first)
        1 -> context.getString(R.string.second)
        else -> throw IllegalStateException("Unexpected position $position")
    }
}
* */

