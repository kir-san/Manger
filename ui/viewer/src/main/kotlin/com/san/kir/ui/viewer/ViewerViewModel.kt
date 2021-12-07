package com.san.kir.ui.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.log
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.StatisticDao
import com.san.kir.data.models.Viewer
import com.san.kir.data.store.ViewerStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
internal class ViewerViewModel @Inject constructor(
    val store: ViewerStore,
    val savedChapter: SavedChapter,
    val chaptersManager: ChaptersManager,
    private val chapterDao: ChapterDao,
    private val statisticDao: StatisticDao,
    private val mangaDao: MangaDao,
    private val loadImage: LoadImage,
) : ViewModel() {

    private val _visibleUI = MutableStateFlow(false)
    val visibleUI = _visibleUI.asStateFlow()

    fun toogleVisibilityUI(state: Boolean = _visibleUI.value.not()) {
        _visibleUI.update { state }
    }

    private val _control = MutableStateFlow(Viewer.Control())
    val control = _control.asStateFlow()

    init {
        store.data
            .onEach { store ->
                _control.update { store.control }
            }
            .launchIn(viewModelScope)
    }

    fun init(chapterId: Long) = viewModelScope.defaultLaunch {
        val mangaName = chapterDao.getMangaName(chapterId)
        val manga = mangaDao.item(mangaName)

        chaptersManager.init(manga, chapterId)
    }

    private val _readTime = MutableStateFlow(0L)

    fun initReadTime() {
        _readTime.update { System.currentTimeMillis() }
    }

    fun setReadTime() {
        viewModelScope.defaultLaunch {
            val time = (System.currentTimeMillis() - _readTime.value) / 1000
            if (time > 0) {
                val stats = statisticDao.getItem(chaptersManager.currentChapter.value.manga)
                stats.lastTime = time
                stats.allTime = stats.allTime + time
                stats.maxSpeed =
                    max(stats.maxSpeed, (stats.lastPages / (time.toFloat() / 60)).toInt())
                stats.openedTimes = stats.openedTimes + 1
                statisticDao.update(stats)
            }
        }
    }

    private val _screenParts = MutableStateFlow(0 to 0)
    val screenParts = _screenParts.asStateFlow()

    fun setScreenWidth(width: Int) {
        val leftPart = width * 2 / 5
        val rightPart = width * 3 / 5
        _screenParts.update { leftPart to rightPart }
    }

    fun loadImage(page: Page.Current?, force: Boolean = false) =
        loadImage.flow(page, force)
}

