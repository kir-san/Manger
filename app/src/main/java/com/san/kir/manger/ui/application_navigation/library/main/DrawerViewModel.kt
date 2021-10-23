package com.san.kir.manger.ui.application_navigation.library.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.data.datastore.MainRepository
import com.san.kir.manger.di.DefaultDispatcher
import com.san.kir.manger.room.dao.*
import com.san.kir.manger.utils.enums.DownloadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val mainMenuDao: MainMenuDao,
    private val mangaDao: MangaDao,
    private val storageDao: StorageDao,
    private val categorDao: CategoryDao,
    private val siteDao: SiteDao,
    private val chapterDao: ChapterDao,
    private val plannedDao: PlannedDao,
    private val mainRepository: MainRepository,
    @DefaultDispatcher private val default: CoroutineDispatcher,
) : ViewModel() {
    val editMenu = mainRepository.data.map { it.editMenu }.flowOn(default)
    fun loadMainMenuItems() = mainMenuDao.loadItems().flowOn(default)
    fun loadLibraryCounts() = mangaDao.loadItems().map { it.size }.flowOn(default)
    fun loadStorageSizes() =
        storageDao.flowItems().map { list -> list.sumOf { item -> item.sizeFull } }
            .flowOn(default)

    fun loadCategoriesCount() = categorDao.loadItems().map { it.size }.flowOn(default)
    fun loadSiteCatalogSize() = siteDao.loadItems().map { it.size }.flowOn(default)
    fun loadSiteCatalogVolume() =
        siteDao.loadItems().map { l -> l.sumOf { s -> s.volume } }.flowOn(default)

    fun loadDownloadCount() = chapterDao.loadAllItems().map { l ->
        l.filter { d -> d.status == DownloadState.QUEUED || d.status == DownloadState.LOADING }
    }.map { it.size }.flowOn(default)

    fun loadLatestCount() =
        chapterDao.loadAllItems().map { it.size }.flowOn(default)

    fun loadPlannedCount() = plannedDao.loadItems().map { it.size }.flowOn(default)
    fun swapMenuItems(from: Int, to: Int) {
        viewModelScope.launch {
            val items = mainMenuDao.getItems().toMutableList()
            Collections.swap(items, from, to)
            items.onEachIndexed { i, m -> m.order = i }
            mainMenuDao.update(*items.toTypedArray())
        }
    }
}
