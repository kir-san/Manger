package com.san.kir.manger.components.list_chapters

import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerTabStrip
import com.san.kir.ankofork.appcompat.toolbar
import com.san.kir.ankofork.constraint_layout.constraintLayout
import com.san.kir.ankofork.constraint_layout.matchConstraint
import com.san.kir.ankofork.design.themedAppBarLayout
import com.san.kir.ankofork.dialogs.toast
import com.san.kir.ankofork.doFromSdk
import com.san.kir.ankofork.horizontalProgressBar
import com.san.kir.ankofork.include
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.negative
import com.san.kir.ankofork.positive
import com.san.kir.ankofork.startService
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.MangaColumn
import com.san.kir.manger.services.MangaUpdaterService
import com.san.kir.manger.utils.ActionModeControl
import com.san.kir.manger.utils.enums.ChapterFilter
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.add
import com.san.kir.manger.utils.extensions.addCheckable
import com.san.kir.manger.utils.extensions.boolean
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets
import com.san.kir.manger.utils.extensions.log
import com.san.kir.manger.utils.extensions.quantitySimple
import com.san.kir.manger.utils.extensions.showAlways
import com.san.kir.manger.utils.extensions.specialViewPager
import com.san.kir.manger.utils.extensions.string
import com.san.kir.manger.utils.extensions.visibleOrGone
import com.san.kir.manger.view_models.ListChaptersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ListChaptersActivity : BaseActivity() {
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

        doFromSdk(Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent_dark)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.transparent_dark2)
        }

        constraintLayout {
            lparams(width = matchParent, height = matchParent)

            systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

            doOnApplyWindowInstets { view, insets, _ ->
                // Получаем размер выреза, если есть
                val cutoutRight = insets.displayCutout?.safeInsetRight ?: 0
                val cutoutLeft = insets.displayCutout?.safeInsetLeft ?: 0
                // Вычитаем из WindowInsets размер выреза, для fullscreen
                view.updatePadding(
                    left = insets.systemWindowInsetLeft - cutoutLeft,
                    right = insets.systemWindowInsetRight - cutoutRight
                )
                insets
            }

            val appBar = themedAppBarLayout(R.style.ThemeOverlay_AppCompat_DayNight_ActionBar) {
                id = View.generateViewId()
                doOnApplyWindowInstets { v, insets, _ ->
                    v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        topMargin = insets.systemWindowInsetTop
                    }
                    insets
                }

                toolbar {
                    lparams(width = matchParent, height = wrapContent)
                    setSupportActionBar(this)
                }
            }.lparams(width = matchConstraint, height = wrapContent) {
                topToTop = ConstraintSet.PARENT_ID
                startToStart = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
            }

            // ПрогрессБар для отображения поиска новых глав
            val progress = horizontalProgressBar {
                id = View.generateViewId()
                isIndeterminate = true
                visibleOrGone(mViewModel.isAction, mViewModel.isUpdate)
            }.lparams(width = matchConstraint, height = wrapContent) {
                topToBottom = appBar.id
                startToStart = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
            }

            specialViewPager {
                id = View.generateViewId()
                if (isTitle) {
                    include<PagerTabStrip>(R.layout.page_tab_strip)
                }

                doOnApplyWindowInstets { v, insets, _ ->
                    log("insets.systemWindowInsetBottom ${insets.systemWindowInsetBottom}")

                    insets
                }

                adapter = baseAdapter
            }.lparams(width = matchConstraint, height = matchConstraint) {
                topToBottom = progress.id
                startToStart = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
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
            .setIcon(R.drawable.ic_action_update)

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

    fun onListItemSelect(position: Int) = lifecycleScope.launch(Dispatchers.Main) {
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

