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
import com.github.kittinunf.fuel.Fuel
import com.san.kir.manger.R
import com.san.kir.manger.components.download_manager.ChapterDownloader
import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.eventBus.negative
import com.san.kir.manger.extending.ankoExtend.bigImageView
import com.san.kir.manger.extending.ankoExtend.goneOrVisible
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.extending.ankoExtend.onDoubleTapListener
import com.san.kir.manger.extending.ankoExtend.visibleOrGone
import com.san.kir.manger.extending.launchUI
import com.san.kir.manger.utils.convertImagesToPng
import com.san.kir.manger.utils.createDirs
import com.san.kir.manger.utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.button
import org.jetbrains.anko.centerHorizontally
import org.jetbrains.anko.dip
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sp
import org.jetbrains.anko.textView
import java.io.File
import kotlin.coroutines.CoroutineContext

class ViewerPageFragment : Fragment(), CoroutineScope {
    companion object {
        private const val page_name = "page_name"

        fun newInstance(page: Page): ViewerPageFragment {
            val set = Bundle()
            set.putParcelable(page_name, page)
            val frag = ViewerPageFragment()
            frag.arguments = set
            return frag
        }
    }

    lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val isLoad = Binder(true)
    private lateinit var page: Page

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        // При создании фрагмента получить файл
        page = arguments?.getParcelable(page_name)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val act = activity as ViewerActivity
        return context?.linearLayout {
            lparams(width = matchParent, height = matchParent)

            gravity = Gravity.CENTER

            progressBar {
                visibleOrGone(isLoad)
            }.lparams(width = dip(100), height = dip(100)) {
                gravity = Gravity.CENTER
            }

            linearLayout {
                lparams(width = matchParent, height = matchParent)

                bigImageView {
                    lparams(width = matchParent, height = matchParent)

                    launchUI {
                        when (resources.configuration.orientation) {
                            Configuration.ORIENTATION_LANDSCAPE -> setMinimumScaleType(
                                SubsamplingScaleImageView.SCALE_TYPE_START
                            )
                            Configuration.ORIENTATION_PORTRAIT -> setMinimumScaleType(
                                SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
                            )
                        }

                        setImage(loadImage())
                        isLoad.negative()
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

                goneOrVisible(isLoad)
            }
        }
    }

    private suspend fun loadImage() =
        withContext(coroutineContext) {
            val name = ChapterDownloader.nameFromUrl(page.link)
            log("page.link = ${page.link}")
            val file = File(page.fullPath, name)

            if (!file.exists()) {
                Fuel.download(ChapterDownloader.prepareUrl(page.link))
                    .destination { _, _ ->
                        createDirs(file.parentFile)
                        file.createNewFile()
                        file
                    }
                    .response()
                    .third
                    .fold({ },
                          {
                              it.exception.printStackTrace()

                          })
            }

            ImageSource.uri(
                Uri.fromFile(
                    if (file.extension in arrayOf("gif", "webp")) {
                        convertImagesToPng(file)
                    } else {
                        file
                    }
                )
            )
        }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
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
