package com.san.kir.manger.ui.application_navigation.library.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.data.datastore.MainRepository
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MainMenuDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.PlannedDao
import com.san.kir.data.db.dao.SiteDao
import com.san.kir.data.db.dao.StorageDao
import com.san.kir.core.utils.coroutines.defaultDispatcher
import com.san.kir.manger.utils.enums.DownloadState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val mainMenuDao: com.san.kir.data.db.dao.MainMenuDao,
    private val mangaDao: com.san.kir.data.db.dao.MangaDao,
    private val storageDao: com.san.kir.data.db.dao.StorageDao,
    private val categorDao: com.san.kir.data.db.dao.CategoryDao,
    private val siteDao: com.san.kir.data.db.dao.SiteDao,
    private val chapterDao: com.san.kir.data.db.dao.ChapterDao,
    private val plannedDao: com.san.kir.data.db.dao.PlannedDao,
    mainRepository: MainRepository,
) : ViewModel() {
    val editMenu = mainRepository.data.map { it.editMenu }.flowOn(com.san.kir.core.utils.coroutines.defaultDispatcher)
    fun loadMainMenuItems() = mainMenuDao.loadItems().flowOn(com.san.kir.core.utils.coroutines.defaultDispatcher)
    fun loadLibraryCounts() = mangaDao.loadItems().map { it.size }.flowOn(com.san.kir.core.utils.coroutines.defaultDispatcher)
    fun loadStorageSizes() =
        storageDao.flowItems().map { list -> list.sumOf { item -> item.sizeFull } }
            .flowOn(com.san.kir.core.utils.coroutines.defaultDispatcher)

    fun loadCategoriesCount() = categorDao.loadItems().map { it.size }.flowOn(com.san.kir.core.utils.coroutines.defaultDispatcher)
    fun loadSiteCatalogSize() = siteDao.loadItems().map { it.size }.flowOn(com.san.kir.core.utils.coroutines.defaultDispatcher)
    fun loadSiteCatalogVolume() =
        siteDao.loadItems().map { l -> l.sumOf { s -> s.volume } }.flowOn(com.san.kir.core.utils.coroutines.defaultDispatcher)

    fun loadDownloadCount() = chapterDao.loadAllItems().map { l ->
        l.filter { d -> d.status == DownloadState.QUEUED || d.status == DownloadState.LOADING }
    }.map { it.size }.flowOn(com.san.kir.core.utils.coroutines.defaultDispatcher)

    fun loadLatestCount() =
        chapterDao.loadAllItems().map { it.size }.flowOn(com.san.kir.core.utils.coroutines.defaultDispatcher)

    fun loadPlannedCount() = plannedDao.loadItems().map { it.size }.flowOn(com.san.kir.core.utils.coroutines.defaultDispatcher)
    fun swapMenuItems(from: Int, to: Int) {
        viewModelScope.launch {
            val items = mainMenuDao.getItems().toMutableList()
            Collections.swap(items, from, to)
            items.onEachIndexed { i, m -> m.order = i }
            mainMenuDao.update(*items.toTypedArray())
        }
    }
}
