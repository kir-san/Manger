package com.san.kir.manger.components.catalogForOneSite

import android.support.v7.widget.RecyclerView
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.log
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withContext
import kotlin.system.measureTimeMillis

typealias onEnd = (Int) -> Unit

class CatalogForOneSiteRecyclerPresenter : RecyclerPresenter() {

    companion object {
        const val DATE = 0
        const val NAME = 1
        const val POP = 2
    }

    private val adapter = RecyclerViewAdapterFactory.createSimple { CatalogForOneSiteItemView() }
    val filterAdapterList = listOf(
        CatalogFilter("Жанры", FilterAdapter()),
        CatalogFilter("Тип манги", FilterAdapter()),
        CatalogFilter("Статус манги", FilterAdapter()),
        CatalogFilter("Авторы", FilterAdapter())
    )
    private var isReversed = false // Сохранение информации о порядке сортировки
    private var sortType = DATE // Сохранение информации о типе сортировки
    private var searchText = "" // Сохранение информации о поисковом запросе
    private var filters = listOf<FilterAdapter>() // Сохранение информации фильтрах
    private var backupCatalog = listOf<SiteCatalogElement>()
    private var mainJob = Job()
    fun setSite(siteId: Int, end: onEnd? = null) {
        mainJob = async(UI) {
            val time = measureTimeMillis {
                try {
                    adapter.items = withContext(DefaultDispatcher) {
                        SiteCatalogElementViewModel.setSiteId(siteId).items()
                    }
                    backupCatalog = adapter.items

                    changeOrder()

                    var jobs = listOf<Job>()

                    jobs += launch {
                        adapter.items.forEach { item ->
                            filterAdapterList[0].adapter.add(*item.genres.toTypedArray())
                        }
                    }
                    jobs += launch {
                        adapter.items.forEach { item ->
                            filterAdapterList[1].adapter.add(item.type)
                        }
                    }
                    jobs += launch {
                        adapter.items.forEach { item ->
                            filterAdapterList[2].adapter.add(item.statusEdition)
                        }
                    }
                    jobs += launch {
                        adapter.items.forEach { item ->
                            filterAdapterList[3].adapter.add(*item.authors.toTypedArray())
                        }
                    }

                    jobs.forEach { it.join() }

                    filterAdapterList.forEach {
                        it.adapter.finishAdd()
                    }

                } catch (e: Exception) {
                    log("setSite send exception: $e")
                } finally {
                    end?.invoke(adapter.itemCount)

                }
            }
            log("$siteId finished in $time")
        }
    }

    fun close() {
        runBlocking {
            mainJob.cancel()
        }
    }

    private fun swapItems(newCatalog: List<SiteCatalogElement>) = launch(UI) {
        adapter.items = newCatalog
        adapter.notifyDataSetChanged()
    }

    fun changeOrder(
        sortType: Int = this.sortType,
        isReversed: Boolean = this.isReversed,
        searchText: String = this.searchText,
        filters: List<FilterAdapter> = this.filters
    ): Int {
        var list = backupCatalog

        // Обработка поискового запроса
        if (searchText.isNotEmpty()) {
            list = list.filter { it.name.toLowerCase().contains(searchText.toLowerCase()) }
        }

        // Обработка фильтров
        if (!filters.all { it.getSelected().isEmpty() }) {
            val genres = filters[0].getSelected()
            val types = filters[1].getSelected()
            val statuses = filters[2].getSelected()
            val authors = filters[3].getSelected()

            if (genres.isNotEmpty())
                list = list.filter { it.genres.containsAll(genres) }

            if (types.isNotEmpty())
                list = list.filter { types.contains(it.type) }

            if (statuses.isNotEmpty())
                list = list.filter { statuses.contains(it.statusEdition) }

            if (authors.isNotEmpty())
                list = list.filter { it.authors.containsAll(authors) }
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

