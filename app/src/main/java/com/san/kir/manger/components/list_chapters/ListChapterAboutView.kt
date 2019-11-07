package com.san.kir.manger.components.list_chapters

import android.graphics.Color
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.BOTTOM
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.END
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.START
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.TOP
import com.san.kir.ankofork.constraint_layout.applyConstraintSet
import com.san.kir.ankofork.constraint_layout.constraintLayout
import com.san.kir.ankofork.constraint_layout.matchConstraint
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.padding
import com.san.kir.ankofork.sdk28.backgroundColor
import com.san.kir.ankofork.sdk28.button
import com.san.kir.ankofork.sdk28.imageView
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.startActivity
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.components.viewer.ViewerActivity
import com.san.kir.manger.extending.dialogs.DeleteReadChaptersDialog
import com.san.kir.manger.utils.ActivityView
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListChapterAboutView(private val act: ListChaptersActivity) : ActivityView() {

    private lateinit var continueBtn: Button
    private lateinit var startBtn: Button
    private lateinit var deleteBtn: Button
    private lateinit var info: TextView

    override fun createView(ui: AnkoContext<BaseActivity>) = with(ui) {
        constraintLayout {
            lparams(matchParent, matchParent)

            imageView {
                id = ID.generate()
                loadImage(act.manga.logo).into(this)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }.lparams(width = matchConstraint, height = matchConstraint) {
                endToEnd = PARENT_ID
                topToTop = PARENT_ID
                startToStart = PARENT_ID
                bottomToBottom = PARENT_ID
            }

            continueBtn = button {
                id = ID.generate()
                isEnabled = false
                padding = dip(16)
            }.lparams(width = matchConstraint)

            startBtn = button(R.string.list_chapters_about_start) {
                id = ID.generate()
                padding = dip(16)
            }.lparams(width = wrapContent)

            deleteBtn = button(R.string.list_chapters_about_delete) {
                id = ID.generate()
                padding = dip(16)
            }.lparams(width = matchConstraint)

            info = textView(act.getString(R.string.list_chapters_about_read, 0, "", 0)) {
                id = ID.generate()
                backgroundColor = Color.argb(210, 0, 0, 0)
                padding = dip(18)
                textSize = 18f
            }.lparams(width = matchConstraint, height = wrapContent)

            applyConstraintSet {
                continueBtn {
                    connect(
                        START to START of PARENT_ID margin dip(9),
                        BOTTOM to BOTTOM of PARENT_ID margin dip(9),
                        END to END of PARENT_ID margin dip(9)
                    )
                }
                startBtn {
                    connect(
                        BOTTOM to TOP of continueBtn margin dip(5),
                        END to END of PARENT_ID margin dip(9)
                    )
                }
                deleteBtn {
                    connect(
                        BOTTOM to TOP of continueBtn margin dip(5),
                        END to START of startBtn margin dip(9),
                        START to START of PARENT_ID margin dip(9)
                    )
                }
                info {
                    connect(
                        BOTTOM to TOP of deleteBtn margin dip(9),
                        START to START of PARENT_ID,
                        END to END of PARENT_ID
                    )
                }
            }

            bind()
        }
    }

    fun bind() {
        continueBtn.onClick {
            act.lifecycleScope.launch(Dispatchers.IO) {
                act.startActivity<ViewerActivity>(
                    "chapter" to /*withContext(Dispatchers.IO) {*/
                        act.mViewModel.getFirstNotReadChapter(act.manga)
                    /*}*/,
                    "is" to act.manga.isAlternativeSort
                )
            }
        }
        startBtn.onClick {
            act.lifecycleScope.launch(Dispatchers.IO) {
                act.startActivity<ViewerActivity>(
                    "chapter" to /*withContext(Dispatchers.IO) {*/
                        act.mViewModel.getFirstChapter(act.manga)
                    /*}*/,
                    "is" to act.manga.isAlternativeSort
                )
            }
        }
        deleteBtn.onClick {
            DeleteReadChaptersDialog(act, act.manga)
        }

        act.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                owner.lifecycleScope.launch(Dispatchers.Main) {
                    val chapters =
                        withContext(Dispatchers.IO) { act.mViewModel.chapters(act.manga) }

                    val readCount = chapters.filter { it.isRead }.size

                    info.text = act.getString(
                        R.string.list_chapters_about_read,
                        readCount,
                        act.resources.getQuantityString(R.plurals.chapters, readCount),
                        chapters.size
                    )

                    withContext(Dispatchers.IO) { act.mViewModel.getFirstNotReadChapter(act.manga) }?.let {
                        continueBtn.text =
                            act.getString(R.string.list_chapters_about_continue, it.name)
                        continueBtn.isEnabled = true
                    }
                }
            }

            override fun onPause(owner: LifecycleOwner) {
                continueBtn.isEnabled = false
            }
        })
    }
}
