package com.san.kir.manger.components.CatalogForOneSite

import android.support.v7.widget.RecyclerView
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.log
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

typealias onStart = () -> Unit
typealias onEnd = (Int) -> Unit
class CatalogForOneSiteRecyclerPresenter(injector: KodeinInjector) : RecyclerPresenter() {

    companion object {
        val DATE = 0
        val NAME = 1
        val POP = 2
    }

    private val adapter = RecyclerViewAdapterFactory.createSimple { CatalogForOneSiteItemView() }
    private val filterAdapterList: List<CatalogFilter> by injector.instance()
    private var isReversed = false // Сохранение информации о порядке сортировки
    private var sortType = DATE // Сохранение информации о типе сортировки
    private var searchText = "" // Сохранение информации о поисковом запросе
    private var filters = listOf<FilterAdapter>() // Сохранение информации фильтрах
    private var backupCatalog = listOf<SiteCatalogElement>()

    fun setSite(siteId: Int, start: onStart? = null, end: onEnd? = null) = async(UI) {
        try {
            // Turn on busy indicator
            start?.invoke()

            adapter.items = async {
                SiteCatalogElementViewModel.setSiteId(siteId).items()
            }.await()
            backupCatalog = adapter.items

            changeOrder()

            async {
                adapter.items.forEach { item ->
                    filterAdapterList[0].adapter.addAll(item.genres)
                    filterAdapterList[1].adapter.add(item.type)
                }
            }.await()

            filterAdapterList[0].adapter.finishAdd()
            filterAdapterList[1].adapter.finishAdd()

        } catch (e: Exception) {
            log("setSite send exception: $e")
        } finally {
            end?.invoke(adapter.itemCount)
        }
    }

    private fun swapItems(newCatalog: List<SiteCatalogElement>) = launch(UI) {
        adapter.items = newCatalog
        adapter.notifyDataSetChanged()
    }

    fun changeOrder(sortType: Int = this.sortType,
                    isReversed: Boolean = this.isReversed,
                    searchText: String = this.searchText,
                    filters: List<FilterAdapter> = this.filters): Int {
        var list = backupCatalog

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
            DATE -> list.sortedBy { it.dateId } // Сортировать по дате
            NAME -> list.sortedBy { it.name } // Сортировать по имени
            POP -> list.sortedBy { it.populate } // Сортировать по популярности
            else -> list // .sortedBy { it.dateId } // Сортировать по дате
        }

        // Запоминание полученных данных
        this.isReversed = isReversed
        this.sortType = sortType
        this.searchText = searchText
        this.filters = filters

        // Обработка направления сортировки и обновление адаптера
        swapItems(if (isReversed) list.reversed() else list) // Изменить порядок списка, если надо по условию

        return list.size
    }

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recycler.adapter = this.adapter
    }

    fun clear() {
        swapItems(listOf())
    }
}

