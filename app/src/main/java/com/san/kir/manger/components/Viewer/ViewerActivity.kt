package com.san.kir.manger.components.Viewer

import android.R.id
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR
import android.graphics.Point
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.Extending.AnkoExtend.radioButton
import com.san.kir.manger.Extending.BaseActivity
import com.san.kir.manger.Extending.Views.showAlways
import com.san.kir.manger.R
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.utils.isFirstRun
import com.san.kir.manger.utils.sPrefViewer
import org.jetbrains.anko.alert
import org.jetbrains.anko.checkBox
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.radioGroup
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import java.util.*
import kotlin.properties.Delegates.notNull
import kotlin.properties.Delegates.observable

class ViewerActivity : BaseActivity() {
    companion object { // константы для сохранения настроек
        private const val SAVE_IS_SHOW_BAR = "isShowBar"
        private const val ORIENTATION = "orientation"
        private const val CONTROL_TAP = "tap"
        private const val CONTROL_SWIPE = "swipe"
        private const val CONTROL_KEY = "key"

        private var MANGA = "mangaName"
        private var CHAPTER = "chapterName"
        private var PAGE = "pagePostition"
    }

    val chapters = Main.db.chapterDao

    val progress = Binder(0) // Текущая страница, которую в данный момент читают
    val isBottomBar = Binder(true) // Отображение нижнего бара
    var timer: Timer? = null // Таймер
    var isBar by observable(true) { _, old, new ->
        if (old != new) {
            if (timer == null) // Если таймера нет
                timer = Timer() // создать

            if (!new) {
                supportActionBar!!.hide() //Скрыть бар сверху
                isBottomBar.item = false // Скрыть нижний бар
                timer?.cancel() // Отменить таймер
                timer = null // Убрать таймер
            } else {
                supportActionBar!!.show() // Показать бар сверху
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        isBottomBar.item = true // Показать нижний бар
                    }
                }, 300) // после 3 секунд
            }
        }
    } // Отображение обоих баров


    val isNext = Binder(true) // Есть ли следующая глава
    val isPrev = Binder(true) // Есть ли предыдущая глава

    val adapter: Binder<ViewerPageAdapter?> = Binder(null) // Адаптер для читалки

    var LEFT_PART_SCREEN = 0 // Левая часть экрана
    var RIGHT_PART_SCREEN = 0 // Правая часть экрана

    // Режимы листания страниц
    var isTapControl = false // Нажатия на экран
    var isSwipeControl = Binder(true) // Свайпы
    private var isKeyControl = false // Кнопки громкости

    private var CHAPTERS: ChaptersList by notNull() // Менеджер глав и страниц

    private var mangaName = ""
    private var chapterName = ""
    private var pagePostition = -1

    private val view = ViewerView(this)

    /* Перезаписанные функции */
    @SuppressLint("MissingSuperCall", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view.setContentView(this) // Установка разметки

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Кнопка назад в верхнем баре
        supportActionBar?.setShowHideAnimationEnabled(true) // Анимация скрытия, сокрытия

        getSharedPreferences(sPrefViewer, MODE_PRIVATE).apply {
            if (contains(MANGA)) mangaName = getString(MANGA, "")
            if (contains(CHAPTER)) chapterName = getString(CHAPTER, "")
            if (contains(PAGE)) pagePostition = getInt(PAGE, -1)
        }

        if (isFirstRun) // Проверка на уникальность запуска
            intent.apply {
                //            log = getStringExtra("manga_name")
//            log = getStringExtra("chapter")
//            log = getIntExtra("page_position", 0).toString()
                mangaName = getStringExtra("manga_name")
                chapterName = getStringExtra("chapter")
                pagePostition = getIntExtra("page_position", 0)
                isFirstRun = false
            }

        title = chapterName // Смена заголвка

        // Создание менеджера
        CHAPTERS = ChaptersList(mangaName, chapterName, pagePostition, this)

    }

    override fun onResume() {
        super.onResume()

        val point = Point() // Хранилище для данных экрана
        windowManager.defaultDisplay.getSize(point) // Сохранение данных в хранилище
        LEFT_PART_SCREEN = point.x / 3 // Установка данных
        RIGHT_PART_SCREEN = point.x * 2 / 3 // Установка данных

        adapter.item = ViewerPageAdapter(supportFragmentManager,
                                         CHAPTERS.page.list) // Создание адаптера
        // Загрузка настроек
        getSharedPreferences(sPrefViewer, MODE_PRIVATE).apply {
            if (contains(ORIENTATION)) {
                requestedOrientation = getInt(ORIENTATION, SCREEN_ORIENTATION_SENSOR)
                isBar = getBoolean(SAVE_IS_SHOW_BAR, true)
                isTapControl = getBoolean(CONTROL_TAP, false)
                isKeyControl = getBoolean(CONTROL_KEY, false)
                isSwipeControl.item = getBoolean(CONTROL_SWIPE, true)
            }
        }

        view.max.item = CHAPTERS.page.max // Установка значения
        progress.item =
                if (CHAPTERS.page.position <= 0) 1 // Если полученная позиция не больше нуля, то присвоить значение 1
                else CHAPTERS.page.position // Иначе то что есть

        // При изменении прогресса, отдать новое значение в менеджер
        progress.bind { pos -> CHAPTERS.page.position = pos }

        view.maxChapters = CHAPTERS.chapter.max // Установка значения
        view.progressChapters.item = CHAPTERS.chapter.position // Установка значения

        checkButton() // проверка и установка видимости
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Установка режима во весь экрана без верхней строки и навигационных кнопок
        if (hasFocus) { // Срабатывает только если был получен фокус
                window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Переключение слайдов с помощью клавиш громкости
        if (isKeyControl) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> progress.item += 1
                KeyEvent.KEYCODE_VOLUME_UP -> progress.item -= 1
            }
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            openMenu()
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Кнопка для включения своего меню
        menu.add(0, 0, 0, "").setIcon(R.drawable.dots_vertical).showAlways()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            id.home -> onBackPressed()
            0 -> openMenu()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        // Сохранение настроек
        getSharedPreferences(sPrefViewer, MODE_PRIVATE).apply {
            edit().apply {
                putInt(ORIENTATION, requestedOrientation)
                putBoolean(SAVE_IS_SHOW_BAR, isBottomBar.item)
                putBoolean(CONTROL_TAP, isTapControl)
                putBoolean(CONTROL_SWIPE, isSwipeControl.item)
                putBoolean(CONTROL_KEY, isKeyControl)

                putString(MANGA, mangaName)
                putString(CHAPTER, chapterName)
                putInt(PAGE, progress.item)
            }.apply()
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onDestroy() {
        super.onDestroy()
        adapter.close()
    }

    /* Функции */
    // Предыдущая глава
    fun prevChapter() {
        CHAPTERS.chapter.prev // Переключение главы
        initChapter()
    }

    // Следующая глава
    fun nextChapter() {
        CHAPTERS.chapter.next // Переключение главы
        initChapter()
    }

    /* Приватные функции */
    private fun initChapter() {
        adapter.item = ViewerPageAdapter(supportFragmentManager, CHAPTERS.page.list)
        progress.item = 1
        view.max.item = CHAPTERS.page.max
        view.progressChapters.item = CHAPTERS.chapter.position
        checkButton()
        chapterName = CHAPTERS.chapter.current.name // Сохранение данных
        title = chapterName // Смена заголовка
    }

    // Меню для упраиления настройками
    private fun openMenu() {
        // id элементов меню
        val portN = 0
        val portR = 1
        val landN = 2
        val landR = 3
        val auto = 4

        var orientation = requestedOrientation // Сохраняем значение ориентации
        alert {
            customView {
                // Свое оформление для диалога
                linearLayout {
                    lparams(width = matchParent, height = matchParent)
                    padding = dip(4)

                    radioGroup {
                        lparams(width = matchParent) { weight = 1f }

                        // Заголовок
                        textView(text = R.string.viewer_menu_orientation).lparams { gravity = Gravity.CENTER }

                        // Портретная
                        radioButton(id = portN, text = R.string.viewer_menu_orientation_portrait) {
                            isChecked = requestedOrientation == SCREEN_ORIENTATION_PORTRAIT
                        }

                        // Портретная обратная
                        radioButton(id = portR,
                                    text = R.string.viewer_menu_orientation_portrait_reverse) {
                            isChecked = requestedOrientation == SCREEN_ORIENTATION_REVERSE_PORTRAIT
                        }

                        // Ландшафтная
                        radioButton(id = landN,
                                    text = R.string.viewer_menu_orientation_landscape) {
                            isChecked = requestedOrientation == SCREEN_ORIENTATION_LANDSCAPE
                        }

                        // Ландшафтная обратная
                        radioButton(id = landR,
                                    text = R.string.viewer_menu_orientation_landscape_reverse) {
                            isChecked = requestedOrientation == SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        }

                        // Автоматическая
                        radioButton(id = auto, text = R.string.viewer_menu_orientation_auto) {
                            isChecked = requestedOrientation == SCREEN_ORIENTATION_SENSOR
                        }

                        // Действия на изменение выбора
                        onCheckedChange { _, i ->
                            orientation = when (i) { // Присвоение выбора в переменную
                                portN -> SCREEN_ORIENTATION_PORTRAIT
                                portR -> SCREEN_ORIENTATION_REVERSE_PORTRAIT
                                landN -> SCREEN_ORIENTATION_LANDSCAPE
                                landR -> SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                                auto -> SCREEN_ORIENTATION_SENSOR
                                else -> SCREEN_ORIENTATION_SENSOR
                            }
                        }
                    }

                    verticalLayout {
                        lparams(width = matchParent) { weight = 1f }

                        // Заголовок
                        textView(text = R.string.viewer_menu_control).lparams { gravity = Gravity.CENTER }

                        // Тапы по экрану
                        checkBox(text = R.string.viewer_menu_is_tap) {
                            isChecked = isTapControl
                            onCheckedChange { _, b -> isTapControl = b }
                        }

                        // Свайпы по экрану
                        checkBox(text = R.string.viewer_menu_is_swipe) {
                            isChecked = isSwipeControl.item
                            onCheckedChange { _, b -> isSwipeControl.item = b }
                        }

                        // Кнопки громкости
                        checkBox(text = R.string.viewer_menu_is_key) {
                            isChecked = isKeyControl
                            onCheckedChange { _, b -> isKeyControl = b }
                        }
                    }
                }
            }
            positiveButton("Применить") {
                requestedOrientation = orientation
            }
            negativeButton("Отменить") {}

        }.show()
    }

    // Проверка видимости кнопок переключения глав
    private fun checkButton() {
        isPrev.item = CHAPTERS.page.list.first().name == "prev"
        isNext.item = CHAPTERS.page.list.last().name == "next"
    }
}
