package com.san.kir.manger.ui.application_navigation.library.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.room.dao.CategoryDao
import com.san.kir.manger.room.dao.ChapterDao
import com.san.kir.manger.room.dao.DownloadDao
import com.san.kir.manger.room.dao.MainMenuDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.dao.PlannedDao
import com.san.kir.manger.room.dao.SiteDao
import com.san.kir.manger.room.dao.StorageDao
import com.san.kir.manger.utils.enums.DownloadStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
    private val downloadDao: DownloadDao,
    private val chapterDao: ChapterDao,
    private val plannedDao: PlannedDao,
) : ViewModel() {
    fun loadMainMenuItems() = mainMenuDao.loadItems().flowOn(Dispatchers.Default)
    fun loadLibraryCounts() = mangaDao.flowItems().map { it.size }.flowOn(Dispatchers.Default)
    fun loadStorageSizes() =
        storageDao.flowItems().map { list -> list.sumOf { item -> item.sizeFull } }
            .flowOn(Dispatchers.Default)

    fun loadCategoriesCount() = categorDao.loadItems().map { it.size }.flowOn(Dispatchers.Default)
    fun loadSiteCatalogSize() = siteDao.loadItems().map { it.size }.flowOn(Dispatchers.Default)
    fun loadSiteCatalogVolume() =
        siteDao.loadItems().map { l -> l.sumOf { s -> s.volume } }.flowOn(Dispatchers.Default)

    fun loadDownloadCount() = downloadDao.flowItems().map { l ->
        l.filter { d -> d.status == DownloadStatus.queued || d.status == DownloadStatus.loading }
    }.map { it.size }.flowOn(Dispatchers.Default)

    fun loadLatestCount() =
        chapterDao.loadAllItems().map { it.size }.flowOn(Dispatchers.Default)

    fun loadPlannedCount() = plannedDao.loadItems().map { it.size }.flowOn(Dispatchers.Default)
    fun swapMenuItems(from: Int, to: Int) {
        viewModelScope.launch {
            val items = mainMenuDao.getItems().toMutableList()
            Collections.swap(items, from, to)
            items.onEachIndexed { i, m -> m.order = i }
            mainMenuDao.update(*items.toTypedArray())
        }
    }
}
