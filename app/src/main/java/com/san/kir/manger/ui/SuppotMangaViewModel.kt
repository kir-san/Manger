package com.san.kir.manger.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.data.parsing.SiteCatalogsManager
import com.san.kir.manger.data.room.entities.SiteCatalogElement
import com.san.kir.manger.data.room.entities.authorsList
import com.san.kir.manger.data.room.entities.genresList
import com.san.kir.core.utils.coroutines.defaultLaunchInVM
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.core.utils.coroutines.withMainContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SuppotMangaViewModel @Inject constructor(
    private val application: Application,
    private val mangaDao: com.san.kir.data.db.dao.MangaDao,
    private val manager: com.san.kir.data.parsing.SiteCatalogsManager,
) : ViewModel() {

    suspend fun isContainManga(item: SiteCatalogElement): Boolean =
        com.san.kir.core.utils.coroutines.withDefaultContext {
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
            com.san.kir.core.utils.coroutines.withMainContext {
                application.longToast("Информация о манге ${item.name} обновлена")
            }
        }
    }

    suspend fun fullElement(element: SiteCatalogElement): SiteCatalogElement {
        return manager.getFullElement(element)
    }
}
