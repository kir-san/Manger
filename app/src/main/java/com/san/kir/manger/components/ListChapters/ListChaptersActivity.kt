package com.san.kir.manger.components.ListChapters

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.san.kir.manger.R
import com.san.kir.manger.components.ChaptersDownloader.ChaptersDownloader
import com.san.kir.manger.dbflow.wrapers.MangaWrapper
import com.san.kir.manger.utils.CHAPTER_STATUS
import com.san.kir.manger.utils.MangaUpdater
import com.san.kir.manger.utils.sPrefListChapters
import com.san.kir.manger.utils.showAlways
import com.san.kir.manger.utils.showIfRoom
import com.san.kir.manger.utils.showNever
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.toast

class ListChaptersActivity : AppCompatActivity(), ActionMode.Callback {

    companion object {
        private const val filterStatus = "filterStatus"
        private const val sortStatus = "sortStatus"
    }

    // получение манги по уникальному имени
    private val manga by lazy { MangaWrapper.get(intent.getStringExtra("manga_unic")) }

    var actionMode: ActionMode? = null

    private val view = ListChapterView(this)

    private lateinit var adapter: ListChaptersAdapter

    /* Перезаписанные функции */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view.setContentView(this) // Загружаем разметку

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = manga!!.name // Меняем заголовок

        adapter = ListChaptersAdapter(this, manga!!)
        view.adapter.item = adapter // Присваиваем наш адаптер

        MangaUpdater.bus.register() // Подписка на обновление манги
        ChaptersDownloader.bus.register(2) // Подписка на загрузчик глав
    }

    override fun onResume() {
        super.onResume()
        // Загрузка настроек
        getSharedPreferences(sPrefListChapters, MODE_PRIVATE).apply {
            if (contains(sortStatus)) {// порядок сортировки
                view.sortIndicator.item = getBoolean(sortStatus, false)
                // Обновить список после загрузки параметра из настроек
                adapter.update(!view.sortIndicator.item)
            }
            if (contains(filterStatus))
                view.filterIndicator = getInt(filterStatus,
                                              ListChaptersAdapter.ALL_READ) // тип фильтрации
        }

        // Если в данный момент ведется поиск новых глав для данной манги
        if (MangaUpdater.contains(manga!!))
            view.isVisibleProgress.item = true // Показать прогрессБар

        // Реакция на сообщения от поиска новых глав
        MangaUpdater.bus.onEvent { (manga, isFoundNew, countNew) ->
            if (manga.unic == this.manga!!.unic) { // Если совпадает манга
                if (countNew == -1) // Если произошла ошибка ошибках
                    longToast(R.string.list_chapters_message_error)
                else
                    if (!isFoundNew) // Если ничего не нашлось
                        longToast(R.string.list_chapters_message_no_found)
                    else { // Если нашлость, вывести сообщение с количеством
                        longToast(getString(R.string.list_chapters_message_count_new,
                                            countNew))
                        // Обновить список
                        adapter.update(!view.sortIndicator.item)
                    }

                view.isVisibleProgress.item = false // Скрыть прогрессБар
            }
        }
    }

    // id группы меню для экшнМода
    private val groupId = 1

    // id пунктов меню для экшнМода
    private object id {
        val selectAll = 1
        val delete = 2
        val download = 3
        val setRead = 4
        val setNotRead = 5
        val selectPrev = 6
        val selectNext = 7
    }

    // Создание меню для экшнМода
    override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
        // Выделить все
        mode.menu.add(groupId, id.selectAll, id.selectAll, R.string.action_select_all)
                .setIcon(R.drawable.ic_action_all_white)
                .showAlways()
//                .isChecked = false

        // Удалить выделенное
        mode.menu.add(groupId, id.delete, 100, R.string.action_delete)
                .setIcon(R.drawable.ic_action_delete_white)
                .showIfRoom()

        // Скачать выделенное
        mode.menu.add(groupId, id.download, 100, R.string.action_set_download)
                .setIcon(R.drawable.ic_action_download_white)
                .showIfRoom()

        // Сделать прочитанными
        mode.menu.add(groupId, id.setRead, 100, R.string.action_set_read)
                .showNever()

        // Сделать не прочитанными
        mode.menu.add(groupId, id.setNotRead, 100, R.string.action_set_not_read)
                .showNever()

        // Выделить предыдущие
        mode.menu.add(groupId, id.selectPrev, 101, R.string.action_select_prev)
                .showNever()
                .isEnabled = false

        // Выделить предыдущие
        mode.menu.add(groupId, id.selectNext, 102, R.string.action_select_next)
                .showNever()
                .isEnabled = false

        return true
    }

    // При изменениях в опциях меню в экшнМоде
    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu): Boolean {
        if (adapter.getSelectedCount() == 1) { // Включение опций только если выделен 1 элемент
            menu.findItem(id.selectNext).isEnabled = true
            menu.findItem(id.selectPrev).isEnabled = true
        } else {
            menu.findItem(id.selectNext).isEnabled = false
            menu.findItem(id.selectPrev).isEnabled = false
        }
        return true
    }

    // При нажатии опций меню в экщнМоде
    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
        when (item.itemId) { // Действия над выделенными главами
            id.delete -> {
                alert {
                    titleResource = R.string.list_chapters_remove_text
//                    message("${adapter.item!!.getSelectedCount()}")
                    positiveButton(R.string.list_chapters_remove_yes) {
                        adapter.remove()
                        actionMode?.finish()
                    }
                    negativeButton(R.string.list_chapters_remove_no) {}
                }.show()
                return false
            } // Удалить
            id.download -> adapter.downloadChapter() // Скачать
            id.setNotRead -> adapter.setRead(false) // Сделать не прочитанными
            id.setRead -> adapter.setRead(true) // Сделать прочитанными
            id.selectAll -> {
                adapter.selectAll() // Выбрать все
                actionMode?.title = actionTitle()
                return false
            }
            id.selectPrev -> {
                adapter.selectPrev() // Выбрать предыдущие элементы
                actionMode?.title = actionTitle()
                return false
            }
            id.selectNext -> {
                adapter.selectNext() // Выбрать последующие элементы
                actionMode?.title = actionTitle()
                return false
            }
        }
        actionMode?.finish() // Выйти из экшнМода
        return false
    }

    // При выключении экшнМода
    override fun onDestroyActionMode(mode: ActionMode?) {
        adapter.removeSelection() // Очистить выделение
        actionMode?.let {
            actionMode = null // ЗаNULLить переменную
        }
        view.isVisibleBottom.item = true // Показать бар внизу экрана
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu!!.add(0, 0, 0, R.string.list_chapters_option_update) // Поиск новых глав
                .showAlways()
                .setIcon(R.drawable.ic_action_update_white)

        // Быстрая загрузка глав
        menu.add(0, 1, 1, "Скачать следующую")

        menu.add(0, 2, 2, "Скачать непрочитанное")

        menu.add(0, 3, 3, "Скачать все")

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed() // Назад при нажатию стрелку
            0 -> { // Проверка на наличие новых глав
                view.isVisibleProgress.item = true // Показать прогрессБар
                MangaUpdater.addTask(manga!!) // Добавить мангу для проверки новых глав
            }
            1 -> { // БЫстрая загрузка глав
                val chapter = adapter.getCatalog()
                        .filter { it.action == CHAPTER_STATUS.DOWNLOADABLE }
                        .filter { !it.isRead }
                        .first()
                ChaptersDownloader.addTask(chapter)
                adapter.notifyDataSetChanged()
            }
            2 -> {
                val count = adapter.getCatalog()
                        .filter { it.action == CHAPTER_STATUS.DOWNLOADABLE }
                        .filter { !it.isRead }
                        .map { ChaptersDownloader.addTask(it) }
                        .size
                if (count == 0)
                    toast(R.string.list_chapters_selection_load_error)
                else
                    toast(getString(R.string.list_chapters_selection_load_ok, count))
                adapter.notifyDataSetChanged()
            }
            3 -> {
                val count = adapter.getCatalog()
                        .filter { it.action == CHAPTER_STATUS.DOWNLOADABLE }
                        .map { ChaptersDownloader.addTask(it) }
                        .size
                if (count == 0)
                    toast(R.string.list_chapters_selection_load_error)
                else
                    toast(getString(R.string.list_chapters_selection_load_ok, count))
                adapter.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        // Сохранение настроек
        getSharedPreferences(sPrefListChapters, MODE_PRIVATE).apply {
            edit().apply {
                putInt(filterStatus, view.filterIndicator) // тип фильтрации
                putBoolean(sortStatus, view.sortIndicator.item) // порядок сортировки
            }.apply()
        }
        // Сохранение здесь так как этот метод сработает в любом случае
    }

    override fun onDestroy() {
        MangaUpdater.bus.unregister() // Отписка
        ChaptersDownloader.bus.unregister(2) // Отписка
        super.onDestroy()
    }

    /* Функции */
    // При нажатии элементов
    fun onListItemSelect(position: Int) = launch(UI) {
        adapter.toggleSelection(position) // Переключить выбран элемент или нет

        val hasCheckedItems = adapter.getSelectedCount() > 0 // Проверка есть ли выделенные элементы элементы

        // Если есть выделенные элементы и экшнМод не включен
        if (hasCheckedItems and (actionMode == null)) {
            actionMode = startActionMode(this@ListChaptersActivity) // Включить экшнМод
            view.isVisibleBottom.item = false // Скрыть меню снизу
        } else if (!hasCheckedItems and (actionMode != null)) { // Если все наоборот
            actionMode!!.finish() // Завершить работу экшнМода
        }

        // Вывод в заголовок количество выделенных элементов
        actionMode?.title = actionTitle()
    }

    /* Приватные функции */
    // Заголовок для экшнМода, вынесен из-за повторов и большой длинны
    private fun actionTitle(): String {
        return resources
                .getQuantityString(
                        R.plurals.list_chapters_action_selected,
                        adapter.getSelectedCount(),
                        adapter.getSelectedCount()
                )
    }
}
