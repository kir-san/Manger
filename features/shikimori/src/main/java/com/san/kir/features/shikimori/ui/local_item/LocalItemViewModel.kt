package com.san.kir.features.shikimori.ui.local_item

import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.ShikimoriDao
import com.san.kir.data.models.base.ShikiManga
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import com.san.kir.data.store.TokenStore
import com.san.kir.features.shikimori.Repository
import com.san.kir.features.shikimori.ui.catalog_item.CatalogItemViewModel
import com.san.kir.features.shikimori.ui.catalog_item.SyncState
import com.san.kir.features.shikimori.ui.util.fuzzy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LocalItemViewModel @Inject internal constructor(
    private val shikimoriDao: ShikimoriDao,
    private val mangaDao: MangaDao,
    private val chapterDao: ChapterDao,
    private val manager: Repository,
    store: TokenStore,
) : CatalogItemViewModel(
    shikimoriDao, mangaDao, chapterDao, manager, store
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val item = _id
        .flatMapLatest { id -> shikimoriDao.loadLibraryItem(id) }
        .filterNotNull()
        .onEach { disableForegroundWork() }
        .stateIn(viewModelScope,
                 SharingStarted.WhileSubscribed(),
                 SimplifiedMangaWithChapterCounts())

    @OptIn(ExperimentalCoroutinesApi::class)
    override val syncState: StateFlow<SyncState> = item
        .filter { it.name.isNotEmpty() }
        .flatMapLatest { item ->
            if (shikimoriDao.itemWhereLibId(item.id) != null) {
                local(item.id)
            } else {
                searchInLocal(item.name)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SyncState.Find)

    // Поиск в БД подходящей по названию манги
    // Так как сайт предоставляет названия на русском и английском,
    // то используем оба для повышения точности поиска
    private fun searchInLocal(name: String): Flow<SyncState> {
        return shikimoriDao.items().map { list ->
            val filtered = list.map { manga ->

                // сравниваем названия с помочью нечеткого сравнения
                val fuzzy1 = manga.name fuzzy name
                val fuzzy2 = (manga.manga.english?.first() ?: "") fuzzy name

                // Если хотя бы одно из них дало положительный результат
                // то находим значение наилучшего совпадения
                if (fuzzy1.second || fuzzy2.second) {
                    maxOf(fuzzy1.first, fuzzy2.first) to manga
                } else {
                    0.0 to manga
                }
                // отсеиваем все несовпадения
            }.filter { (fuzzy, _) -> fuzzy > 0.0 }

            if (filtered.isEmpty()) {
                // Возвращение пустого результата
                SyncState.NotFounds(name)
            } else {
                // Возвращение отсортированного списка
                SyncState.Founds(
                    filtered
                        .sortedBy { (fuzzy, _) -> fuzzy }
                        .map { (_, manga) -> manga }
                )
            }
        }
    }

    // Получение связанной с элементом манги
    private fun local(id: Long): Flow<SyncState> {
        return shikimoriDao.loadItemWhereLibId(id)
            .filterNotNull()
            .map { m -> SyncState.Ok(m) }
    }

    override fun updateDataFromNetwork() = viewModelScope.defaultLaunch {
        enableForegroundWork()
        shikimoriDao.item(item.value.id)?.let { shmanga ->
            val newRate = manager.rate(auth.value, shmanga.rate)
            shikimoriDao.update(shmanga.copy(rate = newRate))
        }
    }

    override suspend fun getShikiManga(): ShikiManga? {
        return shikimoriDao.item(item.value.id)
    }
}
