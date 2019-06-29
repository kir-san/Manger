package com.san.kir.manger.components.list_chapters

import android.support.v4.view.PagerTabStrip
import com.san.kir.manger.R
import com.san.kir.manger.extending.anko_extend.specialViewPager
import com.san.kir.manger.extending.anko_extend.visibleOrGone
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.include
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class ListChapterBaseView(private val act: ListChaptersActivity) :
    AnkoComponent<ListChaptersActivity> {
    val mAdapter = ListChapterBaseAdapter(act)
    private val titleKey = act.getString(R.string.settings_list_chapter_title_key)
    private val titleDefault = act.getString(R.string.settings_list_chapter_title_default) == "true"
    private val isTitle = act.defaultSharedPreferences.getBoolean(titleKey, titleDefault)

    override fun createView(ui: AnkoContext<ListChaptersActivity>) = with(ui) {
        verticalLayout {
            // ПрогрессБар для отображения поиска новых глав
            horizontalProgressBar {
                isIndeterminate = true
                visibleOrGone(
                    act.mViewModel.isAction,
                    act.mViewModel.isUpdate
                )
            }.lparams(width = matchParent, height = wrapContent)
            specialViewPager {
                if (isTitle) {
                    include<PagerTabStrip>(R.layout.page_tab_strip)
                }

                adapter = mAdapter
            }

        }
    }
}

