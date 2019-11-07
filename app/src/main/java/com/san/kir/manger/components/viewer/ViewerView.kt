package com.san.kir.manger.components.viewer

import android.graphics.Color
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListenerAdapter
import com.san.kir.ankofork.AnkoComponent
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.above
import com.san.kir.ankofork.alignParentBottom
import com.san.kir.ankofork.alignParentLeft
import com.san.kir.ankofork.alignParentRight
import com.san.kir.ankofork.alignParentTop
import com.san.kir.ankofork.below
import com.san.kir.ankofork.centerInParent
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.horizontalProgressBar
import com.san.kir.ankofork.leftOf
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.padding
import com.san.kir.ankofork.rightOf
import com.san.kir.ankofork.sdk28.backgroundColor
import com.san.kir.ankofork.sdk28.imageButton
import com.san.kir.ankofork.sdk28.imageResource
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.onSeekBarChangeListener
import com.san.kir.ankofork.sdk28.relativeLayout
import com.san.kir.ankofork.sdk28.seekBar
import com.san.kir.ankofork.sdk28.textColor
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.support.onPageChangeListener
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.extensions.goneOrVisible
import com.san.kir.manger.utils.extensions.specialViewPager
import com.san.kir.manger.utils.extensions.visibleOrGone
import com.san.kir.manger.utils.extensions.visibleOrInvisible


class ViewerView(private val presenter: ViewerPresenter) : AnkoComponent<ViewerActivity> {
    lateinit var viewPager: androidx.viewpager.widget.ViewPager

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
                    R.drawable.activity_viewer_progressbar
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
                            .getString(R.string.viewer_pages_text, progress, presenter.max.item)
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
                            R.string.viewer_chapters_text,
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
                    scaleType = ImageView.ScaleType.CENTER_CROP
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
                    scaleType = ImageView.ScaleType.CENTER_CROP
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
