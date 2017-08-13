package com.san.kir.manger.components.Library

import android.graphics.Color
import android.graphics.PorterDuff.Mode.ADD
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.ActionMode
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.san.kir.manger.R
import com.san.kir.manger.components.Main.MainActivity
import com.san.kir.manger.dbflow.models.Category
import com.san.kir.manger.dbflow.wrapers.CategoryWrapper
import com.san.kir.manger.dbflow.wrapers.MangaWrapper
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.LibraryAdaptersCount
import com.san.kir.manger.utils.MangaUpdater
import com.san.kir.manger.utils.SortLibrary
import com.san.kir.manger.utils.SortLibraryUtil
import com.san.kir.manger.utils.showAlways
import com.san.kir.manger.utils.showIfRoom
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.checkBox
import org.jetbrains.anko.dip
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.radioButton
import org.jetbrains.anko.radioGroup
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.onPageChangeListener
import org.jetbrains.anko.textView
import org.jetbrains.anko.toast
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class LibraryFragment(val mAct: MainActivity) : Fragment(), ActionMode.Callback {

    companion object {
        var actionMode: ActionMode? = null
    }

    private object categories {
        val mCategory: List<Category> get() = CategoryWrapper.getCategories()
    }

    val libraryPagerAdapter by lazy {
        LibraryPageAdapter(this)
    }

    val mView = LibraryView(libraryPagerAdapter)

    override fun onCreateView(inflater: LayoutInflater, con: ViewGroup?, saved: Bundle?): View? {
        setHasOptionsMenu(true)
        return mView.createView(AnkoContext.create(context, this))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launch(UI) {
            // Получаем список категорий
            val categories = CategoryWrapper.asyncGetCategories()

            // если список не пуст
            if (categories.isNotEmpty())
                categories.forEach { cat ->
                    // то каждой категории, которая видима создаем страницу в адаптере страниц
                    if (cat.isVisible) {
                        libraryPagerAdapter.addPage(cat)
                    }
                }

            LibraryAdaptersCount.init(libraryPagerAdapter.adapters)

            delay(time = 500)
            mAct.title = getString(R.string.main_menu_library_count,
                                   libraryPagerAdapter.adapters[mView.viewPager.currentItem].itemCount)
            mView.viewPager.onPageChangeListener {
                onPageSelected {
                    mAct.title = getString(R.string.main_menu_library_count,
                                           libraryPagerAdapter.adapters[it].itemCount)

                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        menu.clear()

        // обновить мангу с текущей страницы
        menu.add(0, 0, 0, R.string.library_menu_reload)
                .showAlways().setIcon(R.drawable.ic_update)

        // обновить всю имеющуюся мангу
        menu.add(1, 1, 1, R.string.library_menu_reload_all)
        // меню порядка пунктов в библиотеке
        menu.add(2, 2, 2, R.string.library_menu_order)
        // поиск обновлений приложения
        menu.add(3, 3, 3, R.string.library_menu_update)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> updateCurrent()
            1 -> updateAll()
            2 -> sortCurrent()
            3 -> MainActivity.checkNewVersion(true)
        }
        return super.onOptionsItemSelected(item)
    }

    private object _id {
        val moveToCategory = 1
        val delete = 2
        val selectAll = 3

        val group = 1
    }

    private var index = 0
    private val listener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
        }

        override fun onPageScrolled(position: Int,
                                    positionOffset: Float,
                                    positionOffsetPixels: Int) {
        }

        override fun onPageSelected(position: Int) {
            if (position != index) {
                context.toast("Не надо лезть к другим")
                mView.viewPager.currentItem = index
            }
        }
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
        index = mView.viewPager.currentItem
        mAct.supportActionBar?.hide()
        mView.viewPager.addOnPageChangeListener(listener)

        mode.menu.apply {
            // Перенести в указанную категорию
            add(_id.group,
                _id.moveToCategory,
                _id.moveToCategory,
                R.string.library_action_move_to_category)
                    .showIfRoom()
                    .setIcon(R.drawable.ic_arrow_forward_black_24dp)

            // Удалить вабранную мангу
            add(_id.group,
                _id.delete,
                _id.delete,
                R.string.library_action_remove)
                    .showIfRoom()
                    .setIcon(R.drawable.ic_action_delete_white)

            // Выделить всю мангу
            add(_id.group,
                _id.selectAll,
                _id.selectAll,
                R.string.action_select_all)
                    .showAlways()
                    .setIcon(R.drawable.ic_action_all_white)
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
        val adapter = libraryPagerAdapter.adapters[index]
        when (item.itemId) {
            _id.moveToCategory -> {
                PopupWindow(context).apply {
                    setBackgroundDrawable(background.apply { setColorFilter(Color.WHITE, ADD) })
                    contentView = context.verticalLayout {
                        categories.mCategory.forEach { cat ->
                            textView(text = cat.name) {
                                padding = dip(10)
                                textSize = 18f
                                onClick {
                                    adapter.moveToCategory(cat.name)
                                    LibraryAdaptersCount.update()
                                    dismiss()
                                    actionMode?.finish()
                                }
                            }
                        }
                    }
                    isOutsideTouchable = true
                }.showAtLocation(mView.viewPager, Gravity.RIGHT or Gravity.TOP, 0, 0)
            }
            _id.delete -> {
                context.alert {
                    titleResource = R.string.library_action_remove_title
                    positiveButton(R.string.library_action_remove_yes) {
                        adapter.remove()
                        actionMode?.finish()
                    }
                    neutralPressed(R.string.library_action_remove_no) {}
                    negativeButton(R.string.library_action_remove_yes_with_files) {
                        adapter.remove(withFiles = true)
                        actionMode?.finish()
                    }
                }.show()
            }
            _id.selectAll -> {
                adapter.selectAll()
                // Вывод в заголовок количество выделенных элементов
                actionMode?.title = actionTitle()
            }
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        libraryPagerAdapter.adapters[index].removeSelection()
        actionMode?.let {
            actionMode = null
        }
        mView.viewPager.removeOnPageChangeListener(listener)
        mAct.supportActionBar?.show()
    }

    fun onListItemSelect(position: Int) = launch(UI) {
        // получение текущего адаптера
        val adapter = libraryPagerAdapter.adapters[mView.viewPager.currentItem]
        adapter.toggleSelection(position) // Переключить выбран элемент или нет

        val hasCheckedItems = adapter.getSelectedCount() > 0 // Проверка есть ли выделенные элементы элементы

        // Если есть выделенные элементы и экшнМод не включен
        if (hasCheckedItems and (actionMode == null)) {
            actionMode = activity.startActionMode(this@LibraryFragment) // Включить экшнМод
        } else if (!hasCheckedItems and (actionMode != null)) { // Если все наоборот
            actionMode!!.finish() // Завершить работу экшнМода
        }

        // Вывод в заголовок количество выделенных элементов
        actionMode?.title = actionTitle()
    }

    private fun sortCurrent() {
        // получение текущего адаптера
        val adapter = libraryPagerAdapter.adapters[mView.viewPager.currentItem]
        // получение текущей категории
        val cat = adapter.cat
        val add = ID.generate()
        val abc = ID.generate()
        var selectedSort = 0
        var isReverse = cat.isReverseSort
        context.alert {
            titleResource = R.string.library_menu_order_title

            customView =
                    context.verticalLayout {
                        lparams(width = matchParent, height = wrapContent) {
                            margin = dip(16)
                        }

                        radioGroup {
                            lparams(width = wrapContent, height = wrapContent)

                            setOnCheckedChangeListener { _, i -> selectedSort = i }

                            radioButton {
                                id = add
                                setText(R.string.library_sort_dialog_add)
                                isChecked = true
                            }.lparams(width = matchParent, height = wrapContent)

                            radioButton {
                                id = abc
                                setText(R.string.library_sort_dialog_abc)
                            }.lparams(width = matchParent, height = wrapContent)

                            // установка check`а на пункте, который был сохранен
                            check(if (cat.typeSort == SortLibraryUtil.add) add
                                  else abc)
                        }

                        checkBox {
                            lparams(width = wrapContent, height = wrapContent)
                            setText(R.string.library_sort_dialog_reverse)
                            isChecked = isReverse

                            onCheckedChange { _, b -> isReverse = b }
                        }
                    }


            positiveButton("Изменить") {
                val type = when (selectedSort) {
                    add -> SortLibrary.AddTime
                    abc -> SortLibrary.AbcSort
                    else -> SortLibrary.AddTime
                }

                // изменить порядок в адаптере
                adapter.changeOrder(type, isReverse)
            }
            negativeButton("Я передумал") {}
        }.show()
    }

    private fun updateCurrent() = launch(CommonPool) {
        // получаем позицию текущей страницы
        val current: Int = mView.viewPager.currentItem
        // получаем список манги в текущей странице и обновляем их
        libraryPagerAdapter.adapters[current].getCatalog().forEach {
            MangaUpdater.addTask(it)
        }
    }

    private fun updateAll() = launch(CommonPool) {
        // обновление всей манги
        MangaWrapper.getAllManga().forEach {
            MangaUpdater.addTask(it)
        }
    }

    // Заголовок для экшнМода, вынесен из-за повторов и большой длинны
    private fun actionTitle(): String {
        val adapter = libraryPagerAdapter.adapters[mView.viewPager.currentItem]
        return resources
                .getQuantityString(
                        R.plurals.list_chapters_action_selected,
                        adapter.getSelectedCount(),
                        adapter.getSelectedCount()
                )
    }
}
