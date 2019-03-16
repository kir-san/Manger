package com.san.kir.manger.components.catalog_for_one_site

import android.support.v7.widget.RecyclerView
import com.san.kir.manger.components.parsing.SiteCatalog
import com.san.kir.manger.extending.launchCtx
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class CatalogForOneSiteRecyclerPresenter(private val act: CatalogForOneSiteActivity) :
    RecyclerPresenter() {
    companion object {
        const val DATE = 0
        const val NAME = 1
        const val POP = 2
    }

    private val adapter = RecyclerViewAdapterFactory.createSimple { CatalogForOneSiteItemView(act) }
    val pagerAdapter = CatalogForOneSiteFilterPagesAdapter()
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

    fun setSite(siteId: SiteCatalog, end: ((Int) -> Unit)? = null) {
        if (mainJob.isActive) mainJob.cancel()
        mainJob = act.launchCtx {
            try {
                backupCatalog = act.mViewModel.getSiteCatalogItems(siteId, true)

                adapter.items = backupCatalog

                changeOrder()

                listOf(
                    launch(Dispatchers.Default) {
                        adapter.items.forEach { item ->
                            filterAdapterList[0].adapter.add(*item.genres.toTypedArray())
                        }
                    },
                    launch(Dispatchers.Default) {
                        adapter.items.forEach { item ->
                            filterAdapterList[1].adapter.add(item.type)
                        }
                    },
                    launch(Dispatchers.Default) {
                        adapter.items.forEach { item ->
                            filterAdapterList[2].adapter.add(item.statusEdition)
                        }
                    },
                    launch(Dispatchers.Default) {
                        adapter.items.forEach { item ->
                            filterAdapterList[3].adapter.add(*item.authors.toTypedArray())
                        }
                    }
                ).joinAll()

                withContext(Dispatchers.Main) {
                    var endList = listOf<CatalogFilter>()
                    filterAdapterList.forEach {
                        it.adapter.finishAdd()
                        if (it.adapter.catalog.size > 1) {
                            endList = endList + it
                        }

                    }
                    pagerAdapter.init(act, endList)
                }

                end?.invoke(adapter.itemCount)
            } catch (e: Exception) {
                log("setSite send exception: $e")
                setSite(siteId, end)
            }
        }
    }

    fun close() {
        runBlocking {
            mainJob.cancel()
        }
    }

    private fun swapItems(newCatalog: List<SiteCatalogElement>): Job {
        return act.launch(Dispatchers.Main) {
            adapter.items = newCatalog
            adapter.notifyDataSetChanged()
        }
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

