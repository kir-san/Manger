package com.san.kir.manger.components.ListChapters

import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.ViewGroup
import com.san.kir.manger.R
import com.san.kir.manger.components.ChaptersDownloader.ChaptersDownloader
import com.san.kir.manger.dbflow.models.Chapter
import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.dbflow.wrapers.ChapterWrapper
import com.san.kir.manger.utils.CHAPTER_STATUS
import com.san.kir.manger.utils.delChapters
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.toast


class ListChaptersAdapter(
        private val activity: ListChaptersActivity, // Получаем активити
        private val manga: Manga // и мангу
) : RecyclerView.Adapter<ListChaptersViewHolder>() {

    companion object {
        val ALL_READ = 0
        val NOT_READ = 1
        val IS_READ = 2
    }

    private val mCatalog = mutableListOf<Chapter>() // Основной список для работы адаптера
    private var dCatalog = listOf<Chapter>() // Запасной список для работы сортировок и фильтраций

    private var isReversed = false // Сохранение информации и порядке сортировки
    private var filterType = ALL_READ // Сохранение информации о типе фильтра
    private val selectedItems = SparseBooleanArray() // Список хранения результатов выделения

    init {
        setHasStableIds(true)
        update()
    }

    /* Перезаписанные методы */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListChaptersViewHolder? {
        return ListChaptersViewHolder(ListChaptersItemView(activity), parent)
    }

    override fun onBindViewHolder(holder: ListChaptersViewHolder, position: Int) {
        holder.bind(mCatalog[position], selectedItems[position])
    }

    override fun getItemCount(): Int = mCatalog.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    /* Функции */

    fun getCatalog() = dCatalog

    private fun swapItems(catalog: List<Chapter>) = launch(UI) {
        //        val diffCallback = ListChaptersDiffCallback(mCatalog, catalog)
//        val diffResult = DiffUtil.calculateDiff(diffCallback)

        mCatalog.clear()
        mCatalog.addAll(catalog)
//        diffResult.dispatchUpdatesTo(this@ListChaptersAdapter)
        notifyDataSetChanged()
    }

    fun update(isReversed: Boolean = this.isReversed) = launch(CommonPool) {
        dCatalog = ChapterWrapper.asyncGetChapters(manga.unic) // Получение списка из базы данных
        changeOrder(isReversed = isReversed) // изменить только порядок
    }

    fun changeOrder(filter: Int = filterType,
                    isReversed: Boolean = this.isReversed) = launch(CommonPool) {
        val list = when (filter) {
            ALL_READ -> dCatalog // Отобразить все главы
            NOT_READ -> dCatalog.filter { !it.isRead } // Отобразить все не прочитанные главы
            IS_READ -> dCatalog.filter { it.isRead } // Отобразить все прочитанные главы
            else -> dCatalog // Отобразить все главы
        }
        swapItems(if (isReversed) list.reversed() else list) // Изменить порядок списка, если надо по условию
        this@ListChaptersAdapter.isReversed = isReversed
        filterType = filter
    }

    fun setRead(isReading: Boolean) { // Установить статус главы для выделенных элементов
        forSelection { i ->
            // Для всех выделеных элементов
            mCatalog[i].updateStatus(isReading) // Установить статус главы
        }
        changeOrder()
    }

    fun downloadChapter() { // Скачивание глав для выделенных элементов
        var count = 0
        forSelection { i ->
            val chapter = mCatalog[i]
            // для каждого выделенный элемент
            if (chapter.action == CHAPTER_STATUS.DOWNLOADABLE) {
                ChaptersDownloader.addTask(chapter) // Добавить задачу в загрузчик
                count++
            }
        }
        if (count == 0) {
            activity.toast(R.string.list_chapters_selection_load_error)
        } else {
            activity.toast(activity.getString(R.string.list_chapters_selection_load_ok, count))
        }
    }

    fun remove() { // Удаление глав для выделенных элементов
        var count = 0
        forSelection { i ->
            val chapter = mCatalog[i]
            if (chapter.action == CHAPTER_STATUS.DELETE) {
                delChapters(chapter)// Удалить
                count++
            }
        }
        if (count == 0) {
            activity.toast(R.string.list_chapters_selection_del_error)
        } else {
            activity.toast(R.string.list_chapters_selection_del_ok)
        }
    }

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
        repeat(mCatalog.size) { i ->
            // Каждый элемент списка
            selectedItems.put(i, true) // выделить
            notifyItemChanged(i) // обновить этот элемент
        }
    }

    fun selectPrev() {
        // Выделить предидущие элементы
        if (selectedItems.size() == 1) { // Работает только для одного выделенного элемента
            val position = selectedItems.keyAt(0) // Запомнить позицию единственного элемента
            repeat(mCatalog.size) { i ->
                // Для всех элементов основного списка
                if (i > position) { // Проверка больше ли позиции элемента, чем выделенного
                    selectedItems.put(i, true) // Если условие верно выделить его
                    notifyItemChanged(i) // Обновить данный элемент
                }
            }
        }
    }

    fun selectNext() {
        // Выделить последующие элементы
        if (selectedItems.size() == 1) { // Работает только для одного выделенного элемента
            val position = selectedItems.keyAt(0) // Запомнить позицию единственного элемента
            repeat(mCatalog.size) { i ->
                // Для всех элементов основного списка
                if (i < position) { // Проверка меньше ли позиции элемента, чем выделенного
                    selectedItems.put(i, true) // Если условие верно выделить его
                    notifyItemChanged(i) // Обновить данный элемент
                }
            }
        }
    }

    fun removeSelection() {
        // Очистить выделение
        repeat(mCatalog.size) { i ->
            // Каждый элемент списка
            selectedItems.delete(i) // снять выделение
            notifyItemChanged(i) // обновить этот элемент
        }
    }

    fun getSelectedCount(): Int { // Получение количества выделенных элементов
        return selectedItems.size()
    }

    /* Приватные функции */
    private fun forSelection(block: (Int) -> Unit) {
        // Нужно для упращения работы с выделением
        repeat(selectedItems.size()) { i ->
            // Повторять столько, сколько элементов в выделении
            if (selectedItems.valueAt(i)) { // Если значение true для значения
                block(selectedItems.keyAt(i)) // Выполнить действие с соответствующим ключем
                notifyItemChanged(selectedItems.keyAt(i)) // Обновить требуемую строку
            }
        }
    }

}
