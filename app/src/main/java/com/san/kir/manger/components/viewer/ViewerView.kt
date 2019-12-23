package com.san.kir.manger.components.viewer

import android.graphics.Color
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListenerAdapter
import com.san.kir.ankofork.AnkoComponent
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.constraint_layout.constraintLayout
import com.san.kir.ankofork.constraint_layout.matchConstraint
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.horizontalPadding
import com.san.kir.ankofork.horizontalProgressBar
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.padding
import com.san.kir.ankofork.sdk28.backgroundColor
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.textColor
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.support.onPageChangeListener
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.extensions.goneOrVisible
import com.san.kir.manger.utils.extensions.specialViewPager
import com.san.kir.manger.utils.extensions.visibleOrGone


class ViewerView(private val presenter: ViewerPresenter) : AnkoComponent<ViewerActivity> {
    lateinit var viewPager: androidx.viewpager.widget.ViewPager

    private object Id {
        val progressBar = ID.generate()
        val bottomBar = ID.generate()
        val chapters = ID.generate()
    }

    override fun createView(ui: AnkoContext<ViewerActivity>) = with(ui) {
        val actionBarSize = dip(50) // Размер бара снизу

        constraintLayout {
            // Корень
            lparams(width = matchParent, height = matchParent)

            horizontalProgressBar {
                id = Id.progressBar
                progressDrawable = ContextCompat.getDrawable(
                    this@with.ctx,
                    R.drawable.activity_viewer_progressbar
                )
                incrementProgressBy(1)
                presenter.max.bind { max = it }
                presenter.progressPages.bind { progress = it }
                goneOrVisible(presenter.isBottomBar)
            }.lparams(width = matchConstraint, height = dip(2)) {
                topToTop = PARENT_ID
                startToStart = PARENT_ID
                endToEnd = PARENT_ID
            }

            viewPager = specialViewPager {
                id = ID.generate()
                presenter.into(this)
                lparams(width = matchConstraint, height = matchConstraint) {
                    topToBottom = Id.progressBar
                    startToStart = PARENT_ID
                    endToEnd = PARENT_ID
                    bottomToBottom = PARENT_ID
                }
                onPageChangeListener {
                    onPageSelected { position ->
                        presenter.progressPages.unicItem = position
                        presenter.invalidateFragmentMenus(position)
                    }
                }

                presenter.isSwipeControl.bind { setLocked(!it) }
            }

            linearLayout {
                id = Id.bottomBar
                backgroundColor = ContextCompat.getColor(this.context, R.color.transparent_dark)

                textView {
                    id = Id.chapters
                    padding = dip(6)
                    horizontalPadding = dip(16)
                    textColor = Color.WHITE
                    presenter.progressChapters.bind { progressChapters ->
                        text = resources.getString(
                            R.string.viewer_chapters_text, progressChapters, presenter.maxChapters
                        )
                    }
                }.lparams(width = matchParent) {
                    weight = 1f
                }

                textView {
                    padding = dip(6)
                    horizontalPadding = dip(16)
                    textColor = Color.WHITE
                    presenter.progressPages.bind { progress ->
                        text = resources
                            .getString(R.string.viewer_pages_text, progress, presenter.max.item)
                    }
                    textAlignment = View.TEXT_ALIGNMENT_TEXT_END
                }.lparams(width = matchParent) {
                    weight = 1f
                }


                presenter.isBottomBar.bind {
                    if (it) {
                        ViewCompat.animate(this)
                            .setDuration(300)
                            .translationY(0f)
                            .setInterpolator(DecelerateInterpolator())
                            .setListener(object : ViewPropertyAnimatorListenerAdapter() {
                                override fun onAnimationStart(view: View?) {
                                    visibleOrGone(true)
                                }
                            })
                            .start()
                    } else {
                        ViewCompat.animate(this)
                            .setDuration(300)
                            .translationY(actionBarSize.toFloat() * 2.5F)
                            .setInterpolator(DecelerateInterpolator())
                            .setListener(object : ViewPropertyAnimatorListenerAdapter() {
                                override fun onAnimationEnd(view: View?) {
                                    visibleOrGone(false)
                                }
                            })
                            .start()
                    }
                }

            }.lparams(width = matchConstraint, height = wrapContent) {
                startToStart = PARENT_ID
                endToEnd = PARENT_ID
                bottomToBottom = PARENT_ID
            }
        }
    }
}
