package com.san.kir.manger.components.library

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.margin
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.padding
import com.san.kir.ankofork.sdk28.backgroundColor
import com.san.kir.ankofork.sdk28.backgroundResource
import com.san.kir.ankofork.sdk28.imageView
import com.san.kir.ankofork.sdk28.textColor
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.utils.extensions.squareFrameLayout


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

            root = this
        }
    }
}
