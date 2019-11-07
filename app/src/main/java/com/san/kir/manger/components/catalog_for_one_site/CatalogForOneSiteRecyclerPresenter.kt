package com.san.kir.manger.components.catalog_for_one_site

import androidx.lifecycle.lifecycleScope
import com.san.kir.manger.components.parsing.SiteCatalog
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.extensions.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*

class CatalogForOneSiteRecyclerPresenter(private val act: CatalogForOneSiteActivity) :
    RecyclerPresenter() {
    companion object {
        const val DATE = 0
        const val NAME = 1
        const val POP = 2
    }

    private val adapter = RecyclerViewAdapterFactory.createSimple { CatalogForOneSiteItemView(act) }
    val pagerAdapter = CatalogForOneSiteFilterPagesAdapter()
    private val filterAdapterList = listOf(
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
    private var mainJob: Job = Job()

    fun setSite(siteId: SiteCatalog, end: ((Int) -> Unit)? = null) {
        if (mainJob.isActive) mainJob.cancel()
        mainJob = act.lifecycleScope.launch(Dispatchers.Default) {
            try {
                backupCatalog = act.mViewModel.items(siteId, true)

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
                    filterAdapterList.forEach {
                        it.adapter.finishAdd()
                    }
                    pagerAdapter.init(act, filterAdapterList)
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
        return act.lifecycleScope.launch(Dispatchers.Main) {
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
            list = list.filter {
                it.name.toLowerCase(Locale.ROOT).contains(searchText.toLowerCase(Locale.ROOT))
            }
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

    override fun into(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.into(recyclerView)
        recycler.adapter = this.adapter
    }

    @Suppress("unused")
    fun clear() {
        swapItems(listOf())
    }
}

