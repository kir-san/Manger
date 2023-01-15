package com.san.kir.features.viewer

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.data.models.base.Settings
import com.san.kir.features.viewer.databinding.MainBinding
import com.san.kir.features.viewer.utils.Page
import com.san.kir.features.viewer.utils.VIEW_OFFSET
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

object MangaViewer {
    fun start(
        context: Context,
        chapterID: Long,
    ) {
        ViewerActivity.start(context, chapterID)
    }
}

@AndroidEntryPoint
internal class ViewerActivity : AppCompatActivity() {

    companion object {
        private const val chapterKey = "chapter_key"

        fun start(
            context: Context,
            chapterID: Long,
        ) {
            val intent = Intent(context, ViewerActivity::class.java)
            intent.putExtra(chapterKey, chapterID)
            context.startActivity(intent)
        }
    }

    private val binding by lazy { MainBinding.inflate(layoutInflater) }
    private val viewModel: ViewerViewModel by viewModels()
    private val adapter by lazy { Adapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        autoHideSystemUI()
        /*
        Используется ViewPager первой версии
        из-за невозможности использовать вторую версию,
        так как в ней были выявлены следующие проблемы:
        - обновление адаптера происходило очень криво, элементы распологалась не в том порядке, что
            передавал адаптеру
        - onPageChangeListener отрабатывал после инициализации данных, чем нарушал логику
            и изменял установленный прогресс
        Попытки костылестроения не увенчались успехом. НЕ ПЫТАТЬСЯ ИСПОЛЬЗОВАТЬ VIEWPAGER2
        */

        binding.pager.adapter = adapter
        binding.pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                lifecycleScope.defaultLaunch {
                    Timber.v("onPageSelected with $position")
                    viewModel.chaptersManager.updatePagePosition(position)
                }
            }
        })

        binding.pager.offscreenPageLimit = 2

        binding.next.setOnClickListener { // Следующая глава
            lifecycleScope.defaultLaunch { viewModel.chaptersManager.nextChapter() }
        }
        binding.prev.setOnClickListener { // Предыдущая глава
            lifecycleScope.defaultLaunch { viewModel.chaptersManager.prevChapter() }
        }
        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        // Загрузка настроек
        lifecycleScope.launchWhenResumed {
            viewModel.settingsRepository.viewer().collect { data: Settings.Viewer ->
                requestedOrientation = when (data.orientation) {
                    Settings.Viewer.Orientation.PORT      -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    Settings.Viewer.Orientation.LAND      -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    Settings.Viewer.Orientation.AUTO      -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    Settings.Viewer.Orientation.PORT_REV  -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    Settings.Viewer.Orientation.LAND_REV  -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    Settings.Viewer.Orientation.AUTO_PORT -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                    Settings.Viewer.Orientation.AUTO_LAND -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    else                                  -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }

                if (Build.VERSION.SDK_INT >= 28) {
                    val attrs = window.attributes
                    attrs.layoutInDisplayCutoutMode =
                        if (data.cutOut) WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        else WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
                    window.attributes = attrs
                }

                // переключение управления свайпами
                binding.pager.setSwipable(data.control.swipes)
            }
        }

        viewModel.initReadTime()
        viewModel.setScreenWidth(getScreenWidth())

        runStateChangeListener()

        runUIVisibleChangeListener()

        initData()

        runStopWatch()
    }

    override fun onPause() {
        super.onPause()
        // Сохранение настроек
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        binding.pager.clearOnPageChangeListeners()
        binding.prev.setOnClickListener(null)
        binding.next.setOnClickListener(null)

        viewModel.setReadTime()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Переключение слайдов с помощью клавиш громкости
        if (viewModel.control.value.keys) {
            lifecycleScope.launch {
                when (keyCode) {
                    KeyEvent.KEYCODE_VOLUME_DOWN -> viewModel.chaptersManager.nextPage()
                    KeyEvent.KEYCODE_VOLUME_UP   -> viewModel.chaptersManager.prevPage()
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun runStateChangeListener() {
        viewModel.chaptersManager.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .onEach { state ->
               // Timber.i("state -> $state")
                if (state.pages.isNotEmpty()) {
                    // Обновление адаптера
                    adapter.setList(state.pages)

                    // установка страницы ViewPager
                    if (binding.pager.currentItem != state.pagePosition) {
                        Timber.v("pagePosition is ${state.pagePosition}")
                        binding.pager.currentItem = maxOf(0, state.pagePosition)
                    }

                    // обновление прогрессбара
                    binding.progressBar.max = state.pages.size - 1
                    binding.progressBar.progress = state.pagePosition

                    // Проверка видимости кнопок переключения глав
                    binding.prev.isEnabled = state.pages.first() is Page.Prev
                    binding.prev.isVisible = state.pages.first() is Page.Prev
                    binding.next.isEnabled = state.pages.last() is Page.Next
                    binding.next.isVisible = state.pages.last() is Page.Next

                    // Обновление статуса прочитанных страниц
                    binding.pagesText.text = getString(
                        R.string.viewer_pages_text, state.pagePosition, state.pages.size
                    )
                }

                // статуса прочитанных глав
                if (state.chapters.isNotEmpty()) {
                    binding.chaptersText.text = getString(
                        R.string.viewer_chapters_text, state.uiChapterPosition, state.chapters.size
                    )
                }

                // Обновление заголовка
                binding.title.text = state.currentChapter.name
            }
            .launchIn(lifecycleScope)
    }

    private fun runUIVisibleChangeListener() {
        var visibleJob: Job? = null
        // Переключение видимости элементов
        viewModel.visibleUI
            .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .onEach { state ->
                if (state) {
                    visibleJob?.cancel()
                    visibleJob = lifecycleScope.launch {
                        showUI()
                        delay(4.seconds)
                        viewModel.toggleVisibilityUI(false)
                    }
                } else {
                    visibleJob?.cancel()
                    hideUI()
                }
            }.launchIn(lifecycleScope)
    }

    private fun initData() {
        // получение данных и инициализация менеджера
        val id = intent.getLongExtra(chapterKey, -1L)
        if (id != -1L) {
            viewModel.init(chapterId = id)
        }
    }

    private fun runStopWatch() {
        viewModel.getStopWatch()
            .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .onEach { binding.stopwatch.text = getString(R.string.stopwatch, it) }
            .launchIn(lifecycleScope)
    }

    private fun autoHideSystemUI() {
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            if (insets.isVisible(WindowInsetsCompat.Type.navigationBars())
                || insets.isVisible(WindowInsetsCompat.Type.statusBars())) {
                Timber.i("autoHideSystemUI")
                hideSystemUI()
            }

            insets
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun showUI() {
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.appbar, View.TRANSLATION_Y, -VIEW_OFFSET, 0f).apply {
                    doOnStart { binding.progressBar.isVisible = false }
                },
                ObjectAnimator.ofFloat(binding.prev, View.TRANSLATION_Y, VIEW_OFFSET, 0f),
                ObjectAnimator.ofFloat(binding.prev, View.TRANSLATION_X, -VIEW_OFFSET, 0f),
                ObjectAnimator.ofFloat(binding.next, View.TRANSLATION_Y, VIEW_OFFSET, 0f),
                ObjectAnimator.ofFloat(binding.next, View.TRANSLATION_X, VIEW_OFFSET, 0f),
            )
            start()
        }
    }

    private fun hideUI() {
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.appbar, View.TRANSLATION_Y, 0f, -VIEW_OFFSET).apply {
                    doOnEnd { binding.progressBar.isVisible = true }
                },
                ObjectAnimator.ofFloat(binding.prev, View.TRANSLATION_Y, 0f, VIEW_OFFSET),
                ObjectAnimator.ofFloat(binding.prev, View.TRANSLATION_X, 0f, -VIEW_OFFSET),
                ObjectAnimator.ofFloat(binding.next, View.TRANSLATION_Y, 0f, VIEW_OFFSET),
                ObjectAnimator.ofFloat(binding.next, View.TRANSLATION_X, 0f, VIEW_OFFSET),
            )
            start()
        }
    }

    @Suppress("DEPRECATION")
    private fun getScreenWidth(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            windowMetrics.bounds.width()
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }
}

