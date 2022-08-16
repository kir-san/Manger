package com.san.kir.features.shikimori.useCases

import com.san.kir.data.models.base.ShikiDbManga
import com.san.kir.data.models.base.ShikimoriMangaItem
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import com.san.kir.features.shikimori.repositories.ItemsRepository
import com.san.kir.features.shikimori.ui.util.fuzzy

class BindingHelper(
    private val itemsRepository: ItemsRepository,
) {
    fun <T : ShikimoriMangaItem> prepareData(): suspend (List<T>) -> List<BindStatus<T>> =
        { list ->
            list.filter { checkNoBind(it) }
                .sortedBy { item -> item.name }
                .map { item -> BindStatus(item, CanBind.Check) }
        }

    fun <T : ShikimoriMangaItem> checkBinding(): suspend (List<BindStatus<T>>) -> List<BindStatus<T>> =
        { list ->
            val dataList = itemsRepository.items()
            list.map { (manga, _) ->
                val fuzzyCount = dataList
                    .filter { checkNoBind(it) }
                    .map { manga2 -> manga fuzzy manga2 }
                    .any { fuzzy -> fuzzy.second }
                val canBind = if (fuzzyCount) CanBind.Ok else CanBind.No
                BindStatus(manga, canBind)
            }.sortedBy { (_, can) -> can.ordinal }
        }

    fun <T : ShikimoriMangaItem> filterData(): suspend (List<T>) -> List<T> =
        { list ->
            list.filterNot { checkNoBind(it) }
                .sortedBy { item -> item.name }
        }

    private suspend fun checkNoBind(item: ShikimoriMangaItem): Boolean {
        return when (item) {
            is ShikiDbManga -> item.libMangaId == -1L
            is SimplifiedMangaWithChapterCounts -> {
                val itemWhereLibId = itemsRepository.itemById(item.id)
                itemWhereLibId == null
            }
            else -> false
        }
    }
}

data class BindStatus<T : ShikimoriMangaItem>(
    val item: T,
    val status: CanBind
)

enum class CanBind {
    Already, Ok, No, Check
}
