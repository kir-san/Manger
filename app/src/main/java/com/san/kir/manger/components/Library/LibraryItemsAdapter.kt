package com.san.kir.manger.components.Library

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.ViewGroup
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.delete
import com.san.kir.manger.room.DAO.loadMangaWhereCategory
import com.san.kir.manger.room.DAO.removeChapters
import com.san.kir.manger.room.DAO.update
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.SortLibrary
import com.san.kir.manger.utils.SortLibraryUtil
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.getFullPath
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class LibraryItemsAdapter(val cat: Category, private val fragment: LibraryActivity) :
        RecyclerView.Adapter<LibraryItemsViewHolder>() {

    private val mangas = Main.db.mangaDao
    private val chapters = Main.db.chapterDao
    private val categories = Main.db.categoryDao

    private var mItemsList = listOf<Manga>() // основной список
    private var dItemsList = listOf<Manga>() // вспомогательный список
    val isEmpty = Binder(itemCount == 0) // флаг отсутствия элементов

    private var type = SortLibraryUtil.toType(cat.typeSort)
    private var isReverse = cat.isReverseSort
    private val selectedItems = SparseBooleanArray()

    init {
        // при создании адаптера обновить списки
        setHasStableIds(true)
        update()
    }

    /* Перезаписанные методы */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryItemsViewHolder? {
        return LibraryItemsViewHolder(
                LibraryPageItemView(cat.name == CATEGORY_ALL, fragment), parent)
    }

    override fun onBindViewHolder(holder: LibraryItemsViewHolder, position: Int) {
        holder.bind(mItemsList[position], selectedItems[position])
    }

    override fun getItemCount(): Int = mItemsList.size
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItemViewType(position: Int): Int = position

    // использование DiffUtil для простого мониторинга и
    // применения изменений в адаптере
    private fun swapItems(mangas: List<Manga>) = launch(UI) {
        val diffResult = DiffUtil.calculateDiff(LibraryItemsDiffCalback(mItemsList, mangas))

        mItemsList = mangas
        isEmpty.item = itemCount == 0 // проверка не пуст ли адаптер
        diffResult.dispatchUpdatesTo(this@LibraryItemsAdapter)
        notifyDataSetChanged()
    }

    // Обновление адаптера
    fun update() = launch(CommonPool) {
        // получение данных из базы данных
        dItemsList = mangas.loadMangaWhereCategory(cat.name)
        changeOrder()
    }

    // изменение порядка в адаптере
    fun changeOrder(type: SortLibrary = this.type,
                    isReverse: Boolean = this.isReverse) = launch(CommonPool) {
        // сохранение новых настроек порядка
        cat.typeSort = SortLibraryUtil.toString(type)
        cat.isReverseSort = isReverse
        categories.update(cat)
        this@LibraryItemsAdapter.type = type
        this@LibraryItemsAdapter.isReverse = isReverse

        val list = when (type) {
            SortLibrary.AddTime -> dItemsList
            SortLibrary.AbcSort -> dItemsList.sortedBy { it.name }
        }
        swapItems(if (isReverse) list.reversed() else list)
    }


    fun getCatalog(): List<Manga> = mItemsList

    /* Работа с выделением */
    fun toggleSelection(position: Int) {
        // Переключение выделения
        val value = !selectedItems.get(position) // Запомнить противоположное состояния выделения
        if (value) // Если оно положительно
            selectedItems.put(position, value) // То выделить элемент
        else
            selectedItems.delete(position) // Иначе снять выделение
        notifyItemChanged(position) // Обновить элемент
    }

    fun selectAll() {
        // Выделить все элементы
        repeat(mItemsList.size) { i ->
            // Каждый элемент списка
            selectedItems.put(i, true) // выделить
            notifyItemChanged(i) // обновить этот элемент
        }
    }

    fun removeSelection() {
        // Очистить выделение
        repeat(mItemsList.size) { i ->
            // Каждый элемент списка
            selectedItems.delete(i) // снять выделение
            notifyItemChanged(i) // обновить этот элемент
        }
    }

    // Получение количества выделенных элементов
    fun getSelectedCount() = selectedItems.size()

    fun remove(withFiles: Boolean = false) {
        forSelection { i ->
            val manga = mItemsList[i]
            mangas.delete(manga)
            launch(CommonPool) {
                chapters.removeChapters(manga.unic)
                if (withFiles)
                    getFullPath(manga.path).deleteRecursively()
            }
        }
        update()
    }

    fun moveToCategory(newCategory: String) {
        forSelection { i ->
            val manga = mItemsList[i]
            manga.categories = newCategory
            mangas.update(manga)
        }
    }

    private fun forSelection(block: (Int) -> Unit) {
        // Нужно для упращения работы с выделением
        repeat(selectedItems.size()) { i ->
            // Повторять столько, сколько элементов в выделении
            if (selectedItems.valueAt(i)) { // Если значение true для значения
                block(selectedItems.keyAt(i)) // Выполнить действие с соответствующим ключем
//                notifyItemChanged(selectedItems.keyAt(i)) // Обновить требуемую строку
            }
        }
    }
}
