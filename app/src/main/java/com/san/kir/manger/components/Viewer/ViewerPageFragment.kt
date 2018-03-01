package com.san.kir.manger.components.Viewer

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.davemorrissey.labs.subscaleview.ImageSource
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.Extending.AnkoExtend.bigImageView
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.Extending.AnkoExtend.onDoubleTapListener
import com.san.kir.manger.R
import com.san.kir.manger.utils.convertImagesToPng
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sp
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import java.io.File

class ViewerPageFragment : Fragment() {
    companion object {
        private const val File_name = "file_name"
        // Инициализация фрагмента с изображением
        fun newInitstate(file: File): ViewerPageFragment {
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
        // создание разметки
        val act = activity as ViewerActivity
        return context?.linearLayout {
            // Корень
            lparams(width = matchParent, height = matchParent)

            verticalLayout {
                visibility = View.GONE
                textView("При загрузке произошла ошибка")
                textView {
                    bind(errorData) { (size, width, height) ->
                        text = "Размер файла $size Мб\nРазмер страницы $width x $height"
                    }
                }
                bind(isError) { visibility = if (it) View.VISIBLE else View.GONE }
            }

            scrollView {
                // Корень
                lparams(width = matchParent, height = matchParent)

                bigImageView {
                    lparams(width = matchParent, height = matchParent)

                    async {
                        if (mFile.extension in arrayOf("gif", "webp"))
                            mFile = convertImagesToPng(mFile)
                        async(UI) {
                            setImage(ImageSource.uri(Uri.fromFile(mFile)))
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

                bind(isError) { visibility = if (it) View.GONE else View.VISIBLE }
            }
        }
    }
}

// Настроенная разметка для остальных фрагментов
private fun Context.readyLayout(
    viewer: ViewerActivity,
    textRes: Int,
    onTap: (ViewerActivity) -> Unit
): View = with(this) {
    verticalLayout {
        // Корень
        lparams(width = matchParent, height = matchParent)

        textView(text = textRes) {
            isClickable = true
            gravity = Gravity.CENTER
            textSize = sp(16).toFloat()

            onClick {
                onTap(viewer) // Выполнить свое действие при нажатии на экран
            }
        }.lparams(width = matchParent, height = matchParent)
    }
}

// Фрагмент для первой страницы если есть предыдущая глава
class ViewerPagerPrevFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return context?.readyLayout(
            activity as ViewerActivity,
            R.string.viewer_page_prev_text,
            { it.presenter.prevChapter() }
        )
    }
}

// Фрагмент для последней страницы если есть предыдущая глава
class ViewerPagerNextFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return context?.readyLayout(
            activity as ViewerActivity,
            R.string.viewer_page_next_text,
            { it.presenter.nextChapter() }
        )
    }
}

// Фрагмент для последней страницы если есть нет предыдущей главы
class ViewerPageNoneNextFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return context?.readyLayout(activity as ViewerActivity,
                                   R.string.viewer_page_none_next_text,
                                   { it.presenter.prevPage() })
    }
}

// Фрагмент для первой страницы если есть нет предыдущей главы
class ViewerPageNonePrevFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return context?.readyLayout(activity as ViewerActivity,
                                   R.string.viewer_page_none_prev_text,
                                   { it.presenter.nextPage() })
    }
}
