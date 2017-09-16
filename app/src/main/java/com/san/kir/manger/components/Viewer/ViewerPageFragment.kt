package com.san.kir.manger.components.Viewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView.ScaleType.CENTER_CROP
import com.san.kir.manger.EventBus.BinderRx
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.Extending.AnkoExtend.onDoubleTapListener
import com.san.kir.manger.Extending.AnkoExtend.photoView
import com.san.kir.manger.R
import com.san.kir.manger.components.Viewer.ViewerActivity
import com.san.kir.manger.picasso.Picasso
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sp
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import java.io.File

class ViewerPageFragment : Fragment() {
    companion object {
        private val File_name = "file_name"

        // Инициализация фрагмента с изображением
        fun newInitstate(file: File): ViewerPageFragment {
            val set = Bundle()
            set.putString(File_name, file.absolutePath)
            val frag = ViewerPageFragment()
            frag.arguments = set
            return frag
        }
    }

    private val isError = BinderRx(false)
    private val errorData = BinderRx(Triple(0L, 0, 0))
    private lateinit var mFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // При создании фрагмента получить файл
        mFile = File(arguments.getString(File_name))
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // создание разметки
        val act = activity as ViewerActivity
        return context.linearLayout {
            // Корень
            lparams(width = matchParent, height = matchParent)

            verticalLayout {
                visibility = View.GONE
                textView("При загрузке произошла ошибка")
                textView {
                    bind(errorData) { (size, width, height) ->
                        text = "Размер файла $size Мб\n" + "Размер страницы $width x $height"
                    }
                }
                bind(isError) { visibility = if (it) View.VISIBLE else View.GONE }
            }

            scrollView {
                // Корень
                lparams(width = matchParent, height = matchParent)

                photoView {
                    // виджет с фото, там работает мультитач зум
                    lparams(width = matchParent, height = wrapContent)

                    scaleType = CENTER_CROP
                    adjustViewBounds = true


                    val img = BitmapFactory.Options()
                    img.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(mFile.path, img)

                    val MAX = 9500.0
                    var diff = 1.0

                    val mainSize = maxOf(img.outWidth, img.outHeight)

                    if (img.outWidth <= 0 || img.outHeight <= 0) {
                        isError.item = true
                        errorData.item = Triple(mFile.length() / (1024 * 1024),
                                                img.outWidth,
                                                img.outHeight)
                    } else {
                        if (mainSize > MAX)
                            diff = Math.floor(MAX / mainSize * 100) / 100

                        Picasso.with(this@ViewerPageFragment.context)
                                .load(mFile)
                                .resize((img.outWidth * diff).toInt(),
                                        (img.outHeight * diff).toInt())
                                .config(Bitmap.Config.RGB_565)
                                .centerCrop()
                                .into(this)
                    }


                    onDoubleTapListener {
                        // Переопределение одиночного нажатия
                        onSingleTapConfirmed {
                            if (act.isTapControl) // Включен ли режим управления нажатиями на экран
                                if (it.x < act.LEFT_PART_SCREEN) // Нажатие на левую часть экрана
                                    act.progress.item -= 1 // Предыдущая страница
                                else if (it.x > act.RIGHT_PART_SCREEN) // Нажатие на правую часть
                                    act.progress.item += 1 // Следущая страница
                            true
                        }
                        // Переопределение двойного нажатия
                        // и заодно отключается зум по двойному нажатию
                        onDoubleTap {
                            // Если нажатие по центральной части
                            if (it.x > act.LEFT_PART_SCREEN && it.x < act.RIGHT_PART_SCREEN)
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
private fun Context.readyLayout(viewer: ViewerActivity,
                                textRes: Int,
                                onTap: (ViewerActivity) -> Unit): View = with(this) {
    verticalLayout {
        // Корень
        lparams(width = matchParent, height = matchParent)
        textView(text = textRes) {
            lparams(width = matchParent, height = matchParent)
            isClickable = true
            gravity = Gravity.CENTER
            textSize = sp(16).toFloat()

            onClick {
                onTap(viewer) // Выполнить свое действие при нажатии на экран
            }
        }
    }
}

// Фрагмент для первой страницы если есть предыдущая глава
class ViewerPagerPrevFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return context.readyLayout(activity as ViewerActivity,
                                   R.string.viewer_page_prev_text,
                                   ViewerActivity::prevChapter)
    }
}

// Фрагмент для последней страницы если есть предыдущая глава
class ViewerPagerNextFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return context.readyLayout(activity as ViewerActivity,
                                   R.string.viewer_page_next_text,
                                   ViewerActivity::nextChapter)
    }
}

// Фрагмент для последней страницы если есть нет предыдущей главы
class ViewerPageNoneNextFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return context.readyLayout(activity as ViewerActivity,
                                   R.string.viewer_page_none_next_text,
                                   { it.progress.item -= 1 })
    }
}

// Фрагмент для первой страницы если есть нет предыдущей главы
class ViewerPageNonePrevFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return context.readyLayout(activity as ViewerActivity,
                                   R.string.viewer_page_none_prev_text,
                                   { it.progress.item += 1 })
    }
}
