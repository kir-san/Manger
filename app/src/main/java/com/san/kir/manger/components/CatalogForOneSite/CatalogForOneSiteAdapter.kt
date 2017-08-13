package com.san.kir.manger.components.CatalogForOneSite

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.san.kir.manger.dbflow.models.SiteCatalogElement
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class CatalogForOneSiteAdapter : RecyclerView.Adapter<CatalogForOneSiteAdapter.ViewHolder>() {

    companion object {
        val DATE = 0
        val NAME = 1
        val POP = 2
    }

    /*    Куча каталогов    */
    private val mCatalog = mutableListOf<SiteCatalogElement>()
    private var dCatalog = listOf<SiteCatalogElement>()

    private var isReversed = false // Сохранение информации о порядке сортировки
    private var sortType = DATE // Сохранение информации о типе сортировки
    private var searchText = "" // Сохранение информации о поисковом запросе
    private var filters = listOf<FilterAdapter>() // Сохранение информации фильтрах

    /* Перезаписанные методы */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        return ViewHolder(CatalogForOneSiteItemView(), parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mCatalog[position], getItemViewType(position))
    }

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0) 0 else 1
    }

    override fun getItemCount(): Int {
        return mCatalog.size
    }


    /* Функции */

    private fun swapItems(catalog: List<SiteCatalogElement> = dCatalog) = launch(UI) {
        mCatalog.clear()
        mCatalog.addAll(catalog)
        notifyDataSetChanged()
    }

    suspend fun changeOrder(sortType: Int = this.sortType,
                            isReversed: Boolean = this.isReversed,
                            searchText: String = this.searchText,
                            filters: List<FilterAdapter> = this.filters): Int {
        var list = dCatalog

        // Обработка поискового запроса
        if (searchText.isNotEmpty()) {
            list = list.filter { it.name.toLowerCase().contains(searchText.toLowerCase()) }
        }

        // Обработка фильтров
        if (!filters.all { it.getSelected().isEmpty() }) {
            val genres = filters[0].getSelected()
            val types = filters[1].getSelected()

            if (genres.isNotEmpty())
                list = list.filter { it.genres.containsAll(genres) }

            if (types.isNotEmpty())
                list = list.filter { types.contains(it.type) }
        }

        // Обработка сортировки
        list = when (sortType) {
            DATE -> list // .sortedBy { it.dateId } // Сортировать по дате
            NAME -> list.sortedBy { it.name } // Сортировать по имени
            POP -> list.sortedBy { it.populate } // Сортировать по популярности
            else -> list // .sortedBy { it.dateId } // Сортировать по дате
        }

        // Обработка направления сортировки и обновление адаптера
        swapItems(if (isReversed) list.reversed() else list) // Изменить порядок списка, если надо по условию

        // Запоминание полученных данных
        this@CatalogForOneSiteAdapter.isReversed = isReversed
        this@CatalogForOneSiteAdapter.sortType = sortType
        this@CatalogForOneSiteAdapter.searchText = searchText
        this@CatalogForOneSiteAdapter.filters = filters
        return list.size
    }

    // очистка списка
    fun clear() {
        dCatalog = listOf()
        swapItems(dCatalog)
    }

    suspend fun add(element: SiteCatalogElement) {
        dCatalog += element
    }

    class ViewHolder(val view: CatalogForOneSiteItemView, parent: ViewGroup) :
            RecyclerView.ViewHolder(view.createView(parent)) {
        fun bind(el: SiteCatalogElement, viewType: Int) {
            view.bind(el, viewType)
        }

    }
}
