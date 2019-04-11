package com.san.kir.manger.components.list_chapters

import android.graphics.Color
import android.support.constraint.ConstraintSet.PARENT_ID
import android.widget.ImageView
import android.widget.TextView
import com.san.kir.manger.components.viewer.ViewerActivity
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.extending.anko_extend.onClick
import com.san.kir.manger.extending.anko_extend.visibleOrGone
import com.san.kir.manger.extending.dialogs.DeleteReadChaptersDialog
import com.san.kir.manger.extending.getOrientation
import com.san.kir.manger.extending.launchUI
import com.san.kir.manger.utils.AnkoActivityComponent
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.loadImage
import com.san.kir.manger.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.applyRecursively
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.button
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.BOTTOM
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.END
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.START
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.TOP
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class ListChapterAboutView(private val act: ListChaptersActivity) : AnkoActivityComponent() {
    override fun createView(ui: AnkoContext<BaseActivity>) = with(ui) {
        constraintLayout {
            val logo = imageView {
                id = ID.generate()
                loadImage(act.manga.logo).into(this)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }.lparams(width = matchConstraint, height = matchConstraint)

            val continueBtn = button("Продолжить чтение") {
                id = ID.generate()
                onClick {
                    act.startActivity<ViewerActivity>(
                        "chapter" to withContext(Dispatchers.IO) {
                            act.mViewModel.getFirstNotReadChapter(
                                act.manga
                            )
                        },
                        "is" to act.manga.isAlternativeSort
                    )
                }
            }.lparams(width = matchConstraint, height = dip(80))

            val startBtn = button("Читать с начала") {
                id = ID.generate()
                onClick {
                    act.startActivity<ViewerActivity>(
                        "chapter" to withContext(Dispatchers.IO) {
                            act.mViewModel.getFirstChapter(act.manga)
                        },
                        "is" to act.manga.isAlternativeSort
                    )
                }
            }.lparams(width = matchConstraint, height = wrapContent)

            val info = verticalLayout {
                id = ID.generate()
                backgroundColor = Color.argb(200, 0, 0, 0)
                padding = dip(8)

                val size = textView("В этой манге сейчас 0 глав")

                val read = textView("Из всего этого количества глав вы прочитали 0")

                val next = textView("След. глава") {
                    visibleOrGone(false)
                }

                applyRecursively {
                    if (it is TextView) {
                        it.textSize = 16f
                    }
                }

                act.launchUI {
                    val chapters =
                        withContext(Dispatchers.IO) { act.mViewModel.getChapters(act.manga) }

                    size.text = "В этой манге сейчас ${chapters.size} глав"

                    read.text =
                        "Из всего этого количества глав вы прочитали ${chapters.filter { it.isRead }.size}"

                    withContext(Dispatchers.IO) { act.mViewModel.getFirstNotReadChapter(act.manga) }?.let {
                        next.text = "След. глава ${it.name}"
                        next.visibleOrGone(true)
                    }

                }
            }
                .lparams(width = matchConstraint, height = wrapContent)

            val deleteBtn = button("Удалить прочитанное") {
                id = ID.generate()
                onClick {
                    DeleteReadChaptersDialog(act, act.manga)
                }
            }.lparams(width = matchConstraint, height = wrapContent)

            applyConstraintSet {
                logo {
                    connect(
                        START to START of PARENT_ID,
                        END to END of PARENT_ID,
                        TOP to TOP of PARENT_ID,
                        BOTTOM to BOTTOM of PARENT_ID
                    )
                }

                continueBtn {
                    connect(
                        START to START of PARENT_ID,
                        BOTTOM to BOTTOM of PARENT_ID,
                        END to END of PARENT_ID
                    )
                }

                startBtn {
                    connect(
                        END to END of PARENT_ID
                    )
                }

                info {
                    connect(
                        START to START of PARENT_ID,
                        END to END of PARENT_ID
                    )
                }

                deleteBtn {
                    connect(
                        START to START of PARENT_ID
                    )
                }

                connect(
                    BOTTOM of deleteBtn to TOP of continueBtn,
                    BOTTOM of startBtn to TOP of continueBtn,
                    END of deleteBtn to START of startBtn margin dip(5),
                    BOTTOM of info to TOP of deleteBtn
                )
            }
            lparams(matchParent, matchParent)
        }
    }

}
