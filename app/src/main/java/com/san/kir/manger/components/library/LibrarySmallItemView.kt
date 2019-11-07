package com.san.kir.manger.components.library

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.alignParentBottom
import com.san.kir.ankofork.alignParentEnd
import com.san.kir.ankofork.baselineOf
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.margin
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.padding
import com.san.kir.ankofork.rightOf
import com.san.kir.ankofork.sdk28.backgroundColor
import com.san.kir.ankofork.sdk28.backgroundResource
import com.san.kir.ankofork.sdk28.relativeLayout
import com.san.kir.ankofork.sdk28.textColor
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.extensions.roundedImageView

class LibrarySmallItemView(
    activity: LibraryActivity,
    cat: Category
) : LibraryItemView(activity, cat) {
    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = dip(75)) {
                margin = dip(2)
            }

            backgroundResource = R.color.colorPrimary

            logo = roundedImageView {
                id = ID.generate()
            }.lparams(width = dip(73), height = dip(73)) {
                margin = dip(2)
            }

            name = textView {
                id = ID.generate()
                maxLines = 1
                textSize = 19f
                typeface = Typeface.DEFAULT_BOLD
                padding = dip(4)
            }.lparams(width = matchParent) {
                rightOf(logo.id)
                topMargin = dip(5)
            }

            notReadChapters = textView {
                padding = dip(4)
                textSize = 18f
            }.lparams(width = wrapContent, height = wrapContent) {
                baselineOf(name)
                alignParentEnd()
            }

            category = textView {
                backgroundColor = Color.BLACK
                textColor = Color.WHITE
                visibility = View.INVISIBLE
                padding = dip(4)
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentEnd()
                alignParentBottom()
            }


            root = this
        }
    }
}
