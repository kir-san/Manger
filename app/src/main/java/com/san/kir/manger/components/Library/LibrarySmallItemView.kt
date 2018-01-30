package com.san.kir.manger.components.Library

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.github.salomonbrys.kodein.KodeinInjector
import com.san.kir.manger.R
import com.san.kir.manger.room.models.Category
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.imageView
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class LibrarySmallItemView(
    injector: KodeinInjector,
    cat: Category
) : LibraryItemView(injector, cat) {
    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        frameLayout {
            lparams(width = matchParent, height = dip(80)) {
                margin = dip(2)
            }

            val colorBackground = Color.argb(210, 63, 81, 181)

            backgroundResource = R.color.colorPrimary

            logo = imageView {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }.lparams(width = matchParent, height = matchParent) {
                margin = dip(2)
            }

            verticalLayout {
                name = textView {
                    backgroundColor = colorBackground
                    maxLines = 1
                    typeface = Typeface.DEFAULT_BOLD
                    padding = dip(4)
                }.lparams(width = matchParent, height = wrapContent)

                category = textView {
                    backgroundColor = Color.BLACK
                    textColor = Color.WHITE
                    visibility = View.INVISIBLE
                    padding = dip(4)
                }.lparams(width = wrapContent, height = wrapContent)

                notReadChapters = textView {
                    backgroundColor = colorBackground
                    padding = dip(4)
                }.lparams(width = wrapContent, height = wrapContent)
            }

            selected = imageView {
            }.lparams(width = matchParent, height = matchParent)

            isUpdate = progressBar {
                isIndeterminate = true
                visibility = View.GONE
            }.lparams(width = wrapContent, height = wrapContent) {
                gravity = Gravity.END or Gravity.BOTTOM
            }

            root = this
        }
    }
}
