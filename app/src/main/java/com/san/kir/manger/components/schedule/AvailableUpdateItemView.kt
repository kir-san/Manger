package com.san.kir.manger.components.schedule

import android.view.Gravity
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.margin
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.lines
import com.san.kir.ankofork.sdk28.switch
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.extensions.onCheckedChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AvailableUpdateItemView(private val act: ScheduleActivity) :
    RecyclerViewAdapterFactory.AnkoView<Manga>() {

    private lateinit var manga: TextView
    private lateinit var category: TextView
    private lateinit var switch: Switch

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        linearLayout {
            lparams(width = matchParent, height = dip(60))
            gravity = Gravity.CENTER_VERTICAL

            verticalLayout {
                manga = textView {
                    textSize = 16f
                    lines = 1
                }

                category = textView {
                    textSize = 14f
                }

            }.lparams(width = matchParent, height = wrapContent) {
                weight = 1f
                leftMargin = dip(16)
            }

            switch = switch {
            }.lparams {
                margin = dip(16)
            }
        }
    }

    override fun bind(item: Manga, isSelected: Boolean, position: Int) {
        act.lifecycleScope.launch(Dispatchers.Main) {
            manga.text = item.name
            category.text = category.context.getString(
                R.string.available_update_category_name,
                item.categories
            )
            switch.isChecked = item.isUpdate

            switch.onCheckedChange { _, isChecked ->
                item.isUpdate = isChecked
                act.mViewModel.update(item)
            }
        }
    }
}
