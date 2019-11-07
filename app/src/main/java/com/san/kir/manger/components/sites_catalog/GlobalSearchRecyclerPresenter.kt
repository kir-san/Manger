package com.san.kir.manger.components.sites_catalog

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.extensions.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class GlobalSearchRecyclerPresenter(private val act: GlobalSearchActivity) : RecyclerPresenter() {

    private val adapter = RecyclerViewAdapterFactory.createSimple { GlobalSearchItemView(act) }

    private var searchText = "" // Сохранение информации о поисковом запросе
    private var backupCatalog = listOf<SiteCatalogElement>()
    private var mainJob: Job = Job()

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        recycler.adapter = this.adapter
    }

    fun loadSites(end: ((Int) -> Unit)? = null) {
        if (mainJob.isActive) mainJob.cancel()
        mainJob = act.lifecycleScope.launch(Dispatchers.Default) {
            try {
                ManageSites
                    .CATALOG_SITES
                    .forEach {
                        backupCatalog = backupCatalog + act.mViewModel.items(it)
                    }

                adapter.items = backupCatalog

                changeOrder()

                end?.invoke(adapter.itemCount)
            } catch (e: Exception) {
                log("loadSites send exception: $e")
                loadSites(end)
            }
        }
    }

    fun close() {
        runBlocking {
            mainJob.cancel()
        }
    }

    fun changeOrder(searchText: String = this.searchText): Int {
        var list = backupCatalog

        // Обработка поискового запроса
        if (searchText.isNotEmpty()) {
            list = list.filter { it.name.contains(searchText, true) }
        }


        // Запоминание полученных данных
        this.searchText = searchText

        // Обработка направления сортировки и обновление адаптера
        swapItems(list) // Изменить порядок списка, если надо по условию

        return list.size
    }

    private fun swapItems(newCatalog: List<SiteCatalogElement>): Job {
        adapter.items = newCatalog
        return act.lifecycleScope.launch(Dispatchers.Main) {
            adapter.notifyDataSetChanged()
        }
    }
}
