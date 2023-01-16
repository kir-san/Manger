package com.san.kir.features.shikimori.logic

import com.san.kir.data.models.base.ShikimoriMangaItem
import com.san.kir.data.models.base.ShikimoriRate
import com.san.kir.data.models.base.ShikimoriStatus
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import com.san.kir.data.models.utils.ChapterComparator
import com.san.kir.features.shikimori.logic.repo.ChapterRepository
import com.san.kir.features.shikimori.logic.repo.ProfileItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

internal class SyncManager @Inject internal constructor(
    private val profileItemRepository: ProfileItemRepository,
    private val chapterRepository: ChapterRepository,
) {
    // Состояние опроса
    private val _dialogState = MutableStateFlow<SyncDialogState>(SyncDialogState.None)
    val dialogState = _dialogState.asStateFlow()

    private var bindChangeAction: suspend () -> Unit = {}
    private var beforeBindChangeAction: suspend () -> Unit = {}

    fun beforeBindChange(action: suspend () -> Unit) {
        beforeBindChangeAction = action
    }

    fun onBindChange(action: suspend () -> Unit) {
        bindChangeAction = action
    }

    // Сброс состояния
    fun dialogNone() {
        _dialogState.value = SyncDialogState.None
    }

    // запрос на отмену связывания объектов
    fun askCancelSync(rate: ShikimoriRate) {
        _dialogState.value = SyncDialogState.CancelSync(rate)
    }

    // Взависимости от выбранного источника правды, происходит связывание элементов и обновление
    suspend fun launchSync(
        profileRate: ShikimoriRate,
        libraryManga: ShikimoriMangaItem,
        onlineIsTruth: Boolean,
    ) {
        beforeBindChangeAction()

        Timber.i(
            "launchSync\n" +
                    "profileRate is ${profileRate.targetId}\n" +
                    "libraryManga is ${libraryManga.name}\n" +
                    "onlineIsTruth is $onlineIsTruth"
        )

        dialogNone()

        // Связывание элементов в базе данных
        profileItemRepository.bindItem(profileRate, libraryManga.id)

        // Если выбрана локальная манга, то обновляем на сайте информацию
        if (onlineIsTruth.not()) {
            val newRate = profileRate.copy(
                chapters = libraryManga.read,
                status = ShikimoriStatus.Watching
            )
            profileItemRepository.update(newRate)
        } else {
            // Если выбрана манга с сайта, то меняем у глав статус на прочитанный
            var list = chapterRepository.itemsByMangaId(libraryManga.id)
            if (libraryManga is SimplifiedMangaWithChapterCounts && libraryManga.sort) {
                list = list.sortedWith(ChapterComparator())
            }
            list = list.take(profileRate.chapters.toInt())
                .map { chapter -> chapter.copy(isRead = true) }

            chapterRepository.update(list)
        }

        bindChangeAction()
    }

    fun initSync(libraryManga: ShikimoriMangaItem) {
        Timber.i(
            "initSync\n" +
                    "libraryManga is ${libraryManga.name}"
        )

        _dialogState.value = SyncDialogState.Init(libraryManga)
    }

    suspend fun cancelSync(profileRate: ShikimoriRate) {
        beforeBindChangeAction()

        Timber.i(
            "cancelSync\n" +
                    "profileRate is ${profileRate.targetId}"
        )

        dialogNone()

        profileItemRepository.unbindItem(profileRate)

        val newRate = profileRate.copy(
            status = ShikimoriStatus.OnHold
        )

        profileItemRepository.update(newRate)

        bindChangeAction()
    }

    // Вывод запроса на подтверждение,
    // если число прочитанных глав на сайте не совпадает с числом глав элемента библиотеке
    suspend fun checkReadChapters(
        profileRate: ShikimoriRate,
        libraryManga: ShikimoriMangaItem,
    ) {
        Timber.i(
            "checkReadChapters\n" +
                    "profileRate is ${profileRate.targetId}\n" +
                    "libraryManga is ${libraryManga.name}"
        )

        when (libraryManga.read) {
            profileRate.chapters -> launchSync(profileRate, libraryManga, false)
            else -> {
                _dialogState.value = SyncDialogState.DifferentReadCount(
                    manga = libraryManga,
                    profileRate = profileRate,
                    local = libraryManga.read,
                    online = profileRate.chapters
                )
            }
        }
    }

    // Вывод запроса на подтверждение,
    // если чиcло глав на сайте не совпадает с числом глав элемента библиотеке
    suspend fun checkAllChapters(
        mangaAllChapters: Long,
        profileRate: ShikimoriRate,
        libraryManga: ShikimoriMangaItem,
    ) {
        Timber.i(
            "checkAllChapters\n" +
                    "mangaAllChapters is $mangaAllChapters\n" +
                    "profileRate is ${profileRate.targetId}\n" +
                    "libraryManga is ${libraryManga.name}"
        )

        when (libraryManga.all) {
            mangaAllChapters -> checkReadChapters(profileRate, libraryManga)
            else -> {
                _dialogState.value = SyncDialogState.DifferentChapterCount(
                    manga = libraryManga,
                    profileRate = profileRate,
                    local = libraryManga.all,
                    online = mangaAllChapters
                )
            }
        }
    }
}

// Запросы при выполнении связывания
internal sealed interface SyncDialogState {
    object None : SyncDialogState

    data class Init(
        val manga: ShikimoriMangaItem,
    ) : SyncDialogState

    data class DifferentChapterCount(
        val manga: ShikimoriMangaItem,
        val profileRate: ShikimoriRate,
        val local: Long,
        val online: Long,
    ) : SyncDialogState

    data class DifferentReadCount(
        val manga: ShikimoriMangaItem,
        val profileRate: ShikimoriRate,
        val local: Long,
        val online: Long,
    ) : SyncDialogState

    data class CancelSync(
        val rate: ShikimoriRate
    ) : SyncDialogState
}

internal sealed interface SyncDialogEvent {
    data class SyncToggle(
        val item: ShikimoriMangaItem
    ) : SyncDialogEvent

    data class SyncNext(
        val onlineIsTruth: Boolean = false
    ) : SyncDialogEvent

    object DialogDismiss : SyncDialogEvent
    data class SyncCancel(
        val rate: ShikimoriRate
    ) : SyncDialogEvent
}
