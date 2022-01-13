package com.san.kir.features.shikimori.ui.catalog_item

import androidx.compose.animation.core.updateTransition
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.log
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.ShikimoriDao
import com.san.kir.data.models.base.ShikiManga
import com.san.kir.data.models.base.ShikimoriAccount
import com.san.kir.data.models.datastore.ShikimoriAuth
import com.san.kir.data.models.extend.SimplefiedMangaWithChapterCounts
import com.san.kir.data.models.utils.ChapterComparator
import com.san.kir.data.store.TokenStore
import com.san.kir.features.shikimori.Repository
import com.san.kir.features.shikimori.ui.util.fuzzy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ShikiItemViewModel @Inject internal constructor(
    private val shikimoriDao: ShikimoriDao,
    private val mangaDao: MangaDao,
    private val chapterDao: ChapterDao,
    private val manager: Repository,
    store: TokenStore,
) : ViewModel() {
    private val _hasForegroundWork = MutableStateFlow(false)
    val hasForegroundWork = _hasForegroundWork.asStateFlow()

    private val auth = store.data
        .stateIn(viewModelScope, SharingStarted.Eagerly, ShikimoriAuth())

    private val _id = MutableStateFlow(0L)

    fun update(id: Long) {
        _id.value = id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val item = _id
        .flatMapLatest { id -> shikimoriDao.loadItem(id) }
        .filterNotNull()
        .onEach { _hasForegroundWork.value = false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ShikiManga())

    private val _dialog = MutableStateFlow<Dialog>(Dialog.None)
    val dialog = _dialog.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val localSearch: StateFlow<LocalSearch> = item
        .filter { it.name.isNotEmpty() }
        .flatMapLatest { item ->
            when (item.libMangaId) {
                -1L -> searchInLocal(item.manga.russian, item.manga.english.first())
                else -> local(item.libMangaId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), LocalSearch.Searching)

    // Поиск в БД подходящей по названию манги
    // Так как сайт предоставляет названия на русском и английском,
    // то используем оба для повышения точности поиска
    private fun searchInLocal(name: String, name2: String): Flow<LocalSearch> {
        return shikimoriDao.loadLibraryItems().map { list ->
            val filtered = list.map { manga ->
                // сравниеваем названия с помочью нечеткого сравнения
                val fuzzy1 = manga.name fuzzy name
                val fuzzy2 = manga.name fuzzy name2

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
                LocalSearch.NotFounds(name)
            } else {
                // Возвращение отсортированного списка
                LocalSearch.Founds(
                    filtered
                        .sortedBy { (fuzzy, _) -> fuzzy }
                        .map { (_, manga) -> manga }
                )
            }
        }
    }

    // Получение связанной с элементом манги
    private fun local(id: Long): Flow<LocalSearch> {
        return mangaDao.itemWhereId(id)
            .filterNotNull()
            .map { m -> LocalSearch.Sync(m) }
    }

    // Вывод запроса на подтверждение,
    // если чиcло глав на сайте не совпадает с числом глав элемента библиотеке
    fun checkAllChapters(currentManga: SimplefiedMangaWithChapterCounts) {
        when (currentManga.all) {
            item.value.all -> checkReadChapters(currentManga)
            else -> {
                _dialog.value = Dialog.DifferentChapterCount(
                    manga = currentManga,
                    local = currentManga.all,
                    online = item.value.all
                )
            }
        }
    }

    // Вывод запроса на подтверждение,
    // если число прочитанных глав на сайте не совпадает с числом глав элемента библиотеке
    fun checkReadChapters(currentManga: SimplefiedMangaWithChapterCounts) {
        when (currentManga.read) {
            item.value.read -> launchSync(currentManga, false)
            else -> {
                _dialog.value = Dialog.DifferentReadCount(
                    manga = currentManga,
                    local = currentManga.read,
                    online = item.value.read
                )
            }
        }
    }

    // Взависимости от выбранного источника правды, происходит связывание элементов и обновление
    fun launchSync(manga: SimplefiedMangaWithChapterCounts, onlineIsTruth: Boolean) =
        viewModelScope.defaultLaunch {
            dissmissDialog()
            _hasForegroundWork.value = true

            shikimoriDao.update(
                item.value.copy(libMangaId = manga.id)
            )

            // Если выбрана локальная манга, то обновляем на сайте информацию
            if (onlineIsTruth.not()) {
                val newRate = item.value.rate.copy(
                    chapters = manga.read,
                    status = ShikimoriAccount.Status.Watching
                )
                manager.update(auth.value, newRate)

                updateDataFromNetwork()
            } else {
                // Если выбрана манга с сайта, то меняем у глав статус на прочитанный
                var list = chapterDao.getItemsWhereManga(manga.name)
                if (manga.sort) {
                    list = list.sortedWith(ChapterComparator())
                }
                list = list.take(item.value.read.toInt())
                    .onEach { chapter -> chapter.isRead = true }

                chapterDao.update(list)
            }
        }

    fun dissmissDialog() {
        _dialog.value = Dialog.None
    }

    fun updateDataFromNetwork() = viewModelScope.defaultLaunch {
        _hasForegroundWork.value = true
        val newRate = manager.rate(auth.value, item.value.rate)
        shikimoriDao.update(item.value.copy(rate = newRate))
    }

    fun cancelDialog() {
        _dialog.value = Dialog.CancelSync
    }

    fun cancelSync() = viewModelScope.defaultLaunch {
        dissmissDialog()
        _hasForegroundWork.value = true
        shikimoriDao.update(
            item.value.copy(libMangaId = -1L)
        )
        val newRate = item.value.rate.copy(
            status = ShikimoriAccount.Status.OnHold
        )
        manager.update(auth.value, newRate)
        updateDataFromNetwork()
    }
}
