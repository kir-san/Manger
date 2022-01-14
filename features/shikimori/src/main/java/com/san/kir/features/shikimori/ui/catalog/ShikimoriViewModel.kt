package com.san.kir.features.shikimori.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.data.db.dao.ShikimoriDao
import com.san.kir.data.models.base.ShikiManga
import com.san.kir.data.models.base.ShikimoriAccount
import com.san.kir.data.models.datastore.ShikimoriAuth
import com.san.kir.data.models.extend.SimplefiedMangaWithChapterCounts
import com.san.kir.data.store.TokenStore
import com.san.kir.features.shikimori.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShikimoriViewModel @Inject internal constructor(
    private val store: TokenStore,
    private val manager: Repository,
    private val shikimoriDao: ShikimoriDao,
) : ViewModel() {
    val auth = store.data
        .stateIn(viewModelScope, SharingStarted.Eagerly, ShikimoriAuth())

    @OptIn(ExperimentalCoroutinesApi::class)
    val onlineCatalog = auth
        .flatMapLatest { auth ->
            if (auth.isLogin) {
                shikimoriDao
                    .items()
                    .onStart { updateDataFromNetwork() }
            } else {
                flowOf(emptyList())
            }
        }
        // Обновление данных после запуска
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val localCatalog = shikimoriDao
        .loadLibraryItems()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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

            dbItem = dbItem.copy(manga = manager.manga(auth.value, rate))

            shikimoriDao.update(dbItem)

            delay(300L) // искуственное замедление, чтобы не превышать ограничения сервера
        }
    }

    fun logout() = viewModelScope.launch {
        store.setLogin(false)
        shikimoriDao.clearAll()
    }

    fun checkSyncedItem(item: ShikimoriAccount.AbstractMangaItem): StateFlow<Boolean> {
        return when (item) {
            is ShikiManga -> checkSyncedItem(item)
            is SimplefiedMangaWithChapterCounts -> checkSyncedItem(item)
            else -> MutableStateFlow(false).asStateFlow()
        }
    }

    private fun checkSyncedItem(item: ShikiManga): StateFlow<Boolean> {
        return MutableStateFlow(item.libMangaId != -1L).asStateFlow()
    }

    private fun checkSyncedItem(item: SimplefiedMangaWithChapterCounts): StateFlow<Boolean> {
        val state = MutableStateFlow(false)

        viewModelScope.defaultLaunch {
            state.update {
                shikimoriDao.itemWhereLibId(item.id) != null
            }
        }

        return state.asStateFlow()
    }
}
