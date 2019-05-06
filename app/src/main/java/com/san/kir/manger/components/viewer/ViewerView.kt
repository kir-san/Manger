package com.san.kir.manger.components.viewer

import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView.ScaleType.CENTER_CROP
import com.san.kir.manger.R
import com.san.kir.manger.R.drawable
import com.san.kir.manger.R.string
import com.san.kir.manger.extending.anko_extend.goneOrVisible
import com.san.kir.manger.extending.anko_extend.onClick
import com.san.kir.manger.extending.anko_extend.onSeekBarChangeListener
import com.san.kir.manger.extending.anko_extend.specialViewPager
import com.san.kir.manger.extending.anko_extend.visibleOrGone
import com.san.kir.manger.extending.anko_extend.visibleOrInvisible
import com.san.kir.manger.utils.ID
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.above
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.alignParentLeft
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.alignParentTop
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.below
import org.jetbrains.anko.centerInParent
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.imageButton
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.rightOf
import org.jetbrains.anko.seekBar
import org.jetbrains.anko.support.v4.onPageChangeListener
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent


class ViewerView(private val presenter: ViewerPresenter) : AnkoComponent<ViewerActivity> {
    lateinit var viewPager: ViewPager

    private object Id {
        val progressBar = ID.generate()
        val bottomBar = ID.generate()
        val chapters = ID.generate()
        val prev = ID.generate()
        val next = ID.generate()
    }

    override fun createView(ui: AnkoContext<ViewerActivity>) = with(ui) {
        val actionBarSize = dip(50) // Размер бара снизу
        val buttonSize = dip(40) // Размер кнопок

        relativeLayout {
            // Корень
            lparams(width = matchParent, height = matchParent)

            horizontalProgressBar {
                id = Id.progressBar
                progressDrawable = ContextCompat.getDrawable(
                    this@with.ctx,
                    drawable.activity_viewer_progressbar
                )
                incrementProgressBy(1)
                presenter.max.bind { max = it }
                presenter.progressPages.bind { progress = it }
                goneOrVisible(presenter.isBottomBar)
            }.lparams(width = matchParent, height = dip(2)) {
                alignParentTop()
            }

            relativeLayout {
                lparams {
                    width = matchParent
                    height = actionBarSize
                    alignParentBottom()
                }
                id = Id.bottomBar
                backgroundColor = Color.parseColor("#ff212121")

                seekBar {
                    presenter.max.bind { max = it }
                    presenter.progressPages.bind { progress = it }

                    onSeekBarChangeListener {
                        var progress = 0
                        onProgressChanged { _, p, _ -> progress = p }
                        onStopTrackingTouch {
                            //                            presenter.progressPages.unicItem = progress
                            viewPager.currentItem = progress
                        }
                    }
                }.lparams(width = wrapContent, height = wrapContent) {
                    alignParentBottom()
                    rightOf(Id.prev) // От левой кнопки
                    leftOf(Id.next) // До правой
                    bottomMargin = dip(5)
                }

                textView {
                    padding = dip(6)
                    textColor = Color.WHITE
                    presenter.progressPages.bind { progress ->
                        text = resources
                            .getString(string.viewer_pages_text, progress, presenter.max.item)
                    }
                }.lparams(width = wrapContent, height = wrapContent) {
                    rightOf(Id.prev)
                    leftOf(Id.chapters)
                }

                textView {
                    id = Id.chapters
                    padding = dip(6)
                    textColor = Color.WHITE
                    presenter.progressChapters.bind { progressChapters ->
                        text = resources.getString(
                            string.viewer_chapters_text,
                            progressChapters,
                            presenter.maxChapters
                        )
                    }
                }.lparams(width = wrapContent, height = wrapContent) {
                    leftOf(Id.next) // Слева от кнопки
                }

                imageButton {
                    id = Id.prev
                    backgroundColor = Color.parseColor("#00ffffff")
                    scaleType = CENTER_CROP
                    imageResource = R.drawable.ic_previous_white
                    visibleOrInvisible(presenter.isPrev)
                    onClick { presenter.prevChapter() }
                }.lparams(width = buttonSize, height = buttonSize) {
                    alignParentLeft()
                    centerInParent()
                    leftMargin = dip(6)
                    rightMargin = leftMargin
                }

                imageButton {
                    id = Id.next
                    backgroundColor = Color.parseColor("#00ffffff")
                    scaleType = CENTER_CROP
                    imageResource = R.drawable.ic_next_white
                    visibleOrInvisible(presenter.isNext)
                    onClick { presenter.nextChapter() }
                }.lparams(width = buttonSize, height = buttonSize) {
                    alignParentRight()
                    centerInParent()
                    leftMargin = dip(6)
                    rightMargin = leftMargin
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

            }

            viewPager = specialViewPager {
                id = ID.generate()
                presenter.into(this)
                lparams(width = matchParent, height = matchParent) {
                    below(Id.progressBar)
                    above(Id.bottomBar)
                }
                onPageChangeListener {
                    onPageSelected { position ->
                        presenter.progressPages.unicItem = position
                        presenter.invalidateFragmentMenus(position)
                    }
                }

                presenter.isSwipeControl.bind { setLocked(!it) }
            }
        }
    }
}
