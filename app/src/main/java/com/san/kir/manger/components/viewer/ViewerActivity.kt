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
import com.san.kir.ankofork.doFromSdk
import com.san.kir.ankofork.negative
import com.san.kir.ankofork.positive
import com.san.kir.ankofork.setContentView
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.add
import com.san.kir.manger.utils.extensions.boolean
import com.san.kir.manger.utils.extensions.showAlways
import com.san.kir.manger.utils.extensions.string
import com.san.kir.manger.utils.extensions.stringSet
import com.san.kir.manger.view_models.ViewerViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ViewerActivity : BaseActivity() {
    companion object { // константы для сохранения настроек
        var LEFT_PART_SCREEN = 0 // Левая часть экрана
        var RIGHT_PART_SCREEN = 0 // Правая часть экрана
    }

    val mViewModel by viewModels<ViewerViewModel>()

    private var showHide: Job? = Job()

    // Режимы листания страниц
    var isTapControl = false // Нажатия на экран
    private var isKeyControl = false // Кнопки громкости

    var chapter = Chapter()
    private var isAlternative = false

    val presenter by lazy { ViewerPresenter(this) }
    val mView by lazy { ViewerView(presenter) }

    private var readTime = 0L

    private val orientation by string(
        R.string.settings_viewer_orientation_key, R.string.settings_viewer_orientation_default
    )
    private val controlKey by stringSet(
        R.string.settings_viewer_control_key, R.array.settings_viewer_control_default
    )
    private var cutout by boolean(
        R.string.settings_viewer_cutout_key, R.string.settings_viewer_cutout_default
    )

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mView.setContentView(this) // Установка разметки

        doFromSdk(28) {
            window.attributes.layoutInDisplayCutoutMode =
                if (cutout) WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                else WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
        }

        setSupportActionBar(mView.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Кнопка назад в верхнем баре
        supportActionBar?.setShowHideAnimationEnabled(true) // Анимация скрытия, сокрытия

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                supportActionBar!!.show() // Показать бар сверху
                presenter.isBottomBar.positive()// Показать нижний бар
                showHide = lifecycleScope.launch {
                    delay(4000L)
                    hideSystemUI()
                }
            } else {
                supportActionBar!!.hide() //Скрыть бар сверху
                presenter.isBottomBar.negative() // Скрыть нижний бар
                showHide?.cancel()
                showHide = null
            }
        }
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
        requestedOrientation = when (orientation) {
            getString(R.string.settings_viewer_orientation_auto) -> SCREEN_ORIENTATION_SENSOR
            getString(R.string.settings_viewer_orientation_port) -> SCREEN_ORIENTATION_PORTRAIT
            getString(R.string.settings_viewer_orientation_port_rev) -> SCREEN_ORIENTATION_REVERSE_PORTRAIT
            getString(R.string.settings_viewer_orientation_land) -> SCREEN_ORIENTATION_LANDSCAPE
            getString(R.string.settings_viewer_orientation_land_rev) -> SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            getString(R.string.settings_viewer_orientation_auto_port) -> SCREEN_ORIENTATION_SENSOR_PORTRAIT
            getString(R.string.settings_viewer_orientation_auto_land) -> SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            else -> SCREEN_ORIENTATION_SENSOR
        }

        controlKey.forEach {
            when (it) {
                getString(R.string.settings_viewer_control_taps) -> isTapControl = true
                getString(R.string.settings_viewer_control_swipes) -> presenter.isSwipeControl.positive()
                getString(R.string.settings_viewer_control_keys) -> isKeyControl = true
            }
        }

        doFromSdk(Build.VERSION_CODES.LOLLIPOP) {
            val color = ContextCompat.getColor(this, R.color.transparent_dark)
            window.statusBarColor = color
            window.navigationBarColor = color
        }

        intent.apply {
            chapter = getParcelableExtra("chapter")
            isAlternative = getBooleanExtra("is", false)
        }

        title = chapter.name // Смена заголовка
        readTime = System.currentTimeMillis() // Старт отсчета времени чтения

        val point = Point() // Хранилище для данных экрана
        windowManager.defaultDisplay.getSize(point) // Сохранение данных в хранилище
        LEFT_PART_SCREEN = point.x / 3 // Установка данных
        RIGHT_PART_SCREEN = point.x * 2 / 3 // Установка данных

        presenter.configManager(chapter, isAlternative).invokeOnCompletion {
            presenter.isLoad.negative()
        }


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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
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
