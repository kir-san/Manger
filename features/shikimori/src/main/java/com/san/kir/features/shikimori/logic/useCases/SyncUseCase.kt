package com.san.kir.features.shikimori.logic.useCases

import com.san.kir.data.models.base.ShikiDbManga
import com.san.kir.data.models.base.ShikimoriMangaItem
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import com.san.kir.features.shikimori.logic.fuzzy
import com.san.kir.features.shikimori.logic.repo.ItemsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

internal class SyncUseCase<T : ShikimoriMangaItem>(
    private val oppositeItemsRepository: ItemsRepository,
) {

    // Состояние привязки
    private val _syncState = MutableStateFlow<SyncState>(SyncState.None)
    val syncState = _syncState.asStateFlow()

    private var _item: T? = null

    suspend fun launchSyncCheck(item: T? = _item) {
        Timber.v("launchSyncCheck")

        if (item == null) return
        _item = item

        findingSyncCheck()

        _syncState.value = if (checkBind(item)) {
            when (item) {
                is ShikiDbManga -> getSyncedItem(item.libMangaId)
                is SimplifiedMangaWithChapterCounts -> getSyncedItem(item.id)
                else -> SyncState.None
            }
        } else {
            searchForSync(item)
        }
    }

    fun findingSyncCheck() {
        Timber.i("findingSyncCheck")

        _syncState.value = SyncState.Finding
    }

    fun cancelSyncCheck() {
        Timber.i("cancelSyncCheck")

        _syncState.value = SyncState.None
    }

    // Получение связанной с элементом манги
    private suspend fun getSyncedItem(id: Long): SyncState {
        Timber.v("getSyncedItem with id $id")

        oppositeItemsRepository.itemById(id)?.let { manga ->
            Timber.v("getSyncedItem finished with $manga")
            return SyncState.Ok(manga)
        } ?: return SyncState.Ok(SimplifiedMangaWithChapterCounts())
    }

    // Поиск подходящей по названию манги
    // Так как сайт предоставляет названия на русском и английском,
    // то используем оба для повышения точности поиска
    private suspend fun searchForSync(manga: T): SyncState {
        Timber.v("searchForSync with $manga")

        val filtered = oppositeItemsRepository.items().map { manga2 ->
            // сравниваем названия с помочью нечеткого сравнения
            val fuzzy = manga fuzzy manga2
            fuzzy to manga2
            // отсеиваем все несовпадения
        }.filter { (fuzzy, _) -> fuzzy.second }

        Timber.v("filtered is $filtered")

        return if (filtered.isEmpty()) {
            // Возвращение пустого результата
            SyncState.NotFounds(manga.name)
        } else {
            // Возвращение отсортированного списка
            SyncState.Founds(
                filtered
                    .sortedBy { (fuzzy, _) -> fuzzy.first }
                    .map { (_, manga) -> manga }
            )
        }
    }

    private suspend fun checkBind(item: ShikimoriMangaItem): Boolean {
        return when (item) {
            is ShikiDbManga -> item.libMangaId != -1L
            is SimplifiedMangaWithChapterCounts -> oppositeItemsRepository.itemById(item.id) != null
            else -> false
        }
    }
}

// Состояние связывания
internal sealed interface SyncState {
    object None : SyncState
    class Ok(val manga: ShikimoriMangaItem) : SyncState
    object Finding : SyncState
    class Founds(val items: List<ShikimoriMangaItem>) : SyncState
    class NotFounds(val name: String) : SyncState
}
