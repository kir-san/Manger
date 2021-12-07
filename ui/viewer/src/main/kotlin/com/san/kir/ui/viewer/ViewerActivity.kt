package com.san.kir.ui.viewer

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListenerAdapter
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.san.kir.core.utils.log
import com.san.kir.data.models.Viewer
import com.san.kir.ui.viewer.databinding.MainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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
    private val pageListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            viewModel.chaptersManager.updatePagePosition(position)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        lifecycleScope.launch {
            val chapter = viewModel.savedChapter.get()
            if (chapter != null && chapter.id > 0) {
                viewModel.init(chapterId = chapter.id)
            } else {
                val id = intent.getLongExtra(chapterKey, -1L)
                if (id != -1L) {
                    viewModel.init(chapterId = id)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Загрузка настроек
        lifecycleScope.launchWhenResumed {
            viewModel.store.data.collect { data: Viewer ->
                requestedOrientation = when (data.orientation) {
                    Viewer.Orientation.PORT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    Viewer.Orientation.LAND -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    Viewer.Orientation.AUTO -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    Viewer.Orientation.PORT_REV -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    Viewer.Orientation.LAND_REV -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    Viewer.Orientation.AUTO_PORT -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                    Viewer.Orientation.AUTO_LAND -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }

                if (Build.VERSION.SDK_INT >= 28) {
                    window.attributes.layoutInDisplayCutoutMode =
                        if (data.cutOut) WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        else WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
                }
            }
        }

        viewModel.initReadTime()
        viewModel.setScreenWidth(getScreenWidth())

        initUI()
    }

    override fun onPause() {
        super.onPause()
        // Сохранение настроек
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
        binding.pager.unregisterOnPageChangeCallback(pageListener)
        binding.prev.setOnClickListener(null)
        binding.next.setOnClickListener(null)

// TODO        viewModel.setReadTime()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
//        else showSystemUI()
    }

    override fun onBackPressed() {
        viewModel.savedChapter.clear()
        super.onBackPressed()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Переключение слайдов с помощью клавиш громкости
        if (viewModel.control.value.keys) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> viewModel.chaptersManager.nextPage()
                KeyEvent.KEYCODE_VOLUME_UP -> viewModel.chaptersManager.prevPage()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun initUI() {
        hideSystemUI()

        if (Build.VERSION.SDK_INT >= 28) {
            val color = ContextCompat.getColor(this, R.color.transparent_dark)
            window.statusBarColor = color
            window.navigationBarColor = color
        }

        binding.pager.adapter = adapter
        binding.pager.registerOnPageChangeCallback(pageListener)
        binding.pager.offscreenPageLimit = 1

        binding.next.setOnClickListener { // Следующая глава
            lifecycleScope.launch { viewModel.chaptersManager.nextChapter() }
        }
        binding.prev.setOnClickListener { // Предыдущая глава
            lifecycleScope.launch { viewModel.chaptersManager.prevChapter() }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.appbar) { v, insets ->
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                topMargin = systemBars.top
                // Получаем размер выреза, если есть
                val cutoutRight = insets.displayCutout?.safeInsetRight ?: 0
                val cutoutLeft = insets.displayCutout?.safeInsetLeft ?: 0
                // Вычитаем из WindowInsets размер выреза, для fullscreen
                rightMargin = systemBars.right - cutoutRight
                leftMargin = systemBars.left - cutoutLeft
            }
            insets
        }

        lifecycleScope.launchWhenResumed {
            val manager = viewModel.chaptersManager
            val progressBar = binding.progressBar
            val pager = binding.pager

            combine(manager.pages, manager.pagePosition) { list, i ->
                if (list.isNotEmpty()) {
                    // обновление прогрессбара
                    progressBar.max = list.size
                    progressBar.progress = i

                    // установка страницы ViewPager
                    if (pager.currentItem != i)
                        pager.setCurrentItem(if (i < 0) 0 else i, true)

                    // Проверка видимости кнопок переключения глав
                    binding.prev.isVisible = list.first() is Page.Prev
                    binding.next.isVisible = list.last() is Page.Next

                    // Обновление статуса прочитанных страниц
                    binding.pagesText.text = getString(
                        R.string.viewer_pages_text, i, list.size
                    )
                }
            }.launchIn(this)

            // статуса прочитанных глав
            combine(manager.chapters, manager.chapterPosition) { list, i ->
                if (list.isNotEmpty()) {
                    binding.chaptersText.text = getString(
                        R.string.viewer_chapters_text, i, list.size
                    )
                }
            }.launchIn(this)

            // переключение управления свайпами
            viewModel.control
                .onEach { control ->
                    binding.pager.isUserInputEnabled = control.swipes
                }.launchIn(this)

            // Обновление списка страниц
            manager.pages.onEach { list -> adapter.updateItems(list) }.launchIn(this)

            manager.currentChapter
                .onEach { chapter ->
                    // Обновление заголовка
                    binding.title.text = chapter.name
                    // сохранение статуса текущей главы
                    viewModel.savedChapter.set(chapter)
                }
                .launchIn(this)

            viewModel.visibleUI.onEach { state ->
                if (state) {
                    showUI()
                    delay(4000L)
                    viewModel.toogleVisibilityUI(false)
                } else {
                    hideUI()
                }
            }.launchIn(this)
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

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView)
            .show(WindowInsetsCompat.Type.systemBars())
    }

    private fun showUI() {
        ViewCompat.animate(binding.appbar)
            .setDuration(200L)
            .translationY(0f)
            .setInterpolator(DecelerateInterpolator())
            .setListener(object : ViewPropertyAnimatorListenerAdapter() {
                override fun onAnimationStart(view: View?) {
                    view?.isVisible = true
                }
            })
    }

    private fun hideUI() {
        ViewCompat.animate(binding.appbar)
            .setDuration(200)
            .translationYBy(-200f)
            .setInterpolator(DecelerateInterpolator())
            .setListener(object : ViewPropertyAnimatorListenerAdapter() {
                override fun onAnimationEnd(view: View?) {
                    view?.isVisible = false
                }
            })
            .start()
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
