package com.san.kir.features.shikimori.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.data.db.dao.ShikimoriDao
import com.san.kir.data.models.base.ShikiManga
import com.san.kir.data.models.datastore.ShikimoriAuth
import com.san.kir.data.store.TokenStore
import com.san.kir.features.shikimori.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ShikimoriViewModel @Inject internal constructor(
    private val store: TokenStore,
    private val manager: Repository,
    private val shikimoriDao: ShikimoriDao,
) : ViewModel() {
    val auth = store.data
        .stateIn(viewModelScope, SharingStarted.Eagerly, ShikimoriAuth())

    val onlineCatalog = auth
        .flatMapLatest { auth ->
            if (auth.isLogin) {
                shikimoriDao
                    .items()
                    .mapLatest { items ->
                        items
                            .sortedBy { item -> item.name }
                            .groupBy { item -> item.libMangaId != -1L }
                    }
                    .onStart { updateDataFromNetwork() }
            } else {
                flowOf(emptyMap())
            }
        }
        // Обновление данных после запуска
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    val localCatalog = shikimoriDao
        .loadLibraryItems()
        .mapLatest { items ->
            items.groupBy { item ->
                shikimoriDao.itemWhereLibId(item.id) != null
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    fun updateDataFromNetwork() = viewModelScope.defaultLaunch {
        val rates = manager.userMangas(auth.value)

        // Получение данных из сети и сохранение в базе данных
        rates.forEach { rate ->
            var dbItem = shikimoriDao.item(rate.target_id) ?: run {
                // Отсутствующие элементы сразу добавляются
                val newItem = ShikiManga(target_id = rate.target_id, rate = rate)
                shikimoriDao.insert(newItem)
                newItem
            }

            manager.manga(auth.value, rate)?.let { shikiManga ->
                dbItem = dbItem.copy(manga = shikiManga)
                shikimoriDao.update(dbItem)
            }
        }
    }

    fun logout() = viewModelScope.launch {
        store.setLogin(false)
        shikimoriDao.clearAll()
    }
}
