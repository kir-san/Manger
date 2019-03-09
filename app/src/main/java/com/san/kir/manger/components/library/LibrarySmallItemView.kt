package com.san.kir.manger.components.library

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import com.san.kir.manger.R
import com.san.kir.manger.extending.anko_extend.roundedImageView
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.utils.ID
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.baselineOf
import org.jetbrains.anko.dip
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.rightOf
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

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
