package com.san.kir.manger.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.room.entities.authorsList
import com.san.kir.manger.room.entities.genresList
import com.san.kir.manger.room.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class AbstractMangaViewModel(app: Application): AndroidViewModel(app) {
    private val mangaDao by lazy { getDatabase(app).mangaDao }

    fun isContainManga(item: SiteCatalogElement): Boolean =
        mangaDao.getItems().any { it.shortLink == item.shotLink }

    fun onlineUpdate(item: SiteCatalogElement) {
        viewModelScope.launch(Dispatchers.Default) {
            val oldManga = mangaDao.getItems().first { it.shortLink == item.shotLink }
            val updItem = ManageSites.getFullElement(item)
            oldManga.authorsList = updItem.authors
            oldManga.logo = updItem.logo
            oldManga.about = updItem.about
            oldManga.genresList = updItem.genres
            oldManga.host = updItem.host
            oldManga.shortLink = updItem.shotLink
            oldManga.status = updItem.statusEdition
            mangaDao.update(oldManga)
            getApplication<Application>().longToast("Информация о манге ${item.name} обновлена")
        }
    }
}
