package com.san.kir.manger.components.Viewer

import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView.ScaleType.CENTER_CROP
import com.san.kir.manger.EventBus.BinderRx
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.Extending.AnkoExtend.specialViewPager
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
    private object _id { // id элементов
        val progressBar = ID.generate()
        val bottomBar = ID.generate()
        val seekbar = ID.generate()
        val pages = ID.generate()
        val chapters = ID.generate()
        val prev = ID.generate()
        val next = ID.generate()
    }

    var maxChapters = -1 // Количество глав доступных для чтения
    val progressChapters = BinderRx(-1) // Текущая глава
    val max = BinderRx(0) // Количество страниц в главе

    override fun createView(ui: AnkoContext<ViewerActivity>) = with(ui) {

        val actionBarSize = dip(50) // Размер бара снизу
        val buttonSize = dip(40) // Размер кнопок

        relativeLayout {
            // Корень
            lparams(width = matchParent, height = matchParent)

            horizontalProgressBar {
                lparams(width = matchParent, height = dip(2)) {
                    alignParentTop()
                }

                id = _id.progressBar

                // Установка своего прогрессБара
                progressDrawable = ContextCompat.getDrawable(this@with.ctx,
                                                             drawable.activity_viewer_progressbar)
                incrementProgressBy(1) // Увеличение прогресса по одному

                bind(this@ViewerView.max) { max = it } // Установка максимального значения
                bind(act.progress) { progress = it } // Установка текущего значения
                // Переключение режима видимости
                bind(act.isBottomBar) { post { visibility = if (it) View.GONE else View.VISIBLE } }
            }

            relativeLayout {
                // Нижний бар
                lparams {
                    width = matchParent
                    // Переключение видимости, путем смены высоты элемента
                    // сделано так, ибо по другому не работает
                    height = actionBarSize
                    bind(act.isBottomBar) { height = if (it) actionBarSize else 0 }
                    alignParentBottom()
                }

                id = _id.bottomBar

                backgroundColor = Color.parseColor("#ff212121")

                seekBar {
                    // Ползунок
                    id = _id.seekbar

                    //TODO
//                    gravity = Gravity.CENTER

                    bind(this@ViewerView.max) { max = it } // Установка максимального значения
                    bind(act.progress) { progress = it } // Установка текущего значения

                    onSeekBarChangeListener {
                        // При изменении текущего значения
                        var _progress = 0
                        // При изменении прогресса присвоить во временную переменную
                        onProgressChanged { _, progress, _ -> _progress = progress }
                        // Если изменения закончились изменить progress
                        onStopTrackingTouch { act.progress.item = _progress }
                    }
                }.lparams(width = wrapContent, height = wrapContent) {
                    alignParentBottom()
                    rightOf(_id.prev) // От левой кнопки
                    leftOf(_id.next) // До правой
                    bottomMargin = dip(5)
                }

                textView {
                    // Отображение прогресса чтения страниц в текстовом виде
                    id = _id.pages

                    padding = dip(6)

                    textColor = Color.WHITE

                    // Обновление статуса
                    bind(act.progress) { progress ->
                        text = resources
                                .getString(string.viewer_pages_text, progress, max.item)
                    }
                }.lparams(width = wrapContent, height = wrapContent) {
                    rightOf(_id.prev) // От кнопки слева
                    leftOf(_id.chapters) // до текста справа
                }


                textView {
                    // Отображние статуса прочитанных глав в текстовом виде
                    id = _id.chapters

                    padding = dip(6)
                    textColor = Color.WHITE
                    // Обновить статус
                    bind(progressChapters) { progressChapters ->
                        text = resources.getString(string.viewer_chapters_text,
                                                   progressChapters,
                                                   maxChapters)
                    }
                }.lparams(width = wrapContent, height = wrapContent) {
                    leftOf(_id.next) // Слева от кнопки
                }


                imageButton {
                    // Кнопка перелючения на предыдущию страницу
                    id = _id.prev

                    backgroundColor = Color.parseColor("#00ffffff")
                    scaleType = CENTER_CROP
                    imageResource = R.drawable.ic_previous_white

                    bind(act.isPrev) {
                        // Переключения видимости
                        visibility =
                                if (it) View.VISIBLE
                                else View.INVISIBLE
                    }
                    onClick { act.prevChapter() } // Нажатие на кнопку - переключение
                }.lparams(width = buttonSize, height = buttonSize) {
                    alignParentLeft()
                    centerInParent()
                    leftMargin = dip(6)
                    rightMargin = leftMargin
                }


                imageButton {
                    // Кнопка перелючения на следущую страницу
                    id = _id.next

                    backgroundColor = Color.parseColor("#00ffffff")
                    scaleType = CENTER_CROP
                    imageResource = R.drawable.ic_next_white

                    bind(act.isNext) {
                        // Переключения видимости
                        visibility =
                                if (it) View.VISIBLE
                                else View.INVISIBLE
                    }
                    onClick { act.nextChapter() } // Нажатие на кнопку - переключение
                }.lparams(width = buttonSize, height = buttonSize) {
                    alignParentRight()
                    centerInParent()
                    leftMargin = dip(6)
                    rightMargin = leftMargin
                }

            }

            specialViewPager {
                // Виджет просмотра картинок
                lparams(width = matchParent, height = matchParent) {
                    below(_id.progressBar) // Ниже прогрессБара
                    above(_id.bottomBar) // Выше нижнего бара
                }

                // При изменении страницы, изменить переменную progress
                onPageChangeListener { onPageSelected { position -> act.progress.item = position } }
                bind(act.progress) { currentItem = it } // Установка текущей страницы
                bind(act.adapter) {
                    adapter = it
                    currentItem = act.progress.item
                } // Установка адаптера
                bind(act.isSwipeControl) { setLocked(!it) } // Переключения блокировки листания
            }

        }
    }
}
