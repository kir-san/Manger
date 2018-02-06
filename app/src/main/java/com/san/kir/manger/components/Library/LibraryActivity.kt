package com.san.kir.manger.components.Library

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
import com.san.kir.manger.Extending.Views.showAlways
import com.san.kir.manger.Extending.Views.showIfRoom
import com.san.kir.manger.Extending.dialogs.SortCategoryDialog
import com.san.kir.manger.R
import com.san.kir.manger.components.Drawer.DrawerActivity
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.utils.ActionModeControl
import com.san.kir.manger.utils.MangaUpdaterService
import com.san.kir.manger.utils.SortLibraryUtil
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.dip
import org.jetbrains.anko.include
import org.jetbrains.anko.longToast
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startService
import org.jetbrains.anko.support.v4.onPageChangeListener
import org.jetbrains.anko.support.v4.viewPager
import org.jetbrains.anko.textView
import org.jetbrains.anko.toast
import org.jetbrains.anko.verticalLayout

class LibraryActivity : DrawerActivity(), ActionMode.Callback {

    private val mCategory: List<Category> get() = Main.db.categoryDao.loadCategories()
    private val updateApp = ManageSites.UpdateApp(this)
    private lateinit var currentAdapter: LibraryItemsRecyclerPresenter
    private lateinit var viewPager: ViewPager
    private val pagerAdapter by lazy { LibraryPageAdapter(this) }

    var isCustomizeMyOrder = false

    var actionMode = ActionModeControl(this)

    override val LinearLayout.customView: View
        get() = viewPager {
            include<PagerTabStrip>(R.layout.page_tab_strip)
            adapter = pagerAdapter
            viewPager = this
        }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.main_menu_library)
    }

    override fun onResume() {
        super.onResume()
        currentAdapter = pagerAdapter.adapters[0]
        launch(UI) {
            delay(500)
            try {
                currentAdapter = pagerAdapter.adapters[0]
                invalidateOptionsMenu()
                title = getString(R.string.main_menu_library_count, currentAdapter.itemCount)
            } catch (ex: IndexOutOfBoundsException) {
                longToast("Все категории скрыты?")
            }
        }

        viewPager.onPageChangeListener {
            var index = 0

            onPageSelected {
                currentAdapter = pagerAdapter.adapters[it]
                invalidateOptionsMenu()
                title = getString(
                    R.string.main_menu_library_count,
                    currentAdapter.itemCount
                )

                if (!actionMode.hasFinish() || isCustomizeMyOrder) {
                    if (it != index) {
                        toast("Не надо лезть к другим")
                        viewPager.currentItem = index
                    }
                } else {
                    index = it
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        actionMode.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, R.string.library_menu_reload)
            .showAlways().setIcon(R.drawable.ic_update)
        menu.add(1, 1, 1, R.string.library_menu_reload_all)
        menu.add(2, 2, 2, R.string.library_menu_order)
        menu.add(2, 4, 3, R.string.library_menu_customize_my_order)
        menu.add(3, 3, 4, R.string.library_menu_update)
        menu.add(0, 5, 5, "Завершить")
            .showAlways().setIcon(R.drawable.ic_action_close)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(0).isVisible = !isCustomizeMyOrder
        menu.findItem(1).isVisible = !isCustomizeMyOrder
        menu.findItem(2).isVisible = !isCustomizeMyOrder
        menu.findItem(3).isVisible = !isCustomizeMyOrder
        menu.findItem(4).isVisible =
                if (isCustomizeMyOrder) false
                else currentAdapter.cat.typeSort == SortLibraryUtil.man
        menu.findItem(5).isVisible = isCustomizeMyOrder
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> updateCurrent()
            1 -> updateAll()
            2 -> SortCategoryDialog(this, currentAdapter.cat)
            3 -> updateApp.checkNewVersion(true)
            4 -> {
                isCustomizeMyOrder = true
                invalidateOptionsMenu()
                currentAdapter.customizeOn()
            }
            5 -> {
                currentAdapter.customizeOff()
                isCustomizeMyOrder = false
                invalidateOptionsMenu()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private object _id {
        const val moveToCategory = 1
        const val delete = 2
        const val selectAll = 3
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
        supportActionBar?.hide()
        mode.menu.apply {
            add(1, _id.selectAll, 0, R.string.action_select_all)
                .showAlways()
                .setIcon(R.drawable.ic_action_all_white)

            add(1, _id.moveToCategory, 0, R.string.library_action_move_to_category)
                .showIfRoom()
                .setIcon(R.drawable.ic_arrow_forward_white)

            add(1, _id.delete, 0, R.string.library_action_remove)
                .showIfRoom()
                .setIcon(R.drawable.ic_action_delete_white)
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = true

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
        when (item.itemId) {
            _id.moveToCategory -> {
                PopupWindow(this).apply {
                    setBackgroundDrawable(background.apply { setColorFilter(Color.WHITE, ADD) })
                    contentView = context.verticalLayout {
                        mCategory.forEach { cat ->
                            textView(text = cat.name) {
                                padding = dip(10)
                                textSize = 18f
                                onClick {
                                    currentAdapter.moveToCategory(cat.name)
                                    dismiss()
                                    actionMode.finish()
                                }
                            }
                        }
                    }
                    isOutsideTouchable = true
                }.showAtLocation(viewPager, Gravity.END or Gravity.TOP, 0, 0)
            }
            _id.delete -> {
                alert {
                    titleResource = R.string.library_action_remove_title
                    positiveButton(R.string.library_action_remove_yes) {
                        currentAdapter.remove()
                        actionMode.finish()
                    }
                    neutralPressed(R.string.library_action_remove_no) {}
                    negativeButton(R.string.library_action_remove_yes_with_files) {
                        currentAdapter.remove(withFiles = true)
                        actionMode.finish()
                    }
                }.show()
            }
            _id.selectAll -> {
                currentAdapter.selectAll()
                // Вывод в заголовок количество выделенных элементов
                actionMode.setTitle(actionTitle())
            }
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        actionMode.clear()
        async(UI) {
            currentAdapter.removeSelection()
            delay(800)
            supportActionBar?.show()
        }
    }

    fun onListItemSelect(position: Int) = launch(UI) {
        currentAdapter.toggleSelection(position)

        val hasCheckedItems = currentAdapter.selectedCount > 0

        if (hasCheckedItems && actionMode.hasFinish()) {
            actionMode.start(this@LibraryActivity)
        } else if (!hasCheckedItems && !actionMode.hasFinish()) {
            actionMode.finish()
        }

        actionMode.setTitle(actionTitle())
    }

    private fun updateCurrent() = async {
        currentAdapter.catalog.forEach {
            startService<MangaUpdaterService>("manga" to it)
        }
    }

    private fun updateAll() = async {
        Main.db.mangaDao.loadAllManga().forEach {
            startService<MangaUpdaterService>("manga" to it)
        }
    }

    private fun actionTitle(): String {
        return resources
            .getQuantityString(
                R.plurals.list_chapters_action_selected,
                currentAdapter.selectedCount,
                currentAdapter.selectedCount
            )
    }
}
