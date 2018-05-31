package com.san.kir.manger.components.viewer

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
import com.san.kir.manger.R
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.eventBus.negative
import com.san.kir.manger.eventBus.positive
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.extending.ankoExtend.radioButton
import com.san.kir.manger.extending.views.showAlways
import com.san.kir.manger.utils.log
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
import kotlin.properties.Delegates.observable

class ViewerActivity : BaseActivity() {
    companion object { // константы для сохранения настроек
        private const val SAVE_IS_SHOW_BAR = "isShowBar"
        private const val ORIENTATION = "orientation"
        private const val CONTROL_TAP = "tap"
        private const val CONTROL_SWIPE = "swipe"
        private const val CONTROL_KEY = "key"

        var LEFT_PART_SCREEN = 0 // Левая часть экрана
        var RIGHT_PART_SCREEN = 0 // Правая часть экрана
    }

    val chapters = Main.db.chapterDao

    private var timer: Timer? = null // Таймер
    var isBar by observable(true) { _, old, new ->
        if (old != new) {
            if (timer == null) // Если таймера нет
                timer = Timer() // создать

            if (!new) {
                supportActionBar!!.hide() //Скрыть бар сверху
                presenter.isBottomBar.negative() // Скрыть нижний бар
                timer?.cancel() // Отменить таймер
                timer = null // Убрать таймер
            } else {
                supportActionBar!!.show() // Показать бар сверху
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        presenter.isBottomBar.positive()// Показать нижний бар
                    }
                }, 300) // после 3 секунд
            }
        }
    } // Отображение обоих баров

    val adapter: Binder<ViewerPageAdapter?> = Binder(null) // Адаптер для читалки

    // Режимы листания страниц
    var isTapControl = false // Нажатия на экран
    private var isKeyControl = false // Кнопки громкости

    private var mangaName = ""
    var chapterName = ""

    val presenter by lazy { ViewPagePresenter(this) }
    private val view by lazy { ViewerView(presenter) }

    /* Перезаписанные функции */
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getSharedPreferences(sPrefViewer, MODE_PRIVATE).apply {
            if (contains(ORIENTATION)) {
                requestedOrientation = getInt(ORIENTATION, SCREEN_ORIENTATION_SENSOR)
            }
        }

        view.setContentView(this) // Установка разметки

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Кнопка назад в верхнем баре
        supportActionBar?.setShowHideAnimationEnabled(true) // Анимация скрытия, сокрытия

        intent.apply {
            mangaName = getStringExtra("manga_name")
            chapterName = getStringExtra("chapter")
        }

        title = chapterName // Смена заголвка
    }

    override fun onResume() {
        super.onResume()

        val point = Point() // Хранилище для данных экрана
        windowManager.defaultDisplay.getSize(point) // Сохранение данных в хранилище
        LEFT_PART_SCREEN = point.x / 3 // Установка данных
        RIGHT_PART_SCREEN = point.x * 2 / 3 // Установка данных

        // Загрузка настроек
        getSharedPreferences(sPrefViewer, MODE_PRIVATE).apply {
            if (contains(ORIENTATION)) {
                isBar = getBoolean(SAVE_IS_SHOW_BAR, true)
                isTapControl = getBoolean(CONTROL_TAP, false)
                isKeyControl = getBoolean(CONTROL_KEY, false)
                presenter.isSwipeControl.item = getBoolean(CONTROL_SWIPE, true)
            }
        }

        log("создание ")
        presenter.configManager(mangaName, chapterName)
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
                KeyEvent.KEYCODE_VOLUME_DOWN -> presenter.nextPage()
                KeyEvent.KEYCODE_VOLUME_UP -> presenter.prevPage()
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
                putBoolean(SAVE_IS_SHOW_BAR, presenter.isBottomBar.item)
                putBoolean(CONTROL_TAP, isTapControl)
                putBoolean(CONTROL_SWIPE, presenter.isSwipeControl.item)
                putBoolean(CONTROL_KEY, isKeyControl)
            }.apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.close()
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
                        textView(text = R.string.viewer_menu_orientation).lparams {
                            gravity = Gravity.CENTER
                        }

                        // Портретная
                        radioButton(id = portN, text = R.string.viewer_menu_orientation_portrait) {
                            isChecked = requestedOrientation == SCREEN_ORIENTATION_PORTRAIT
                        }

                        // Портретная обратная
                        radioButton(
                            id = portR,
                            text = R.string.viewer_menu_orientation_portrait_reverse
                        ) {
                            isChecked = requestedOrientation == SCREEN_ORIENTATION_REVERSE_PORTRAIT
                        }

                        // Ландшафтная
                        radioButton(
                            id = landN,
                            text = R.string.viewer_menu_orientation_landscape
                        ) {
                            isChecked = requestedOrientation == SCREEN_ORIENTATION_LANDSCAPE
                        }

                        // Ландшафтная обратная
                        radioButton(
                            id = landR,
                            text = R.string.viewer_menu_orientation_landscape_reverse
                        ) {
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
                        textView(text = R.string.viewer_menu_control).lparams {
                            gravity = Gravity.CENTER
                        }

                        // Тапы по экрану
                        checkBox(text = R.string.viewer_menu_is_tap) {
                            isChecked = isTapControl
                            onCheckedChange { _, b -> isTapControl = b }
                        }

                        // Свайпы по экрану
                        checkBox(text = R.string.viewer_menu_is_swipe) {
                            isChecked = presenter.isSwipeControl.item
                            onCheckedChange { _, b -> presenter.isSwipeControl.item = b }
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
}
