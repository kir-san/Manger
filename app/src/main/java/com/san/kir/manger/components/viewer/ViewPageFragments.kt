package com.san.kir.manger.components.viewer

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.kittinunf.fuel.Fuel
import com.san.kir.ankofork.Binder
import com.san.kir.ankofork.alignParentBottom
import com.san.kir.ankofork.centerHorizontally
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.negative
import com.san.kir.ankofork.positive
import com.san.kir.ankofork.sdk28.button
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.progressBar
import com.san.kir.ankofork.sdk28.relativeLayout
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.sp
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.withArguments
import com.san.kir.manger.R
import com.san.kir.manger.components.download_manager.ChapterDownloader
import com.san.kir.manger.utils.extensions.bigImageView
import com.san.kir.manger.utils.extensions.convertImagesToPng
import com.san.kir.manger.utils.extensions.createDirs
import com.san.kir.manger.utils.extensions.goneOrVisible
import com.san.kir.manger.utils.extensions.isOkPng
import com.san.kir.manger.utils.extensions.onDoubleTapListener
import com.san.kir.manger.utils.extensions.showAlways
import com.san.kir.manger.utils.extensions.visibleOrGone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ViewerPageFragment : Fragment() {
    companion object {
        private const val page_name = "page_name"

        fun newInstance(page: Page): ViewerPageFragment {
            return  ViewerPageFragment().withArguments(page_name to page)
        }
    }

    private val isLoad = Binder(true)
    private lateinit var page: Page
    private lateinit var view: SubsamplingScaleImageView
    private var showHide: Job? = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // При создании фрагмента получить файл
        page = arguments?.getParcelable(page_name)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val act = activity as ViewerActivity
        return context?.verticalLayout {
            lparams(width = matchParent, height = matchParent)

            gravity = Gravity.CENTER

            progressBar {
                visibleOrGone(isLoad)
                onClick {
                    // Переключение видимости баров
                    act.isBar = !act.isBar
                }
            }.lparams(width = dip(100), height = dip(100)) {
                gravity = Gravity.CENTER
            }

            linearLayout {
                lparams(width = matchParent, height = matchParent)

                view = bigImageView {
                    lparams(width = matchParent, height = matchParent)

                    lifecycleScope.launch(Dispatchers.Main) {
                        when (resources.configuration.orientation) {
                            Configuration.ORIENTATION_LANDSCAPE -> setMinimumScaleType(
                                SubsamplingScaleImageView.SCALE_TYPE_START
                            )
                            Configuration.ORIENTATION_PORTRAIT -> setMinimumScaleType(
                                SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
                            )
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

                            // Если нажатие по центральной части
                            if (it.x > ViewerActivity.LEFT_PART_SCREEN
                                && it.x < ViewerActivity.RIGHT_PART_SCREEN) {
                                // Переключение видимости баров

                                if (act.isBar) {
                                    act.isBar = false
                                    showHide?.cancel()
                                    showHide = null
                                } else {
                                    act.isBar = true
                                    showHide = lifecycleScope.launch {
                                        delay(3000L)
                                        act.isBar = false
                                    }
                                }
                            }

                            true
                        }
                    }
                }

                loadImage(view, false)

                goneOrVisible(isLoad)
            }
        }
    }

    private fun loadImage(view: SubsamplingScaleImageView, force: Boolean) {
        lifecycleScope.launch(Dispatchers.Main) {
            isLoad.positive()

            view.setImage(getImage(force))

            isLoad.negative()
        }
    }

    private suspend fun getImage(force: Boolean): ImageSource {
        return withContext(Dispatchers.Default) {
            val name = ChapterDownloader.nameFromUrl(page.link)

            var file = File(page.fullPath, name)

            file = File(file.parentFile, "${file.nameWithoutExtension}.png")

            if (file.exists() && file.isOkPng() && !force) {
                return@withContext ImageSource.uri(Uri.fromFile(file))
            }
            file.delete()

            file = File(page.fullPath, name)

            if (file.exists() && !force) {
                val png = convertImagesToPng(file)
                if (png.isOkPng()) {
                    return@withContext ImageSource.uri(Uri.fromFile(png))
                }
                png.delete()
            }

            file.delete()

            Fuel.download(ChapterDownloader.prepareUrl(page.link))
                .fileDestination { _, _ ->
                    (file.parentFile).createDirs()
                    file.createNewFile()
                    file
                }
                .response()
                .third
                .fold({ },
                      {
                          it.exception.printStackTrace()
                      })

            return@withContext ImageSource.uri(
                Uri.fromFile(
                    if (file.extension in arrayOf("gif", "webp", "jpg", "jpeg")) {
                        convertImagesToPng(file)
                    } else {
                        file
                    }
                )
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.add(0, 2, 2, "Обновить").showAlways().setIcon(R.drawable.ic_action_update_white)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 2) {
            loadImage(view, true)
        }
        return super.onOptionsItemSelected(item)
    }
}

abstract class OtherFragment : androidx.fragment.app.Fragment() {

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
