package com.san.kir.manger.components.schedule

import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.dao.updateAsync
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.alignParentStart
import org.jetbrains.anko.below
import org.jetbrains.anko.centerVertically
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalMargin
import org.jetbrains.anko.margin
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.startOf
import org.jetbrains.anko.switch
import org.jetbrains.anko.textView

class AvailableUpdateItemView : RecyclerViewAdapterFactory.AnkoView<Manga>() {
    private object Id {
        val switch = ID.generate()
        val manga = ID.generate()
    }

    private lateinit var manga: TextView
    private lateinit var category: TextView
    private lateinit var switch: Switch

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams {
                margin = dip(3)
                horizontalMargin = dip(5)
            }

            manga = textView {
                id = Id.manga
                textSize = 16f
            }.lparams {
                alignParentStart()
                startOf(Id.switch)
            }

            category = textView {
                textSize = 13f
            }.lparams {
                alignParentStart()
                below(Id.manga)
                startOf(Id.switch)
            }

            switch = switch {
                id = Id.switch
            }.lparams {
                alignParentEnd()
                centerVertically()
            }
        }
    }

    override fun bind(item: Manga, isSelected: Boolean, position: Int) {
        launch(UI) {
            manga.text = item.name
            category.text = category.context.getString(
                R.string.available_update_category_name,
                item.categories
            )
            switch.isChecked = item.isUpdate

            switch.onCheckedChange { _, isChecked ->
                item.isUpdate = isChecked
                Main.db.mangaDao.updateAsync(item)
            }
        }
    }
}
