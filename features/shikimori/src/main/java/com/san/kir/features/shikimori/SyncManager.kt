package com.san.kir.features.shikimori

import com.san.kir.data.models.base.ShikimoriMangaItem
import com.san.kir.data.models.base.ShikimoriRate
import com.san.kir.data.models.base.ShikimoriStatus
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import com.san.kir.data.models.utils.ChapterComparator
import com.san.kir.features.shikimori.repositories.ChapterRepository
import com.san.kir.features.shikimori.repositories.ProfileItemRepository
import com.san.kir.features.shikimori.ui.accountRate.DialogState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

class SyncManager @Inject internal constructor(
    private val profileItemRepository: ProfileItemRepository,
    private val chapterRepository: ChapterRepository,
) {
    // Состояние опроса
    private val _dialogState = MutableStateFlow<DialogState>(DialogState.None)
    val dialogState = _dialogState.asStateFlow()

    // Сброс состояния
    fun dialogNone() {
        _dialogState.value = DialogState.None
    }

    // запрос на отмену связывания объектов
    fun askCancelSync() {
        _dialogState.value = DialogState.CancelSync
    }

    // Взависимости от выбранного источника правды, происходит связывание элементов и обновление
    suspend fun launchSync(
        profileRate: ShikimoriRate,
        libraryManga: ShikimoriMangaItem,
        onlineIsTruth: Boolean,
    ) {
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
            var list = chapterRepository.itemsByManga(libraryManga.name)
            if (libraryManga is SimplifiedMangaWithChapterCounts && libraryManga.sort) {
                list = list.sortedWith(ChapterComparator())
            }
            list = list.take(profileRate.chapters.toInt())
                .onEach { chapter -> chapter.isRead = true }

            chapterRepository.update(list)
        }
    }

    fun initSync(libraryManga: ShikimoriMangaItem) {
        Timber.i(
            "initSync\n" +
                    "libraryManga is ${libraryManga.name}"
        )

        _dialogState.value = DialogState.Init(libraryManga)
    }

    suspend fun cancelSync(profileRate: ShikimoriRate) {
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
                _dialogState.value = DialogState.DifferentReadCount(
                    manga = libraryManga,
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
                _dialogState.value = DialogState.DifferentChapterCount(
                    manga = libraryManga,
                    local = libraryManga.all,
                    online = mangaAllChapters
                )
            }
        }
    }
}
