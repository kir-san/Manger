package com.san.kir.manger.components.viewer

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.san.kir.manger.R
import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.eventBus.positive
import com.san.kir.manger.extending.ankoExtend.bigImageView
import com.san.kir.manger.extending.ankoExtend.goneOrVisible
import com.san.kir.manger.extending.ankoExtend.onDoubleTapListener
import com.san.kir.manger.extending.ankoExtend.visibleOrGone
import com.san.kir.manger.utils.convertImagesToPng
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import java.io.File

class PageItemView(private val act: ViewerActivity) : AnkoComponent<ViewGroup> {

    private val isError = Binder(false)

    private lateinit var bigImage: SubsamplingScaleImageView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        linearLayout {
            lparams(width = matchParent, height = matchParent)

            verticalLayout {
                visibility = View.GONE
                textView(R.string.viewer_page_error)
                visibleOrGone(isError)
            }

            scrollView {
                // Корень
                lparams(width = matchParent, height = matchParent)

                bigImage = bigImageView {
                    lparams(width = matchParent, height = matchParent)

                    onDoubleTapListener {
                        // Переопределение одиночного нажатия
                        onSingleTapConfirmed {
                            if (act.isTapControl) // Включен ли режим управления нажатиями на экран
                                if (it.x < ViewerActivity.LEFT_PART_SCREEN) // Нажатие на левую часть экрана
                                    act.presenter.prevPage() // Предыдущая страница
                                else if (it.x > ViewerActivity.RIGHT_PART_SCREEN) // Нажатие на правую часть
                                    act.presenter.nextPage() // Следущая страница
                            true
                        }
                        // Переопределение двойного нажатия
                        // и заодно отключается зум по двойному нажатию
                        onDoubleTap {
                            // Если нажатие по центральной части
                            if (it.x > ViewerActivity.LEFT_PART_SCREEN && it.x < ViewerActivity.RIGHT_PART_SCREEN)
                            // Переключение видимости баров
                                act.isBar = !act.isBar
                            true
                        }
                    }
                }

                goneOrVisible(isError)
            }
        }
    }

    fun bind(item: File) {
        launch(UI) {
            try {
                bigImage.setImage(
                    ImageSource.uri(
                        Uri.fromFile(
                            withContext(DefaultDispatcher) {
                                if (item.extension in arrayOf("gif", "webp"))
                                    convertImagesToPng(item)
                                else
                                    item
                            })
                    )
                )
            } catch (e: Exception) {
                isError.positive()
                e.printStackTrace()
            }
        }
    }
}
