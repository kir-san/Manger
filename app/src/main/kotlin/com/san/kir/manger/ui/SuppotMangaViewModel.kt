package com.san.kir.manger.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.core.utils.coroutines.withMainContext
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.SiteCatalogElement
import com.san.kir.data.models.authorsList
import com.san.kir.data.models.genresList
import com.san.kir.data.parsing.SiteCatalogsManager
import com.san.kir.core.utils.longToast
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
        viewModelScope.defaultLaunch {
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
