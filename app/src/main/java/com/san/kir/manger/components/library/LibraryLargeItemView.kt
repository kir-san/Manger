package com.san.kir.manger.components.library

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.BOTTOM
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.END
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.START
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.TOP
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.constraint_layout.applyConstraintSet
import com.san.kir.ankofork.constraint_layout.constraintLayout
import com.san.kir.ankofork.constraint_layout.matchConstraint
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.margin
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.padding
import com.san.kir.ankofork.sdk28.backgroundColor
import com.san.kir.ankofork.sdk28.backgroundResource
import com.san.kir.ankofork.sdk28.textColor
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.textColorResource
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.utils.extensions.squareImageView


class LibraryLargeItemView(
    activity: LibraryActivity,
    cat: Category
) : LibraryItemView(activity, cat) {
    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        constraintLayout {
            lparams(width = matchParent, height = wrapContent) {
                margin = dip(2)
            }

            backgroundResource = R.color.colorPrimary

            logo = squareImageView {
                id = View.generateViewId()
                scaleType = ImageView.ScaleType.FIT_XY
            }.lparams(width = matchConstraint, height = matchParent)

            name = textView {
                id = View.generateViewId()
                backgroundResource = R.color.inverseTextColor
                textColorResource = R.color.iconColor
                maxLines = 1
                padding = dip(3)
            }.lparams(width = matchConstraint, height = wrapContent)

            notReadChapters = textView {
                id = View.generateViewId()
                backgroundResource = R.color.inverseTextColor
                textColorResource = R.color.iconColor
                padding = dip(4)
            }.lparams(width = wrapContent, height = wrapContent)

            category = textView {
                id = View.generateViewId()
                backgroundColor = Color.BLACK
                textColor = Color.WHITE
                visibility = View.GONE
            }.lparams(width = wrapContent, height = wrapContent)

            applyConstraintSet {
                logo {
                    connect(
                        START to START of PARENT_ID margin dip(4),
                        TOP to TOP of PARENT_ID margin dip(4),
                        END to END of PARENT_ID margin dip(4)

                    )
                }
                name {
                    connect(
                        TOP to BOTTOM of logo,
                        START to START of PARENT_ID margin dip(4),
                        END to END of PARENT_ID margin dip(4),
                        BOTTOM to BOTTOM of PARENT_ID margin dip(4)
                    )
                }
                notReadChapters {
                    connect(
                        TOP to TOP of PARENT_ID margin dip(4),
                        END to END of PARENT_ID margin dip(4)
                    )
                }
                category {
                    connect(
                        TOP to BOTTOM of notReadChapters,
                        END to END of PARENT_ID margin dip(4)
                    )
                }
            }

            root = this
        }
    }
}
