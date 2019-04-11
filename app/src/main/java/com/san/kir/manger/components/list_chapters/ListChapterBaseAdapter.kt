package com.san.kir.manger.components.list_chapters

import com.san.kir.manger.components.library.Page
import com.san.kir.manger.utils.PreparePagerAdapter

class ListChapterBaseAdapter : PreparePagerAdapter() {
    fun init(act: ListChaptersActivity) {
        pagers = pagers + Page(
            "Описание",
            ListChapterAboutView(act).createView(act)
        )
        if (!act.manga.isAlternativeSite) {
            pagers = pagers + Page(
                "Содержимое",
                ListChapterView(act).createView(act)
            )
        }
        notifyDataSetChanged()
    }
}
