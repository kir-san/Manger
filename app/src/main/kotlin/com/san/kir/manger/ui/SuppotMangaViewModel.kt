package com.san.kir.manger.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.core.utils.coroutines.withMainContext
import com.san.kir.core.utils.longToast
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.base.SiteCatalogElement
import com.san.kir.data.parsing.SiteCatalogsManager
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
            mangaDao.items().any {
                it.shortLink.contains(item.shotLink)
            }
        }

    fun onlineUpdate(item: SiteCatalogElement) {
        viewModelScope.defaultLaunch {
            val oldManga = mangaDao.items().first { it.shortLink.contains(item.shotLink) }
            val updItem = manager.getFullElement(item)
            mangaDao.update(
                oldManga.copy(
                    authorsList = updItem.authors,
                    logo = updItem.logo,
                    about = updItem.about,
                    genresList = updItem.genres,
                    host = updItem.host,
                    shortLink = updItem.shotLink,
                    status = updItem.statusEdition,
                )
            )
            withMainContext {
                application.longToast("Информация о манге ${item.name} обновлена")
            }
        }
    }

    suspend fun fullElement(element: SiteCatalogElement): SiteCatalogElement {
        return manager.getFullElement(element)
    }
}
