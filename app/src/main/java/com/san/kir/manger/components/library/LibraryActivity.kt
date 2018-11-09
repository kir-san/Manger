package com.san.kir.manger.components.library

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff.Mode.ADD
import android.os.Bundle
import android.support.v4.view.PagerTabStrip
import android.support.v4.view.ViewPager
import android.view.ActionMode
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.san.kir.manger.App.Companion.context
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.eventBus.negative
import com.san.kir.manger.eventBus.positive
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.extending.ankoExtend.startForegroundService
import com.san.kir.manger.extending.ankoExtend.visibleOrGone
import com.san.kir.manger.extending.views.showAlways
import com.san.kir.manger.extending.views.showIfRoom
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.MangaColumn
import com.san.kir.manger.utils.ActionModeControl
import com.san.kir.manger.utils.AppUpdateService
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.MangaUpdaterService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.include
import org.jetbrains.anko.longToast
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.startService
import org.jetbrains.anko.support.v4.onPageChangeListener
import org.jetbrains.anko.support.v4.viewPager
import org.jetbrains.anko.textView
import org.jetbrains.anko.toast
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class LibraryActivity : DrawerActivity() {
    private val mCategory: List<Category> get() = Main.db.categoryDao.loadCategories()
    private var currentAdapter: LibraryItemsRecyclerPresenter? = null
    private lateinit var viewPager: ViewPager
    private val pagerAdapter by lazy { LibraryPageAdapter(this) }
    private val actionModeControl = object : ActionMode.Callback {
        val moveToCategory = ID.generate()
        val delete = ID.generate()
        val selectAll = ID.generate()

        override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
            supportActionBar?.hide()
            mode.menu.apply {
                add(1, selectAll, 0, R.string.action_select_all)
                    .showAlways()
                    .setIcon(R.drawable.ic_action_all_white)

                add(1, moveToCategory, 0, R.string.library_action_move_to_category)
                    .showIfRoom()
                    .setIcon(R.drawable.ic_arrow_forward_white)

                add(1, delete, 0, R.string.library_action_remove)
                    .showIfRoom()
                    .setIcon(R.drawable.ic_action_delete_white)
            }
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = true

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                moveToCategory -> {
                    PopupWindow(this@LibraryActivity).apply {
                        setBackgroundDrawable(background.apply { setColorFilter(Color.WHITE, ADD) })
                        contentView = context.verticalLayout {
                            mCategory.forEach { cat ->
                                textView(text = cat.name) {
                                    padding = dip(10)
                                    textSize = 18f
                                    onClick {
                                        currentAdapter?.moveToCategory(cat.name)
                                        mode.finish()
                                        dismiss()
                                    }
                                }
                            }
                        }
                        isOutsideTouchable = true
                    }.showAtLocation(viewPager, Gravity.END or Gravity.TOP, 0, 0)
                }
                delete -> {
                    alert {
                        titleResource = R.string.library_action_remove_title
                        positiveButton(R.string.library_action_remove_yes) {
                            currentAdapter?.remove()
                            mode.finish()
                        }
                        neutralPressed(R.string.library_action_remove_no) {}
                        negativeButton(R.string.library_action_remove_yes_with_files) {
                            currentAdapter?.remove(withFiles = true)
                            mode.finish()
                        }
                    }.show()
                }
                selectAll -> {
                    currentAdapter?.selectAll()
                    // Вывод в заголовок количество выделенных элементов
                    mode.title = actionTitle()
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode.clear()
            GlobalScope.launch(Dispatchers.Main) {
                currentAdapter?.removeSelection()
                delay(800)
                supportActionBar?.show()
            }
        }
    }
    private val isAction = Binder(false)

    var actionMode = ActionModeControl(this)

    override val LinearLayout.customView: View
        @SuppressLint("ResourceType")
        get() = verticalLayout {
            this.id = 1

            horizontalProgressBar {
                isIndeterminate = true
                visibleOrGone(isAction)
            }.lparams(width = matchParent, height = wrapContent)

            viewPager {
                //                this.top
                include<PagerTabStrip>(R.layout.page_tab_strip)
                adapter = pagerAdapter
                viewPager = this
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.main_menu_library)
    }

    override fun onResume() {
        super.onResume()
        isAction.positive()
        pagerAdapter.init.invokeOnCompletion {
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    currentAdapter = pagerAdapter.adapters[0]
                    invalidateOptionsMenu()

                    val count = currentAdapter?.itemCount

                    title = if (count != null && count > 0) {
                        getString(R.string.main_menu_library_count, count)
                    } else {
                        delay(1300L)
                        getString(R.string.main_menu_library_count, currentAdapter?.itemCount)
                    }
                } catch (ex: IndexOutOfBoundsException) {
                    longToast(R.string.library_error_hide_categories)
                } finally {
                    isAction.negative()
                }
            }
        }

        viewPager.onPageChangeListener {
            var index = 0

            onPageSelected {
                GlobalScope.launch(Dispatchers.Main) {
                    currentAdapter = pagerAdapter.adapters[it]
                    invalidateOptionsMenu()
                    title = getString(
                        R.string.main_menu_library_count,
                        currentAdapter?.itemCount
                    )

                    if (!actionMode.hasFinish()) {
                        if (it != index) {
                            toast(R.string.library_selection_mode_message_taboo)
                            viewPager.currentItem = index
                        }
                    } else {
                        index = it
                    }
                }
            }
        }

//        checkNewVersion(this)
    }

    override fun onPause() {
        super.onPause()
        actionMode.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, R.string.library_menu_reload)
        menu.add(1, 1, 1, R.string.library_menu_reload_all)
//        menu.add(2, 2, 2, R.string.library_menu_order)
        menu.add(3, 3, 4, R.string.library_menu_update)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> updateCurrent()
            1 -> updateAll()
//            2 -> SortCategoryDialog(this, currentAdapter?.cat!!)
            3 -> startForegroundService<AppUpdateService>()
        }
        return super.onOptionsItemSelected(item)
    }

    fun onListItemSelect(position: Int) = GlobalScope.launch(Dispatchers.Main) {
        currentAdapter?.toggleSelection(position)

        val hasCheckedItems = currentAdapter?.selectedCount!! > 0

        if (hasCheckedItems && actionMode.hasFinish()) {
            actionMode.start(actionModeControl)
        } else if (!hasCheckedItems && !actionMode.hasFinish()) {
            actionMode.finish()
        }

        actionMode.setTitle(actionTitle())
    }

    private fun updateCurrent() = GlobalScope.launch(Dispatchers.Main) {
        currentAdapter?.catalog?.forEach {
            startService<MangaUpdaterService>(MangaColumn.tableName to it)
        }
    }

    private fun updateAll() = GlobalScope.launch(Dispatchers.Main) {
        Main.db.mangaDao.loadAllManga().forEach {
            startService<MangaUpdaterService>(MangaColumn.tableName to it)
        }
    }

    private fun actionTitle(): String {
        return resources
            .getQuantityString(
                R.plurals.list_chapters_action_selected,
                currentAdapter?.selectedCount!!,
                currentAdapter?.selectedCount
            )
    }
}

