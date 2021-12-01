package com.san.kir.manger.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.manger.components.parsing.SiteCatalogsManager
import com.san.kir.manger.data.room.dao.MangaDao
import com.san.kir.manger.data.room.entities.SiteCatalogElement
import com.san.kir.manger.data.room.entities.authorsList
import com.san.kir.manger.data.room.entities.genresList
import com.san.kir.manger.utils.coroutines.defaultLaunchInVM
import com.san.kir.manger.utils.coroutines.withDefaultContext
import com.san.kir.manger.utils.coroutines.withMainContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SuppotMangaViewModel @Inject constructor(
    private val application: Application,
    private val mangaDao: MangaDao,
    private val manager: SiteCatalogsManager,
) : ViewModel() {

    suspend fun isContainManga(item: SiteCatalogElement): Boolean =
        withDefaultContext {
            mangaDao.getItems().any {
                it.shortLink.contains(item.shotLink)
            }
        }

    fun onlineUpdate(item: SiteCatalogElement) {
        defaultLaunchInVM {
            val oldManga = mangaDao.getItems().first { it.shortLink.contains(item.shotLink) }
            val updItem = manager.getFullElement(item)
            oldManga.authorsList = updItem.authors
            oldManga.logo = updItem.logo
            oldManga.about = updItem.about
            oldManga.genresList = updItem.genres
            oldManga.host = updItem.host
            oldManga.shortLink = updItem.shotLink
            oldManga.status = updItem.statusEdition
            mangaDao.update(oldManga)
            withMainContext {
                application.longToast("Информация о манге ${item.name} обновлена")
            }
        }
    }

    suspend fun fullElement(element: SiteCatalogElement): SiteCatalogElement {
        return manager.getFullElement(element)
    }
}
