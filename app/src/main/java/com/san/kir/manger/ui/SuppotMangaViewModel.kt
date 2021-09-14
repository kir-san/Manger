package com.san.kir.manger.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.manger.components.parsing.SiteCatalogsManager
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.room.entities.authorsList
import com.san.kir.manger.room.entities.genresList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SuppotMangaViewModel @Inject constructor(
    private val application: Application,
    private val mangaDao: MangaDao,
    private val manager: SiteCatalogsManager
) : ViewModel() {

    suspend fun isContainManga(item: SiteCatalogElement): Boolean =
        withContext(Dispatchers.Default) {
            mangaDao.getItems().any { it.shortLink == item.shotLink }
        }

    fun onlineUpdate(item: SiteCatalogElement) {
        viewModelScope.launch(Dispatchers.Default) {
            val oldManga = mangaDao.getItems().first { it.shortLink == item.shotLink }
            val updItem = manager.getFullElement(item)
            oldManga.authorsList = updItem.authors
            oldManga.logo = updItem.logo
            oldManga.about = updItem.about
            oldManga.genresList = updItem.genres
            oldManga.host = updItem.host
            oldManga.shortLink = updItem.shotLink
            oldManga.status = updItem.statusEdition
            mangaDao.update(oldManga)
            withContext(Dispatchers.Main) {
                application.longToast("Информация о манге ${item.name} обновлена")
            }
        }
    }

    suspend fun fullElement(element: SiteCatalogElement): SiteCatalogElement {
        return manager.getFullElement(element)
    }
}
