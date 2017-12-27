package com.san.kir.manger.components.ListChapters

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import com.san.kir.manger.Extending.BaseActivity
import com.san.kir.manger.Extending.Views.showAlways
import com.san.kir.manger.Extending.Views.showIfRoom
import com.san.kir.manger.Extending.Views.showNever
import com.san.kir.manger.R
import com.san.kir.manger.components.DownloadManager.DownloadManager
import com.san.kir.manger.components.DownloadManager.DownloadService
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.ChapterFilter
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.ActionModeControl
import com.san.kir.manger.utils.MangaUpdater
import com.san.kir.manger.utils.log
import com.san.kir.manger.utils.sPrefListChapters
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.longToast
import org.jetbrains.anko.okButton
import org.jetbrains.anko.setContentView

@SuppressLint("MissingSuperCall")
class ListChaptersActivity : BaseActivity(), ActionMode.Callback {
    companion object {
        private const val filterStatus = "filteringStatus"
    }

    private val actionMode: ActionModeControl by instance()
    private val adapter = ListChaptersRecyclerPresenter(injector)
    private val view = ListChapterView(adapter)
    private val mangas = Main.db.mangaDao
    private lateinit var manga: Manga
    private var bound = false
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            log("onServiceDisconnected()")
            bound = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            downloadManager =
                    (service as DownloadService.LocalBinder).service.downloadManager
            bound = true
        }
    }
    lateinit var downloadManager: DownloadManager

    override fun provideOverridingModule() = Kodein.Module {
        bind<ListChaptersActivity>() with instance(this@ListChaptersActivity)
        bind<ActionModeControl>() with singleton {
            ActionModeControl(this@ListChaptersActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view.setContentView(this)

        manga = mangas.loadManga(intent.getStringExtra("manga_unic"))
        title = manga.name

        val intent = Intent(this, DownloadService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        MangaUpdater.bus.register()
    }

    override fun onResume() {
        super.onResume()
        // Загрузка настроек
        getSharedPreferences(sPrefListChapters, MODE_PRIVATE).apply {
            if (contains(filterStatus)) {
                val filterString = getString(filterStatus, ChapterFilter.ALL_READ_ASC.name)
                adapter.setManga(manga, ChapterFilter.valueOf(filterString))
                view.filterState = filterString
            } // тип фильтрации
        }

        // Если в данный момент ведется поиск новых глав для данной манги
        if (MangaUpdater.contains(manga))
            view.isAction.item = true // Показать прогрессБар

        // Реакция на сообщения от поиска новых глав
        MangaUpdater.bus.onEvent { (manga, isFoundNew, countNew) ->
            if (manga.unic == this.manga.unic) { // Если совпадает манга
                if (countNew == -1) // Если произошла ошибка ошибках
                    longToast(R.string.list_chapters_message_error)
                else
                    if (!isFoundNew) // Если ничего не нашлось
                        longToast(R.string.list_chapters_message_no_found)
                    else { // Если нашлость, вывести сообщение с количеством
                        longToast(getString(R.string.list_chapters_message_count_new,
                                            countNew))
                        // Обновить список
                    }

                view.isAction.item = false // Скрыть прогрессБар
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
        val fullDelete = 8
    }

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

        // Полностью удалить главы
        mode.menu.add(groupId, id.fullDelete, 102, R.string.action_full_delete)
                .showNever()

        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu): Boolean {
        menu.findItem(id.selectNext).isEnabled = adapter.selectedCount == 1
        menu.findItem(id.selectPrev).isEnabled = adapter.selectedCount == 1
        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
        when (item.itemId) { // Действия над выделенными главами
            id.delete -> {
                alert {
                    titleResource = R.string.list_chapters_remove_text
                    positiveButton(R.string.list_chapters_remove_yes) {
                        adapter.deleteSelectedItems()
                        actionMode.finish()
                    }
                    negativeButton(R.string.list_chapters_remove_no) {}
                }.show()
                return false
            } // Удалить
            id.download -> adapter.downloadSelectedItems() // Скачать
            id.setNotRead -> adapter.setRead(false) // Сделать не прочитанными
            id.setRead -> adapter.setRead(true) // Сделать прочитанными
            id.selectAll -> {
                adapter.selectAll() // Выбрать все
                actionMode.setTitle(actionTitle())
                return false
            }
            id.selectPrev -> {
                adapter.selectPrev() // Выбрать предыдущие элементы
                actionMode.setTitle(actionTitle())
                return false
            }
            id.selectNext -> {
                adapter.selectNext() // Выбрать последующие элементы
                actionMode.setTitle(actionTitle())
                return false
            }
            id.fullDelete -> {
                alert {
                    this.title = "Внимание!!!"
                    message = "Данное действие приведет к удалению глав из базы данных. " +
                            "Все файлы и папки будут не тронуты, но приложение больше не будет " +
                            "видеть эти главы. Рекомендуется использовать это " +
                            "при появлении копий существующих глав. За один раз удаляет небольшое " +
                            "количество глав."
                    okButton {
                        adapter.fullDeleteSelectedItems()
                        actionMode.finish()
                    }
                    cancelButton { }
                }.show()
                return false
            }
        }
        actionMode.finish()
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        adapter.removeSelection() // Очистить выделение
        actionMode.clear()
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
                view.isAction.item = true // Показать прогрессБар
                MangaUpdater.addTask(manga) // Добавить мангу для проверки новых глав
            }
            1 -> adapter.downloadNextNotReadChapter()
            2 -> adapter.downloadAllNotReadChapters()
            3 -> adapter.downloadAllChapters()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        // Сохранение настроек
        getSharedPreferences(sPrefListChapters, MODE_PRIVATE)
                .edit()
                .putString(filterStatus, view.filterState)
                .apply()
    }

    override fun onDestroy() {
        MangaUpdater.bus.unregister() // Отписка
        super.onDestroy()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    fun onListItemSelect(position: Int) = launch(UI) {
        adapter.toggleSelection(position) // Переключить выбран элемент или нет
        // Если есть выделенные элементы и экшнМод не включен
        if (adapter.selectedCount > 0 && actionMode.hasFinish()) {
            actionMode.start(this@ListChaptersActivity)
            view.isVisibleBottom.item = false // Скрыть меню снизу
        } else if (adapter.selectedCount <= 0 && !actionMode.hasFinish()) { // Если все наоборот
            actionMode.finish() // Завершить работу экшнМода
        }

        // Вывод в заголовок количество выделенных элементов
        actionMode.setTitle(actionTitle())
    }

    private fun actionTitle(): String {
        return resources
                .getQuantityString(
                        R.plurals.list_chapters_action_selected,
                        adapter.selectedCount,
                        adapter.selectedCount
                )
    }
}
