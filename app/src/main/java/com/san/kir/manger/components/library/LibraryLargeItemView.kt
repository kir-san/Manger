package com.san.kir.manger.components.library

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.san.kir.manger.R
import com.san.kir.manger.extending.ankoExtend.squareFrameLayout
import com.san.kir.manger.room.models.Category
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class LibraryLargeItemView(
    activity: LibraryActivity,
    cat: Category
) : LibraryItemView(activity, cat) {
    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        squareFrameLayout {
            lparams(width = matchParent, height = wrapContent) {
                margin = dip(2)
            }

            backgroundResource = R.color.colorPrimary

            logo = imageView {
                scaleType = ImageView.ScaleType.FIT_XY
            }.lparams(width = matchParent, height = matchParent) {
                margin = dip(2)
            }

            name = textView {
                backgroundResource = R.color.colorPrimary
                maxLines = 1
                typeface = Typeface.DEFAULT_BOLD
                padding = dip(4)
            }.lparams(width = matchParent, height = wrapContent) {
                gravity = Gravity.BOTTOM
            }

            notReadChapters = textView {
                backgroundResource = R.color.colorPrimary
                padding = dip(4)
            }.lparams(width = wrapContent, height = wrapContent) {
                gravity = Gravity.TOP or Gravity.END
            }

            category = textView {
                backgroundColor = Color.BLACK
                textColor = Color.WHITE
                visibility = View.GONE
            }.lparams(width = wrapContent, height = wrapContent) {
                gravity = Gravity.END
                topMargin = dip(25)
            }

            selected = imageView {
            }.lparams(width = matchParent, height = matchParent)

            isUpdate = progressBar {
                isIndeterminate = true
                visibility = View.GONE
            }.lparams(width = wrapContent, height = wrapContent) {
                gravity = Gravity.START or Gravity.TOP
            }

            root = this
        }
    }
}
