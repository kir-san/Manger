package com.san.kir.manger.components.Viewer

import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.widget.ImageView.ScaleType.CENTER_CROP
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.Extending.AnkoExtend.goneOrVisible
import com.san.kir.manger.Extending.AnkoExtend.specialViewPager
import com.san.kir.manger.Extending.AnkoExtend.visibleOrInvisible
import com.san.kir.manger.R
import com.san.kir.manger.R.drawable
import com.san.kir.manger.R.string
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
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onSeekBarChangeListener
import org.jetbrains.anko.seekBar
import org.jetbrains.anko.support.v4.onPageChangeListener
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent


class ViewerView(private val act: ViewerActivity) : AnkoComponent<ViewerActivity> {
    private object _id {
        val progressBar = ID.generate()
        val bottomBar = ID.generate()
        val chapters = ID.generate()
        val prev = ID.generate()
        val next = ID.generate()
    }

    var maxChapters = -1
    val progressChapters = Binder(-1)
    val max = Binder(0)

    override fun createView(ui: AnkoContext<ViewerActivity>) = with(ui) {

        val actionBarSize = dip(50) // Размер бара снизу
        val buttonSize = dip(40) // Размер кнопок

        relativeLayout {
            // Корень
            lparams(width = matchParent, height = matchParent)

            horizontalProgressBar {
                id = _id.progressBar
                progressDrawable = ContextCompat.getDrawable(this@with.ctx,
                                                             drawable.activity_viewer_progressbar)
                incrementProgressBy(1)
                bind(this@ViewerView.max) { max = it }
                bind(act.progress) { progress = it }
                goneOrVisible(act.isBottomBar)
            }.lparams(width = matchParent, height = dip(2)) {
                alignParentTop()
            }

            relativeLayout {
                lparams {
                    width = matchParent
                    height = actionBarSize
                    bind(act.isBottomBar) { height = if (it) actionBarSize else 0 }
                    alignParentBottom()
                }
                id = _id.bottomBar
                backgroundColor = Color.parseColor("#ff212121")

                seekBar {
                    bind(this@ViewerView.max) { max = it }
                    bind(act.progress) { progress = it }

                    onSeekBarChangeListener {
                        var _progress = 0
                        onProgressChanged { _, progress, _ -> _progress = progress }
                        onStopTrackingTouch { act.progress.item = _progress }
                    }
                }.lparams(width = wrapContent, height = wrapContent) {
                    alignParentBottom()
                    rightOf(_id.prev) // От левой кнопки
                    leftOf(_id.next) // До правой
                    bottomMargin = dip(5)
                }

                textView {
                    padding = dip(6)
                    textColor = Color.WHITE
                    bind(act.progress) { progress ->
                        text = resources
                                .getString(string.viewer_pages_text, progress, max.item)
                    }
                }.lparams(width = wrapContent, height = wrapContent) {
                    rightOf(_id.prev)
                    leftOf(_id.chapters)
                }

                textView {
                    id = _id.chapters
                    padding = dip(6)
                    textColor = Color.WHITE
                    bind(progressChapters) { progressChapters ->
                        text = resources.getString(string.viewer_chapters_text,
                                                   progressChapters,
                                                   maxChapters)
                    }
                }.lparams(width = wrapContent, height = wrapContent) {
                    leftOf(_id.next) // Слева от кнопки
                }


                imageButton {
                    id = _id.prev
                    backgroundColor = Color.parseColor("#00ffffff")
                    scaleType = CENTER_CROP
                    imageResource = R.drawable.ic_previous_white
                    visibleOrInvisible(act.isPrev)
                    onClick { act.prevChapter() }
                }.lparams(width = buttonSize, height = buttonSize) {
                    alignParentLeft()
                    centerInParent()
                    leftMargin = dip(6)
                    rightMargin = leftMargin
                }

                imageButton {
                    id = _id.next
                    backgroundColor = Color.parseColor("#00ffffff")
                    scaleType = CENTER_CROP
                    imageResource = R.drawable.ic_next_white
                    visibleOrInvisible(act.isNext)
                    onClick { act.nextChapter() }
                }.lparams(width = buttonSize, height = buttonSize) {
                    alignParentRight()
                    centerInParent()
                    leftMargin = dip(6)
                    rightMargin = leftMargin
                }

            }

            specialViewPager {
                id = ID.generate()
                lparams(width = matchParent, height = matchParent) {
                    below(_id.progressBar)
                    above(_id.bottomBar)
                }

                onPageChangeListener { onPageSelected { position -> act.progress.item = position } }
                bind(act.progress) { currentItem = it }
                act.adapter.bind {
                    adapter = it
                    currentItem = act.progress.item
                }
                act.isSwipeControl.bind { setLocked(!it) }
            }

        }
    }
}
