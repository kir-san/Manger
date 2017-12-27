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
import com.san.kir.manger.R
import com.san.kir.manger.components.Drawer.DrawerActivity
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.utils.ActionModeControl
import com.san.kir.manger.utils.MangaUpdater
import com.san.kir.manger.utils.SortLibraryUtil
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.checkBox
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.include
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.radioButton
import org.jetbrains.anko.radioGroup
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.onPageChangeListener
import org.jetbrains.anko.support.v4.viewPager
import org.jetbrains.anko.textView
import org.jetbrains.anko.toast
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class LibraryActivity : DrawerActivity(), ActionMode.Callback {

    // Получаем список категорий
    private val mCategory: List<Category> get() = Main.db.categoryDao.loadCategories()
    private val updateApp = ManageSites.UpdateApp(this)
    private lateinit var currentAdapter: LibraryItemsAdapter
    private lateinit var viewPager: ViewPager
    val pagerAdapter by lazy { LibraryPageAdapter(this) }

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
        pagerAdapter.update()

        launch(UI) {
            delay(500)
            currentAdapter = pagerAdapter.adapters[0]
            title = getString(R.string.main_menu_library_count, currentAdapter.itemCount)
        }

        viewPager.onPageChangeListener {
            var index = 0

            onPageSelected {
                currentAdapter = pagerAdapter.adapters[it]
                title = getString(R.string.main_menu_library_count,
                                  currentAdapter.itemCount)

                if (actionMode.hasFinish()) {
                    index = it
                } else
                    if (it != index) {
                        toast("Не надо лезть к другим")
                        viewPager.currentItem = index
                    }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        actionMode.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // обновить мангу с текущей страницы
        menu.add(0, 0, 0, R.string.library_menu_reload)
                .showAlways().setIcon(R.drawable.ic_update)
        // обновить всю имеющуюся мангу
        menu.add(1, 1, 1, R.string.library_menu_reload_all)
        // меню порядка пунктов в библиотеке
        menu.add(2, 2, 2, R.string.library_menu_order)
        // поиск обновлений приложения
        menu.add(3, 3, 3, R.string.library_menu_update)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> updateCurrent()
            1 -> updateAll()
            2 -> sortCurrent()
            3 -> updateApp.checkNewVersion(true)
        }
        return super.onOptionsItemSelected(item)
    }

    private object _id {
        val moveToCategory = 1
        val delete = 2
        val selectAll = 3
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
//        index = mView.viewPager.currentItem
        supportActionBar?.hide()
        mode.menu.apply {
            // Выделить всю мангу
            add(1, _id.selectAll, 0, R.string.action_select_all)
                    .showAlways()
                    .setIcon(R.drawable.ic_action_all_white)

            // Перенести в указанную категорию
            add(1, _id.moveToCategory, 0, R.string.library_action_move_to_category)
                    .showIfRoom()
                    .setIcon(R.drawable.ic_arrow_forward_white)

            // Удалить вабранную мангу
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
                                    pagerAdapter.update()
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
        currentAdapter.removeSelection()
        actionMode.clear()
        supportActionBar?.show()
    }

    fun onListItemSelect(position: Int) = launch(UI) {
        currentAdapter.toggleSelection(position) // Переключить выбран элемент или нет

        val hasCheckedItems = currentAdapter.getSelectedCount() > 0 // Проверка есть ли выделенные элементы элементы

        // Если есть выделенные элементы и экшнМод не включен
        if (hasCheckedItems && actionMode.hasFinish()) {
            actionMode.start(this@LibraryActivity) // Включить экшнМод
        } else if (!hasCheckedItems && !actionMode.hasFinish()) { // Если все наоборот
            actionMode.finish() // Завершить работу экшнМода
        }

        // Вывод в заголовок количество выделенных элементов
        actionMode.setTitle(actionTitle())
    }

    private fun sortCurrent() {
        // получение текущей категории
        val cat = currentAdapter.cat
        alert {
            titleResource = R.string.library_menu_order_title

            customView {
                verticalLayout {
                    lparams(width = matchParent, height = wrapContent) {
                        padding = dip(16)
                    }

                    radioGroup {
                        radioButton {
                            setText(R.string.library_sort_dialog_add)
                            isChecked = cat.typeSort == SortLibraryUtil.add
                            onClick { cat.typeSort = SortLibraryUtil.add }
                        }

                        radioButton {
                            setText(R.string.library_sort_dialog_abc)
                            isChecked = cat.typeSort == SortLibraryUtil.abc
                            onClick { cat.typeSort = SortLibraryUtil.abc }
                        }
                    }

                    checkBox {
                        setText(R.string.library_sort_dialog_reverse)
                        isChecked = cat.isReverseSort
                        onCheckedChange { _, b -> cat.isReverseSort = b }
                    }
                }
            }

            positiveButton("Изменить") {
                // изменить порядок в адаптере
                currentAdapter.changeOrder(SortLibraryUtil.toType(cat.typeSort), cat.isReverseSort)
            }
            negativeButton("Я передумал") {}
        }.show()
    }

    private fun updateCurrent() = launch(CommonPool) {
        // получаем список манги в текущей странице и обновляем их
        currentAdapter.getCatalog().forEach {
            MangaUpdater.addTask(it)
        }
    }

    private fun updateAll() = launch(CommonPool) {
        // обновление всей манги
        Main.db.mangaDao.loadAllManga().forEach {
            MangaUpdater.addTask(it)
        }
    }

    // Заголовок для экшнМода, вынесен из-за повторов и большой длинны
    private fun actionTitle(): String {
        return resources
                .getQuantityString(
                        R.plurals.list_chapters_action_selected,
                        currentAdapter.getSelectedCount(),
                        currentAdapter.getSelectedCount()
                )
    }
}
