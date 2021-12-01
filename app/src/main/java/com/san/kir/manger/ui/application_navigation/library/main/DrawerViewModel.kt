package com.san.kir.manger.ui.application_navigation.library.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.data.datastore.MainRepository
import com.san.kir.manger.data.room.dao.CategoryDao
import com.san.kir.manger.data.room.dao.ChapterDao
import com.san.kir.manger.data.room.dao.MainMenuDao
import com.san.kir.manger.data.room.dao.MangaDao
import com.san.kir.manger.data.room.dao.PlannedDao
import com.san.kir.manger.data.room.dao.SiteDao
import com.san.kir.manger.data.room.dao.StorageDao
import com.san.kir.manger.utils.coroutines.defaultDispatcher
import com.san.kir.manger.utils.enums.DownloadState
import dagger.hilt.android.lifecycle.HiltViewModel
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
    mainRepository: MainRepository,
) : ViewModel() {
    val editMenu = mainRepository.data.map { it.editMenu }.flowOn(defaultDispatcher)
    fun loadMainMenuItems() = mainMenuDao.loadItems().flowOn(defaultDispatcher)
    fun loadLibraryCounts() = mangaDao.loadItems().map { it.size }.flowOn(defaultDispatcher)
    fun loadStorageSizes() =
        storageDao.flowItems().map { list -> list.sumOf { item -> item.sizeFull } }
            .flowOn(defaultDispatcher)

    fun loadCategoriesCount() = categorDao.loadItems().map { it.size }.flowOn(defaultDispatcher)
    fun loadSiteCatalogSize() = siteDao.loadItems().map { it.size }.flowOn(defaultDispatcher)
    fun loadSiteCatalogVolume() =
        siteDao.loadItems().map { l -> l.sumOf { s -> s.volume } }.flowOn(defaultDispatcher)

    fun loadDownloadCount() = chapterDao.loadAllItems().map { l ->
        l.filter { d -> d.status == DownloadState.QUEUED || d.status == DownloadState.LOADING }
    }.map { it.size }.flowOn(defaultDispatcher)

    fun loadLatestCount() =
        chapterDao.loadAllItems().map { it.size }.flowOn(defaultDispatcher)

    fun loadPlannedCount() = plannedDao.loadItems().map { it.size }.flowOn(defaultDispatcher)
    fun swapMenuItems(from: Int, to: Int) {
        viewModelScope.launch {
            val items = mainMenuDao.getItems().toMutableList()
            Collections.swap(items, from, to)
            items.onEachIndexed { i, m -> m.order = i }
            mainMenuDao.update(*items.toTypedArray())
        }
    }
}
