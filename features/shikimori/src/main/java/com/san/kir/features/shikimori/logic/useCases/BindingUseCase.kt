package com.san.kir.features.shikimori.logic.useCases

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.models.base.ShikiDbManga
import com.san.kir.data.models.base.ShikimoriMangaItem
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import com.san.kir.features.shikimori.logic.fuzzy
import com.san.kir.features.shikimori.logic.repo.ItemsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class BindingUseCase(private val itemsRepository: ItemsRepository) {
    // кеширование данных для ускорения проверки
    private var repositoryItems: List<ShikimoriMangaItem> = emptyList()

    fun <T : ShikimoriMangaItem> prepareData(): suspend (List<T>) -> List<BindStatus<T>> =
        { list ->
            initRepositoryItems()

            list.filter { checkNoBind(it) }
                .sortedBy { item -> item.name }
                .map { item -> BindStatus(item, CanBind.Check) }
        }

    fun <T : ShikimoriMangaItem> checkBinding(): suspend (List<BindStatus<T>>) -> Flow<CheckingStatus<T>> =
        { list ->
            flow {
                initRepositoryItems()

                val mutList = list.toMutableList()

                repeat(list.size) { index ->
                    val item = mutList[index]

                    val fuzzyCount = repositoryItems
                        .filter { checkNoBind(item.item) }
                        .map { manga2 -> item.item fuzzy manga2 }
                        .any { fuzzy -> fuzzy.second }

                    mutList[index] = item.copy(status = if (fuzzyCount) CanBind.Ok else CanBind.No)

                    delay(40L)

                    emit(
                        CheckingStatus(
                            items = mutList.sortedBy { (_, can) -> can.ordinal },
                            progress = index.toFloat() / list.size
                        )
                    )
                }

                emit(CheckingStatus(items = null, progress = null))
            }
        }

    fun <T : ShikimoriMangaItem> filterData(): suspend (List<T>) -> List<T> =
        { list ->
            if (list.isEmpty()) {
                list
            } else {
                initRepositoryItems()

                list.filterNot { checkNoBind(it) }
                    .sortedBy { item -> item.name }
            }
        }

    private suspend fun initRepositoryItems() = withIoContext {
        if (repositoryItems.isEmpty())
            repositoryItems = itemsRepository.items()
    }

    private fun checkNoBind(item: ShikimoriMangaItem): Boolean {
        return when (item) {
            is ShikiDbManga -> item.libMangaId == -1L
            is SimplifiedMangaWithChapterCounts ->
                repositoryItems.firstOrNull { (it as ShikiDbManga).libMangaId == item.id } == null
            else -> false
        }
    }
}

internal data class BindStatus<T : ShikimoriMangaItem>(
    val item: T,
    val status: CanBind
)

internal data class CheckingStatus<T : ShikimoriMangaItem>(
    val items: List<BindStatus<T>>?,
    val progress: Float?,
)

internal enum class CanBind {
    Already, Ok, No, Check
}
