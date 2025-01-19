package com.san.kir.catalog.ui.catalogs

import com.san.kir.background.logic.UpdateCatalogManager
import com.san.kir.background.logic.di.updateCatalogManager
import com.san.kir.core.utils.ManualDI
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.set
import com.san.kir.core.utils.viewModel.Action
import com.san.kir.core.utils.viewModel.ViewModel
import com.san.kir.data.catalogsRepository
import com.san.kir.data.db.catalog.repo.CatalogsRepository
import com.san.kir.data.parsing.SiteCatalogsManager
import com.san.kir.data.parsing.siteCatalogsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlin.math.abs

internal class CatalogsViewModel(
    private val catalogsRepository: CatalogsRepository = ManualDI.catalogsRepository(),
    private val siteCatalogsManager: SiteCatalogsManager = ManualDI.siteCatalogsManager(),
    private val manager: UpdateCatalogManager = ManualDI.updateCatalogManager(),
) : ViewModel<CatalogsState>(), CatalogsStateHolder {

    private var job: Job? = null
    private val items = MutableStateFlow(emptyList<CheckableSite>())
    private val background = MutableStateFlow(false)

    override val tempState = combine(items, background, ::CatalogsState)
    override val defaultState = CatalogsState()

    init {
        updateItemData()
    }

    override suspend fun onAction(action: Action) {
        when (action) {
            CatalogsAction.UpdateData -> updateItemData()
            CatalogsAction.UpdateContent -> {
                siteCatalogsManager.catalog.forEach { manager.addTask(it.name) }
            }
        }
    }

    private fun updateItemData() {
        val temp = siteCatalogsManager.catalog
            .map {
                it to CheckableSite(
                    name = it.name,
                    host = it.catalogName,
                    volume = VolumeState.Load,
                    state = SiteState.Load
                )
            }

        items.update { temp.map { it.second } }
        setUpdateCatalogsListener()

        temp.forEachIndexed { index, (catalog, _) ->
            defaultLaunch {
                val volume = runCatching { catalog.init() }.getOrNull()?.volume
                items.update { list ->
                    val result = if (volume != null) SiteState.Ok else SiteState.Error
                    val currentSite = list[index]
                    list.set(index, currentSite.copy(state = result))
                }

                val dbVolume = runCatching { catalogsRepository.volume(catalog.name) }.getOrNull()
                items.update { list ->
                    val result =
                        if (dbVolume != null) {
                            val diff = (volume ?: 0) - dbVolume
                            VolumeState.Ok(dbVolume, abs(diff), diff > 0)
                        } else {
                            VolumeState.Error
                        }

                    val currentSite = list[index]
                    list.set(index, currentSite.copy(volume = result))
                }
            }
        }
    }

    private fun setUpdateCatalogsListener() {
        job?.cancel()
        job = defaultLaunch {
            manager.loadTasks().collect { tasks -> background.update { tasks.isNotEmpty() } }
        }
    }
}
