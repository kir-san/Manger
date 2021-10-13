package com.san.kir.manger.components.viewer

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.ankofork.doFromSdk
import com.san.kir.ankofork.negative
import com.san.kir.ankofork.positive
import com.san.kir.ankofork.setContentView
import com.san.kir.manger.R
import com.san.kir.manger.Viewer
import com.san.kir.manger.data.datastore.ViewerRepository
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.add
import com.san.kir.manger.utils.extensions.log
import com.san.kir.manger.utils.extensions.showAlways
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class ViewerActivity : BaseActivity() {
    companion object { // константы для сохранения настроек
        var LEFT_PART_SCREEN = 0 // Левая часть экрана
        var RIGHT_PART_SCREEN = 0 // Правая часть экрана
    }

    val mViewModel: ViewerViewModel by viewModels()

    @Inject
    lateinit var viewerStore: ViewerRepository

    private var showHide: Job? = Job()

    // Режимы листания страниц
    var isTapControl = false // Нажатия на экран
    private var isKeyControl = false // Кнопки громкости

    var chapter by Delegates.observable(Chapter()) { _, _, new ->
        mViewModel.setChapter(new)
    }

    val presenter by lazy { ViewerPresenter(this) }
    val mView by lazy { ViewerView(presenter) }

    private var readTime = 0L

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mView.setContentView(this) // Установка разметки

        setSupportActionBar(mView.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Кнопка назад в верхнем баре
        supportActionBar?.setShowHideAnimationEnabled(true) // Анимация скрытия, сокрытия

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            showHide = if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                supportActionBar!!.show() // Показать бар сверху
                presenter.isBottomBar.positive()// Показать нижний бар
                lifecycleScope.launch {
                    delay(4000L)
                    hideSystemUI()
                }
            } else {
                supportActionBar!!.hide() //Скрыть бар сверху
                presenter.isBottomBar.negative() // Скрыть нижний бар
                showHide?.cancel()
                null
            }
        }

        /*
        * Обработка полученных данных для конфигурации менеджера
        * возможные варианты запуска:
        * - continue, manga - продолжить чтение с первой не прочитанной главы
        * - !continue, manga - начать чтение с начала
        * - !continue, chapter - продолжить чтение с текущей главы
        * - все остальные варианты закрывают просмоторщик
        * */
        lifecycleScope.launch(Dispatchers.Main) {
            intent.apply {
                val isAlternative = getBooleanExtra("is", false)

                val tempChapter = mViewModel.getChapter()
                if (tempChapter != null) {
                    log("restore chapter")
                    chapter = tempChapter
                    presenter.configManager(chapter, isAlternative).invokeOnCompletion {
                        presenter.isLoad.negative()
                    }
                } else
                    if (hasExtra("continue")) {
                        log("get continue")
                        val manga = getParcelableExtra<Manga>("manga")
                        if (manga != null) {
                            val chapt = mViewModel.getFirstNotReadChapter(manga)
                            if (chapt != null) {
                                log("init with manga is ok")
                                chapter = chapt
                                presenter.configManager(chapter, isAlternative).invokeOnCompletion {
                                    presenter.isLoad.negative()
                                }
                            } else {
                                log("chapter is not find")
                                applicationContext.longToast("Нет глав для чтения")
                                finish()
                            }
                        } else {
                            log("manga is not find")
                            applicationContext.longToast("Непредвиденная ошибка")
                            finish()
                        }
                    } else {

                        val chapt = getParcelableExtra<Chapter>("chapter")
                        val manga = getParcelableExtra<Manga>("manga")

                        when {
                            chapt != null -> {
                                log("init with chapter is ok")
                                chapter = chapt
                                presenter.configManager(chapter, isAlternative).invokeOnCompletion {
                                    presenter.isLoad.negative()
                                }
                            }
                            manga != null -> {
                                val chap = mViewModel.getFirstChapter(manga)
                                if (chap != null) {
                                    log("init with manga is ok")
                                    chapter = chap
                                    presenter.configManager(chapter, isAlternative)
                                        .invokeOnCompletion {
                                            presenter.isLoad.negative()
                                        }
                                } else {
                                    log("chapter is not find")
                                    applicationContext.longToast("Нет глав для чтения")
                                    finish()
                                }
                            }
                            else -> {
                                log("chapter or manga is not find")
                                applicationContext.longToast("Нет главы для чтения")
                                finish()
                            }
                        }
                    }
                withContext(Dispatchers.Main) {
                    title = chapter.name // Смена заголовка
                }
            }
        }
    }

    override fun onBackPressed() {
        mViewModel.clearChapter()
        super.onBackPressed()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    fun toogleBars() {
        if (window.decorView.windowSystemUiVisibility and
            View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
            hideSystemUI()
        } else {
            showSystemUI()
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    override fun onResume() {
        super.onResume()

        // Загрузка настроек
        lifecycleScope.launchWhenResumed {
            viewerStore.data.collect { data ->
                requestedOrientation = when (data.orientation) {
                    Viewer.Orientation.PORT -> SCREEN_ORIENTATION_PORTRAIT
                    Viewer.Orientation.LAND -> SCREEN_ORIENTATION_LANDSCAPE
                    Viewer.Orientation.AUTO -> SCREEN_ORIENTATION_SENSOR
                    Viewer.Orientation.PORT_REV -> SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    Viewer.Orientation.LAND_REV -> SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    Viewer.Orientation.AUTO_PORT -> SCREEN_ORIENTATION_SENSOR_PORTRAIT
                    Viewer.Orientation.AUTO_LAND -> SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    Viewer.Orientation.UNRECOGNIZED -> SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }

                doFromSdk(28) {
                    window.attributes.layoutInDisplayCutoutMode =
                        if (data.cutout) WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        else WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
                }

                with(data.control) {
                    isTapControl = taps
                    presenter.isSwipeControl.item = swipes
                    isKeyControl = keys
                }
            }
        }

        doFromSdk(Build.VERSION_CODES.LOLLIPOP) {
            val color = ContextCompat.getColor(this, R.color.transparent_dark)
            window.statusBarColor = color
            window.navigationBarColor = color
        }

        readTime = System.currentTimeMillis() // Старт отсчета времени чтения

        val point = Point() // Хранилище для данных экрана
        windowManager.defaultDisplay.getSize(point) // Сохранение данных в хранилище
        LEFT_PART_SCREEN = point.x / 3 // Установка данных
        RIGHT_PART_SCREEN = point.x * 2 / 3 // Установка данных
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Переключение слайдов с помощью клавиш громкости
        if (isKeyControl) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> presenter.nextPage()
                KeyEvent.KEYCODE_VOLUME_UP -> presenter.prevPage()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.id.viewer_menu_prev, R.string.viewer_page_prev_text).showAlways()
            .setIcon(R.drawable.ic_previous_white)
        menu.add(R.id.viewer_menu_next, R.string.viewer_page_next_text).showAlways()
            .setIcon(R.drawable.ic_next_white)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.viewer_menu_prev).isVisible = presenter.isPrev.item
        menu.findItem(R.id.viewer_menu_next).isVisible = presenter.isNext.item
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.viewer_menu_prev -> presenter.prevChapter()
            R.id.viewer_menu_next -> presenter.nextChapter()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        // Сохранение настроек
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER

        val time = (System.currentTimeMillis() - readTime) / 1000
        if (time > 0) {
            mViewModel.updateStatisticInfo(chapter.manga, time)
        }
    }
}
