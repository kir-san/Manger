package com.san.kir.manger.components.list_chapters

import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import com.san.kir.manger.R
import com.san.kir.manger.components.download_manager.ChapterLoader
import com.san.kir.manger.components.download_manager.DownloadService
import com.san.kir.manger.eventBus.negative
import com.san.kir.manger.eventBus.positive
import com.san.kir.manger.extending.ThemedActionBarActivity
import com.san.kir.manger.extending.views.showAlways
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.MangaColumn
import com.san.kir.manger.utils.ActionModeControl
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.MangaUpdaterService
import com.san.kir.manger.utils.enums.ChapterFilter
import com.san.kir.manger.utils.sPrefListChapters
import com.san.kir.manger.view_models.ListChaptersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.longToast
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.startService
import org.jetbrains.anko.toast

class ListChaptersActivity : ThemedActionBarActivity() {
    private val actionCallback by lazy { ListChaptersActionCallback(mAdapter, this) }
    private val filterStatusKey = "filteringStatus"
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            downloadManager =
                    (service as DownloadService.LocalBinderC).chapterLoader
            bound = true
        }
    }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (intent.action == MangaUpdaterService.actionGet) {
                    val manga = intent.getStringExtra(MangaUpdaterService.ITEM_NAME)
                    val isFoundNew = intent.getBooleanExtra(MangaUpdaterService.IS_FOUND_NEW, false)
                    val countNew = intent.getIntExtra(MangaUpdaterService.COUNT_NEW, 0)

                    if (manga == this@ListChaptersActivity.manga.unic) { // Если совпадает манга
                        if (countNew == -1) // Если произошла ошибка ошибках
                            longToast(R.string.list_chapters_message_error)
                        else
                            if (!isFoundNew) // Если ничего не нашлось
                                longToast(R.string.list_chapters_message_no_found)
                            else { // Если нашлость, вывести сообщение с количеством
                                longToast(
                                    getString(
                                        R.string.list_chapters_message_count_new,
                                        countNew
                                    )
                                )
                                // Обновить список
                                mAdapter.update()
                            }

                        view.isAction.item = false // Скрыть прогрессБар
                    }
                }
            }
        }
    }
    val mAdapter = ListChaptersRecyclerPresenter(this)
    private var bound = false
    lateinit var manga: Manga
    lateinit var downloadManager: ChapterLoader
    val mViewModel by lazy {
        ViewModelProviders.of(this).get(ListChaptersViewModel::class.java)
    }
    val view = ListChapterView(mAdapter)
    val actionMode by lazy { ActionModeControl(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view.setContentView(this)

        val intentFilter = IntentFilter().apply { addAction(MangaUpdaterService.actionGet) }
        registerReceiver(receiver, intentFilter)

        runBlocking(coroutineContext) {
            manga = mViewModel.getManga(intent.getStringExtra(MangaColumn.unic))
            manga.populate += 1
            mViewModel.updateManga(manga)

            title = manga.name
        }

        val intent = Intent(this, DownloadService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        // Загрузка настроек
        val key = getString(R.string.settings_list_chapter_filter_key)
        val default =
            getString(R.string.settings_list_chapter_filter_default) == "true"

        val isIndividual = defaultSharedPreferences.getBoolean(key, default)
        if (isIndividual) {
            mAdapter.setManga(manga, manga.chapterFilter).invokeOnCompletion {
                view.isUpdate.negative()
            }
            view.filterState = manga.chapterFilter.name
        } else {
            val filterStatus = defaultSharedPreferences.getString(filterStatusKey, ChapterFilter.ALL_READ_ASC.name)
            filterStatus?.also {
                mAdapter.setManga(manga, ChapterFilter.valueOf(filterStatus)).invokeOnCompletion {
                    view.isUpdate.negative()
                }
                view.filterState = filterStatus
            } ?: kotlin.run {
                toast("Произошли внезапности")
                onBackPressed()
            }
        }



        // Если в данный момент ведется поиск новых глав для данной манги
        if (MangaUpdaterService.contains(manga))
            view.isAction.positive()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(
            OptionId.groupId,
            OptionId.update,
            0,
            R.string.list_chapters_option_update
        )
            .showAlways()
            .setIcon(R.drawable.ic_action_update_white)

        // Быстрая загрузка глав
        menu.add(OptionId.groupId, OptionId.loadNext, 1, R.string.list_chapters_download_next)

        menu.add(
            OptionId.groupId,
            OptionId.loadNotRead,
            2,
            R.string.list_chapters_download_not_read
        )

        menu.add(OptionId.groupId, OptionId.loadAll, 3, R.string.list_chapters_download_all)

        menu.add(OptionId.groupId, OptionId.isUpdate, 4, R.string.list_chapters_is_update)
            .isCheckable = true

        menu.add(OptionId.groupId, OptionId.changeSort, 5, R.string.list_chapters_change_sort)
            .isCheckable = true

        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(OptionId.changeSort).isChecked = manga.isAlternativeSort
        menu.findItem(OptionId.isUpdate).isChecked = manga.isUpdate
        menu.findItem(OptionId.update).isVisible = manga.isUpdate
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed() // Назад при нажатию стрелку
            OptionId.update -> { // Проверка на наличие новых глав
                view.isAction.positive() // Показать прогрессБар
                startService<MangaUpdaterService>("manga" to manga)
            }
            OptionId.loadNext -> mAdapter.downloadNextNotReadChapter()
            OptionId.loadNotRead -> mAdapter.downloadAllNotReadChapters()
            OptionId.loadAll -> mAdapter.downloadAllChapters()
            OptionId.changeSort -> {
                launch(Dispatchers.Default) {
                    manga.isAlternativeSort = !manga.isAlternativeSort
                    mAdapter.changeSort(manga.isAlternativeSort)
                    mViewModel.updateManga(manga)
                }
            }
            OptionId.isUpdate -> {
                launch(Dispatchers.Default) {
                    manga.isUpdate = !manga.isUpdate
                    invalidateOptionsMenu()
                    mViewModel.updateManga(manga)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        // Сохранение статуса фильтрации
        val key = getString(R.string.settings_list_chapter_filter_key)
        val default =
            getString(R.string.settings_list_chapter_filter_default) == "true"
        val isIndividual = defaultSharedPreferences.getBoolean(key, default)
        if (isIndividual) {
            manga.chapterFilter = ChapterFilter.valueOf(view.filterState)
            mViewModel.updateManga(manga)
        } else {
            getSharedPreferences(sPrefListChapters, MODE_PRIVATE)
                .edit()
                .putString(filterStatusKey, view.filterState)
                .apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            unbindService(connection)
            bound = false
        }
        unregisterReceiver(receiver)
    }

    fun onListItemSelect(position: Int) = GlobalScope.launch(Dispatchers.Main) {
        mAdapter.toggleSelection(position) // Переключить выбран элемент или нет
        // Если есть выделенные элементы и экшнМод не включен
        if (mAdapter.getSelectedCount() > 0 && actionMode.hasFinish()) {
            actionMode.start(actionCallback)
            view.isVisibleBottom.item = false // Скрыть меню снизу
        } else if (mAdapter.getSelectedCount() <= 0 && !actionMode.hasFinish()) { // Если все наоборот
            actionMode.finish() // Завершить работу экшнМода
        }

        // Вывод в заголовок количество выделенных элементов
        actionMode.setTitle(
            resources
                .getQuantityString(
                    R.plurals.list_chapters_action_selected,
                    mAdapter.getSelectedCount(),
                    mAdapter.getSelectedCount()
                )
        )
    }

    private object OptionId {
        val update = ID.generate()
        val loadNext = ID.generate()
        val loadNotRead = ID.generate()
        val loadAll = ID.generate()
        val changeSort = ID.generate()
        val isUpdate = ID.generate()

        val groupId = ID.generate()
    }
}

