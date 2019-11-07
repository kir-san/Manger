package com.san.kir.manger.components.list_chapters

import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.dialogs.toast
import com.san.kir.ankofork.horizontalProgressBar
import com.san.kir.ankofork.include
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.negative
import com.san.kir.ankofork.positive
import com.san.kir.ankofork.startService
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.MangaColumn
import com.san.kir.manger.services.MangaUpdaterService
import com.san.kir.manger.utils.ActionModeControl
import com.san.kir.manger.utils.enums.ChapterFilter
import com.san.kir.manger.utils.extensions.ThemedActionBarActivity
import com.san.kir.manger.utils.extensions.add
import com.san.kir.manger.utils.extensions.addCheckable
import com.san.kir.manger.utils.extensions.boolean
import com.san.kir.manger.utils.extensions.quantitySimple
import com.san.kir.manger.utils.extensions.showAlways
import com.san.kir.manger.utils.extensions.specialViewPager
import com.san.kir.manger.utils.extensions.string
import com.san.kir.manger.utils.extensions.visibleOrGone
import com.san.kir.manger.view_models.ListChaptersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ListChaptersActivity : ThemedActionBarActivity() {
    private val receiver by lazy { ListChapterReceiver(this) }
    private val actionCallback by lazy { ListChaptersActionCallback(mAdapter, this) }
    private val baseAdapter = ListChapterBaseAdapter(this)

    val actionMode by lazy { ActionModeControl(this) }
    val mViewModel by viewModels<ListChaptersViewModel>()
    val mAdapter = ListChaptersRecyclerPresenter(this)

    private val isTitle by boolean(
        R.string.settings_list_chapter_title_key, R.string.settings_list_chapter_title_default
    )
    private val isIndividual by boolean(
        R.string.settings_list_chapter_filter_key, R.string.settings_list_chapter_filter_default
    )
    private var filterStatus by string(filterStatusKey, ChapterFilter.ALL_READ_ASC.name)

    var manga = Manga()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verticalLayout {
            // ПрогрессБар для отображения поиска новых глав
            horizontalProgressBar {
                isIndeterminate = true
                visibleOrGone(mViewModel.isAction, mViewModel.isUpdate)
            }.lparams(width = matchParent, height = wrapContent)

            specialViewPager {
                if (isTitle) {
                    include<androidx.viewpager.widget.PagerTabStrip>(R.layout.page_tab_strip)
                }

                adapter = baseAdapter
            }

        }

        IntentFilter().apply {
            addAction(MangaUpdaterService.actionGet)
            registerReceiver(receiver, this)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launchWhenResumed {
            manga = withContext(Dispatchers.Default) {
                mViewModel.manga(intent.getStringExtra(MangaColumn.unic) as String)
            }

            title = manga.name
            baseAdapter.init

        }.invokeOnCompletion {
            if (isIndividual) {
                mAdapter.setManga(manga, manga.chapterFilter).invokeOnCompletion {
                    mViewModel.isUpdate.negative()
                }

                mViewModel.filter.unicItem = manga.chapterFilter

            } else {
                if (filterStatus.isNotEmpty()) {
                    mAdapter
                        .setManga(manga, ChapterFilter.valueOf(filterStatus))
                        .invokeOnCompletion { mViewModel.isUpdate.negative() }
                    mViewModel.filter.unicItem = ChapterFilter.valueOf(filterStatus)
                } else {
                    toast("Произошли внезапности")
                    onBackPressed()
                }
            }
            // Если в данный момент ведется поиск новых глав для данной манги
            if (MangaUpdaterService.contains(manga))
                mViewModel.isAction.positive()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.id.list_chapter_menu_update, R.string.list_chapters_option_update)
            .showAlways()
            .setIcon(R.drawable.ic_action_update_white)

        // Быстрая загрузка глав
        menu.add(R.id.list_chapter_menu_loadnext, R.string.list_chapters_download_next)
        menu.add(R.id.list_chapter_menu_loadnotread, R.string.list_chapters_download_not_read)
        menu.add(R.id.list_chapter_menu_loadall, R.string.list_chapters_download_all)
        menu.addCheckable(R.id.list_chapter_menu_isupdate, R.string.list_chapters_is_update)
        menu.addCheckable(R.id.list_chapter_menu_changesort, R.string.list_chapters_change_sort)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.list_chapter_menu_changesort).isChecked = manga.isAlternativeSort
        menu.findItem(R.id.list_chapter_menu_isupdate).isChecked = manga.isUpdate
        menu.findItem(R.id.list_chapter_menu_update).isVisible = manga.isUpdate
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed() // Назад при нажатию стрелку
            R.id.list_chapter_menu_update -> { // Проверка на наличие новых глав
                mViewModel.isAction.positive() // Показать прогрессБар
                startService<MangaUpdaterService>("manga" to manga)
            }
            R.id.list_chapter_menu_loadnext -> mAdapter.downloadNextNotReadChapter()
            R.id.list_chapter_menu_loadnotread -> mAdapter.downloadAllNotReadChapters()
            R.id.list_chapter_menu_loadall -> mAdapter.downloadAllChapters()
            R.id.list_chapter_menu_changesort -> lifecycleScope.launch(Dispatchers.Default) {
                manga.isAlternativeSort = !manga.isAlternativeSort
                mAdapter.changeSort(manga.isAlternativeSort)
                mViewModel.update(manga)
            }

            R.id.list_chapter_menu_isupdate -> lifecycleScope.launch(Dispatchers.Default) {
                manga.isUpdate = !manga.isUpdate
                invalidateOptionsMenu()
                mViewModel.update(manga)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        // Сохранение статуса фильтрации

        if (isIndividual) {
            manga.chapterFilter = mViewModel.filter.item
            lifecycleScope.launch(Dispatchers.Default) {
                mViewModel.update(manga)
            }
        } else {
            filterStatus = mViewModel.filter.item.name
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    fun onListItemSelect(position: Int) = GlobalScope.launch(Dispatchers.Main) {
        mAdapter.toggleSelection(position) // Переключить выбран элемент или нет
        // Если есть выделенные элементы и экшнМод не включен
        if (mAdapter.getSelectedCount() > 0 && actionMode.hasFinish()) {
            actionMode.start(actionCallback)
            mViewModel.isVisibleBottom.item = false // Скрыть меню снизу
        } else if (mAdapter.getSelectedCount() <= 0 && !actionMode.hasFinish()) { // Если все наоборот
            actionMode.finish() // Завершить работу экшнМода
        }

        // Вывод в заголовок количество выделенных элементов
        actionMode.setTitle(
            quantitySimple(R.plurals.list_chapters_action_selected, mAdapter.getSelectedCount())
        )
    }


    companion object {
        private const val filterStatusKey = "filteringStatus"
    }
}

