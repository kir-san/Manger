package com.san.kir.manger.components.Storage

import android.R
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.Extending.AnkoExtend.bind
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.imageView
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class StorageParentView : AnkoComponent<ViewGroup> {
    private val item: Binder<StorageParentItem?> = Binder(null)
    val expanded = Binder(0f)

    fun createView(parent: ViewGroup): View {
        return createView(AnkoContext.create(parent.context, parent))
    }

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        frameLayout {
            lparams(width = matchParent, height = wrapContent) {
                margin = dip(1)
            }
            backgroundColor = Color.parseColor("#A19F9F")

            // Название
            textView {
                padding = dip(10)
                setTextAppearance(R.attr.textAppearanceLarge)
                setTypeface(typeface, Typeface.BOLD)

                bind(item) { text = it?.name ?: "" }
            }.lparams(width = wrapContent, height = wrapContent) {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
            }

            // Иконка
            imageView {
                backgroundResource = R.drawable.arrow_down_float

                bind(expanded) { rotation = it }
            }.lparams(width = wrapContent, height = wrapContent) {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                margin = dip(8)
            }

        }
    }

    fun bind(item: StorageParentItem) {
        this.item.item = item
    }
}
