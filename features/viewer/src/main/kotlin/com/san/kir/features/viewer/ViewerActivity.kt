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
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.viewpager.widget.ViewPager
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.data.models.main.Settings
import com.san.kir.data.models.utils.Orientation
import com.san.kir.features.viewer.databinding.MainBinding
import com.san.kir.features.viewer.logic.ErrorState
import com.san.kir.features.viewer.utils.Page
import com.san.kir.features.viewer.utils.VIEW_OFFSET
import com.san.kir.features.viewer.utils.setContainerColor
import com.san.kir.features.viewer.utils.setContentColor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

public object MangaViewer {
    public fun start(chapterID: Long, context: Context): Unit = ViewerActivity.start(chapterID, context)
}

internal class ViewerActivity : AppCompatActivity() {

    companion object {
        private const val CHAPTER_KEY = "chapter_key"

        fun start(chapterID: Long, context: Context) {
            val intent = Intent(context, ViewerActivity::class.java)
            intent.putExtra(CHAPTER_KEY, chapterID)
            context.startActivity(intent)
        }
    }

    private val binding by lazy { MainBinding.inflate(layoutInflater) }
    private val viewModel: ViewerViewModel by viewModels(factoryProducer = {
        viewModelFactory { initializer { ViewerViewModel() } }
    })
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
                    //                    Timber.v("onPageSelected with $position")
                    viewModel.chaptersManager.updatePagePosition(position)
                }
            }
        })

        binding.pager.offscreenPageLimit = 2

        binding.next.setOnClickListener { lifecycleScope.defaultLaunch { viewModel.chaptersManager.nextChapter() } }
        binding.prev.setOnClickListener { lifecycleScope.defaultLaunch { viewModel.chaptersManager.prevChapter() } }
        binding.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.reloadPages.setOnClickListener {
            lifecycleScope.defaultLaunch { viewModel.chaptersManager.updatePagesForCurrentChapter() }
            binding.reloadPages.isVisible = false
            binding.loaderText.setText(R.string.data_loading)
            binding.loader.isVisible = true
            binding.loaderContainer.isVisible = true
        }
    }

    override fun onResume() {
        super.onResume()
        // Загрузка настроек
        viewModel.settingsRepository.viewer.onEach { data: Settings.Viewer ->
            requestedOrientation = when (data.orientation) {
                Orientation.PORT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                Orientation.LAND -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                Orientation.AUTO -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
                Orientation.PORT_REV -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                Orientation.LAND_REV -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                Orientation.AUTO_PORT -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                Orientation.AUTO_LAND -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val attrs = window.attributes
                attrs.layoutInDisplayCutoutMode =
                    if (data.cutOut) WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    else WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
                window.attributes = attrs
            }

            // переключение управления свайпами
            binding.pager.setSwipable(data.control.swipes)
        }.launchIn(lifecycleScope)

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
                    KeyEvent.KEYCODE_VOLUME_UP -> viewModel.chaptersManager.prevPage()
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun runStateChangeListener() {
        viewModel.chaptersManager.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .onEach { state ->
                Timber.i("state -> $state")

                // ---
                binding.loaderText.text = when (val error = state.error) {
                    is ErrorState.AuthError -> getString(R.string.auth_error_loading, error.catalogName)
                    is ErrorState.BaseError -> getString(R.string.error_loading, error.text)
                    is ErrorState.NotFoundError -> getString(R.string.not_found_error_loading)
                    ErrorState.None -> getString(R.string.data_loading)
                }
                binding.loader.isVisible = state.error is ErrorState.None
                binding.reloadPages.isVisible = state.error !is ErrorState.None

                // ---
                binding.loaderContainer.isVisible = state.pages.isEmpty()

                if (state.error !is ErrorState.None || state.pages.isEmpty()) {
                    viewModel.toggleVisibilityUI(true, true)
                } else {
                    viewModel.toggleVisibilityUI(viewModel.visibleUI.value.isShown)
                }

                binding.pager.isVisible = state.pages.isNotEmpty()
                if (binding.pager.isVisible) {
                    // Обновление адаптера
                    adapter.setList(state.pages)
                    // установка страницы ViewPager
                    if (binding.pager.currentItem != state.pagePosition) {
                        // Timber.v("pagePosition is ${state.pagePosition}")
                        binding.pager.currentItem = maxOf(0, state.pagePosition)
                    }
                }

                // Проверка видимости кнопок переключения глав
                binding.prev.isEnabled = state.pages.firstOrNull() is Page.Prev
                binding.prev.isVisible = state.pages.firstOrNull() is Page.Prev
                binding.next.isEnabled = state.pages.lastOrNull() is Page.Next
                binding.next.isVisible = state.pages.lastOrNull() is Page.Next

                // Обновление статуса прочитанных страниц
                binding.pagesText.isVisible = state.pagePosition >= 0 && state.pages.isNotEmpty()
                binding.pagesText.text = getString(R.string.viewer_pages_text, state.pagePosition, state.pages.size)

                // обновление прогрессбара
                binding.progressBar.isVisible = state.pagePosition >= 0 && state.pages.isNotEmpty()
                binding.progressBar.max = state.pages.size - 1
                binding.progressBar.progress = state.pagePosition

                // статуса прочитанных глав
                binding.chaptersText.isVisible = state.chapters.isNotEmpty()
                binding.chaptersText.text = getString(
                    R.string.viewer_chapters_text, state.uiChapterPosition, state.chapters.size
                )

                // Обновление заголовка
                binding.title.text = state.currentChapter.name

                // Обновление цветов
                if (state.color != 0) {
                    binding.apply {
                        setContainerColor(state.color, prev, next, appbar, reloadPages)
                        setContentColor(
                            state.color, prev, next, back, title, stopwatch, chaptersText, pagesText, reloadPages
                        )
                    }
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun runUIVisibleChangeListener() {
        var visibleJob: Job? = null
        // Переключение видимости элементов
        viewModel.visibleUI
            .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .onEach { (state, force) ->
                if (state) {
                    visibleJob?.cancel()

                    if (force) {
                        showUI()
                    } else {
                        visibleJob = lifecycleScope.launch {
                            showUI()
                            delay(4.seconds)
                            viewModel.toggleVisibilityUI(false, false)
                        }
                    }
                } else {
                    visibleJob?.cancel()
                    hideUI()
                }
            }.launchIn(lifecycleScope)
    }

    private fun initData() {
        // получение данных и инициализация менеджера
        val id = intent.getLongExtra(CHAPTER_KEY, -1L)
        if (id != -1L) viewModel.init(id)
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
                || insets.isVisible(WindowInsetsCompat.Type.statusBars())
            ) {
                hideSystemUI()
            }

            insets
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun showUI() {
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.appbar, View.TRANSLATION_Y, binding.appbar.translationY, 0f)
                    .apply { doOnStart { binding.progressBar.isVisible = false } },
                ObjectAnimator.ofFloat(binding.prev, View.TRANSLATION_Y, binding.prev.translationY, 0f),
                ObjectAnimator.ofFloat(binding.prev, View.TRANSLATION_X, binding.prev.translationX, 0f),
                ObjectAnimator.ofFloat(binding.next, View.TRANSLATION_Y, binding.next.translationY, 0f),
                ObjectAnimator.ofFloat(binding.next, View.TRANSLATION_X, binding.next.translationX, 0f),
            )
            start()
        }
    }

    private fun hideUI() {
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.appbar, View.TRANSLATION_Y, binding.appbar.translationY, -VIEW_OFFSET)
                    .apply { doOnEnd { binding.progressBar.isVisible = true } },
                ObjectAnimator.ofFloat(binding.prev, View.TRANSLATION_Y, binding.prev.translationY, VIEW_OFFSET),
                ObjectAnimator.ofFloat(binding.prev, View.TRANSLATION_X, binding.prev.translationX, -VIEW_OFFSET),
                ObjectAnimator.ofFloat(binding.next, View.TRANSLATION_Y, binding.next.translationY, VIEW_OFFSET),
                ObjectAnimator.ofFloat(binding.next, View.TRANSLATION_X, binding.next.translationX, VIEW_OFFSET),
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

