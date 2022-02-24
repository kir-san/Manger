package com.san.kir.features.shikimori.ui.catalog_item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.ShikimoriDao
import com.san.kir.data.models.base.ShikiManga
import com.san.kir.data.models.base.ShikimoriAccount
import com.san.kir.data.models.datastore.ShikimoriAuth
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import com.san.kir.data.models.utils.ChapterComparator
import com.san.kir.data.store.TokenStore
import com.san.kir.features.shikimori.Repository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn


abstract class CatalogItemViewModel internal constructor(
    private val shikimoriDao: ShikimoriDao,
    private val mangaDao: MangaDao,
    private val chapterDao: ChapterDao,
    private val manager: Repository,
    store: TokenStore,
) : ViewModel() {

    val auth = store.data
        .stateIn(viewModelScope, SharingStarted.Eagerly, ShikimoriAuth())

    val _id = MutableStateFlow(0L)

    fun update(id: Long) {
        _id.value = id
    }

    abstract val item: StateFlow<ShikimoriAccount.AbstractMangaItem>
    abstract val syncState: StateFlow<SyncState>

    private val _hasForegroundWork = MutableStateFlow(false)
    val hasForegroundWork = _hasForegroundWork.asStateFlow()

    fun disableForegroundWork() {
        _hasForegroundWork.value = false
    }

    fun enableForegroundWork() {
        _hasForegroundWork.value = false
    }

    private val _askState = MutableStateFlow<AskState>(AskState.None)
    val askState = _askState.asStateFlow()

    // Сброс состояния
    fun askNone() {
        _askState.value = AskState.None
    }

    // запрос на отмену связывания объектов
    fun askCancelSync() {
        _askState.value = AskState.CancelSync
    }

    // Взависимости от выбранного источника правды, происходит связывание элементов и обновление
    private fun _launchSync(
        shikiManga: ShikiManga,
        libManga: SimplifiedMangaWithChapterCounts,
        onlineIsTruth: Boolean,
    ) = viewModelScope.defaultLaunch {
        askNone()
        enableForegroundWork()

        // Связывание элементов в базе данных
        shikimoriDao.update(
            shikiManga.copy(libMangaId = libManga.id)
        )

        // Если выбрана локальная манга, то обновляем на сайте информацию
        if (onlineIsTruth.not()) {
            val newRate = shikiManga.rate.copy(
                chapters = libManga.read,
                status = ShikimoriAccount.Status.Watching
            )
            manager.update(auth.value, newRate)

            updateDataFromNetwork()
        } else {
            // Если выбрана манга с сайта, то меняем у глав статус на прочитанный
            var list = chapterDao.getItemsWhereManga(libManga.name)
            if (libManga.sort) {
                list = list.sortedWith(ChapterComparator())
            }
            list = list.take(shikiManga.read.toInt())
                .onEach { chapter -> chapter.isRead = true }

            chapterDao.update(list)
        }
    }

    fun launchSync(
        currentManga: ShikimoriAccount.AbstractMangaItem,
        onlineIsTruth: Boolean,
    ) {
        when (currentManga) {
            is ShikiManga -> _launchSync(
                shikiManga = currentManga,
                libManga = item.value as SimplifiedMangaWithChapterCounts,
                onlineIsTruth
            )
            is SimplifiedMangaWithChapterCounts -> _launchSync(
                shikiManga = item.value as ShikiManga,
                libManga = currentManga,
                onlineIsTruth
            )
        }

    }

    abstract fun updateDataFromNetwork(): Job

    fun cancelSync() = viewModelScope.defaultLaunch {
        askNone()
        enableForegroundWork()

        getShikiManga()?.let { shiki ->
            shikimoriDao.update(
                shiki.copy(libMangaId = -1L)
            )

            val newRate = shiki.rate.copy(
                status = ShikimoriAccount.Status.OnHold
            )

            manager.update(auth.value, newRate)
        }

        updateDataFromNetwork()
    }

    abstract suspend fun getShikiManga(): ShikiManga?

    // Вывод запроса на подтверждение,
    // если число прочитанных глав на сайте не совпадает с числом глав элемента библиотеке
    fun checkReadChapters(currentManga: ShikimoriAccount.AbstractMangaItem) {
        when (currentManga.read) {
            item.value.read -> launchSync(currentManga, false)
            else -> {
                _askState.value = AskState.DifferentReadCount(
                    manga = currentManga,
                    local = currentManga.read,
                    online = item.value.read
                )
            }
        }
    }

    // Вывод запроса на подтверждение,
    // если чиcло глав на сайте не совпадает с числом глав элемента библиотеке
    fun checkAllChapters(currentManga: ShikimoriAccount.AbstractMangaItem) {
        when (currentManga.all) {
            item.value.all -> checkReadChapters(currentManga)
            else -> {
                _askState.value = AskState.DifferentChapterCount(
                    manga = currentManga,
                    local = currentManga.all,
                    online = item.value.all
                )
            }
        }
    }
}
