package com.san.kir.manger.components.viewer

import android.view.Gravity
import android.view.ViewGroup
import com.san.kir.manger.R
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.button
import org.jetbrains.anko.centerHorizontally
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sp
import org.jetbrains.anko.textView


abstract class OtherItemView(private val viewer: ViewerActivity) : AnkoComponent<ViewGroup> {
    abstract val textRes: Int
    abstract val onTap: ViewerActivity.() -> Unit

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            // Корень
            lparams(width = matchParent, height = matchParent)

            textView(text = textRes) {
                isClickable = true
                gravity = Gravity.CENTER
                textSize = sp(16).toFloat()

                onClick {
                    viewer.onTap() // Выполнить свое действие при нажатии на экран
                }
            }.lparams(width = matchParent, height = matchParent)

            button(R.string.viewer_page_close) {
                onClick {
                    viewer.onBackPressed()
                }
            }.lparams {
                alignParentBottom()
                centerHorizontally()
            }
        }
    }
}

// для первой страницы если есть предыдущая глава
class FirstWithPrevItemView(viewer: ViewerActivity) : OtherItemView(viewer) {
    override val textRes = R.string.viewer_page_prev_text
    override val onTap: ViewerActivity.() -> Unit = { presenter.prevChapter() }
}

// для последней страницы если есть предыдущая глава
class LastWithNextItemView(viewer: ViewerActivity) : OtherItemView(viewer) {
    override val textRes = R.string.viewer_page_next_text
    override val onTap: ViewerActivity.() -> Unit = { presenter.nextChapter() }
}

// для последней страницы если есть нет предыдущей главы
class LastNoneNextItemView(viewer: ViewerActivity) : OtherItemView(viewer) {
    override val textRes = R.string.viewer_page_none_next_text
    override val onTap: ViewerActivity.() -> Unit = { presenter.prevPage() }
}

// для первой страницы если есть нет предыдущей главы
class FirstNonePrevItemView(viewer: ViewerActivity) : OtherItemView(viewer) {
    override val textRes = R.string.viewer_page_none_prev_text
    override val onTap: ViewerActivity.() -> Unit = { presenter.nextPage() }
}

