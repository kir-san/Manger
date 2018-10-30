package com.san.kir.manger.components.viewer

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.san.kir.manger.R
import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.extending.ankoExtend.bigImageView
import com.san.kir.manger.extending.ankoExtend.goneOrVisible
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.extending.ankoExtend.onDoubleTapListener
import com.san.kir.manger.extending.ankoExtend.visibleOrGone
import com.san.kir.manger.utils.convertImagesToPng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.button
import org.jetbrains.anko.centerHorizontally
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sp
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import java.io.File

class ViewerPageFragment : Fragment() {
    companion object {
        private const val File_name = "file_name"

        fun newInstance(file: File): ViewerPageFragment {
            val set = Bundle()
            set.putString(File_name, file.absolutePath)
            val frag = ViewerPageFragment()
            frag.arguments = set
            return frag
        }
    }

    private val isError = Binder(false)
    private val errorData = Binder(Triple(0L, 0, 0))
    private lateinit var mFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // При создании фрагмента получить файл
        mFile = File(arguments?.getString(File_name))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val act = activity as ViewerActivity
        return context?.linearLayout {
            lparams(width = matchParent, height = matchParent)

            verticalLayout {
                visibility = View.GONE
                textView(R.string.viewer_page_error)
                textView {
                    errorData.bind { (size, width, height) ->
                        text = context.getString(
                            R.string.viewer_page_error_data,
                            size,
                            width,
                            height
                        )
                    }
                }
                visibleOrGone(isError)
            }

            linearLayout {
                lparams(width = matchParent, height = matchParent)

                bigImageView {
                    lparams(width = matchParent, height = matchParent)

                    GlobalScope.launch(Dispatchers.Main) {
                        try {
                            val img = async {
                                if (mFile.extension in arrayOf("gif", "webp"))
                                    convertImagesToPng(mFile)
                                else
                                    mFile
                            }
                            setImage(ImageSource.uri(Uri.fromFile(img.await())))
                            when(resources.configuration.orientation) {
                                Configuration.ORIENTATION_LANDSCAPE -> setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
                                Configuration.ORIENTATION_PORTRAIT -> setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
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
}

abstract class OtherFragment : Fragment() {

    abstract val textRes: Int
    abstract val onTap: ViewerActivity.() -> Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return context?.relativeLayout {
            // Корень
            lparams(width = matchParent, height = matchParent)

            textView(text = textRes) {
                isClickable = true
                gravity = Gravity.CENTER
                textSize = sp(16).toFloat()

                onClick {
                    (activity as ViewerActivity).onTap() // Выполнить свое действие при нажатии на экран
                }
            }.lparams(width = matchParent, height = matchParent)

            button(R.string.viewer_page_close) {
                onClick {
                    (activity as ViewerActivity).onBackPressed()
                }
            }.lparams {
                alignParentBottom()
                centerHorizontally()
            }
        }
    }
}

// Фрагмент для первой страницы если есть предыдущая глава
class ViewerPagerPrevFragment : OtherFragment() {
    override val textRes = R.string.viewer_page_prev_text
    override val onTap: ViewerActivity.() -> Unit = { presenter.prevChapter() }
}

// Фрагмент для последней страницы если есть предыдущая глава
class ViewerPagerNextFragment : OtherFragment() {
    override val textRes = R.string.viewer_page_next_text
    override val onTap: ViewerActivity.() -> Unit = { presenter.nextChapter() }
}

// Фрагмент для последней страницы если есть нет предыдущей главы
class ViewerPageNoneNextFragment : OtherFragment() {
    override val textRes = R.string.viewer_page_none_next_text
    override val onTap: ViewerActivity.() -> Unit = { presenter.prevPage() }
}

// Фрагмент для первой страницы если есть нет предыдущей главы
class ViewerPageNonePrevFragment : OtherFragment() {
    override val textRes = R.string.viewer_page_none_prev_text
    override val onTap: ViewerActivity.() -> Unit = { presenter.nextPage() }
}
