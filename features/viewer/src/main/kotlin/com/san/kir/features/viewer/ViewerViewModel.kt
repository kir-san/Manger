package com.san.kir.features.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.StatisticDao
import com.san.kir.data.models.base.Settings
import com.san.kir.data.parsing.SiteCatalogsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.concurrent.timer
import kotlin.math.max

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class ViewerViewModel @Inject constructor(
    val settingsRepository: SettingsRepository,
    val chaptersManager: ChaptersManager,
    private val siteCatalogManager: SiteCatalogsManager,
    private val chapterDao: ChapterDao,
    private val statisticDao: StatisticDao,
    private val mangaDao: MangaDao,
) : ViewModel() {

    // Переключение видимости интерфейса
    private val _visibleUI = MutableStateFlow(false)
    val visibleUI = _visibleUI.asStateFlow()

    fun toogleVisibilityUI(state: Boolean = _visibleUI.value.not()) {
        _visibleUI.update { state }
    }

    // Хранение способов листания глав
    val control = settingsRepository.viewer().mapLatest { it.control }
        .stateIn(viewModelScope, SharingStarted.Lazily, Settings.Viewer.Control())

    // инициализация данных
    private var isInitManager = false
    fun init(chapterId: Long) = viewModelScope.defaultLaunch {
        if (isInitManager) return@defaultLaunch // единовременная инициализация
        isInitManager = true

        val mangaName = chapterDao.getMangaName(chapterId)
        val manga = mangaDao.itemByName(mangaName)

        chaptersManager.init(manga, chapterId)
    }

    // Хранение времени начала чтения и работа с ним
    private var _startReadTime = 0L

    fun initReadTime() {
        _startReadTime = System.currentTimeMillis()
    }

    fun setReadTime() = viewModelScope.defaultLaunch {
        val time = (System.currentTimeMillis() - _startReadTime) / 1000
        if (time > 0) {
            statisticDao.update(
                chaptersManager.statisticItem.copy(
                    lastTime = time,
                    allTime = chaptersManager.statisticItem.allTime + time,
                    maxSpeed = max(
                        a = chaptersManager.statisticItem.maxSpeed,
                        b = (chaptersManager.statisticItem.lastPages / (time.toFloat() / 60)).toInt()
                    ),
                    openedTimes = chaptersManager.statisticItem.openedTimes + 1
                )
            )
        }
    }

    // Счетчик времени
    fun getStopWatch(): StateFlow<Int> {
        val timerFlow = MutableStateFlow(-1)

        timer(name = "readTimeStopWatch", period = 60_000) {
            timerFlow.update { old -> old + 1 }
        }

        return timerFlow.asStateFlow()
    }

    // Хранение границ экрана в пределах которых работают нажатия на экран
    private var leftPart = 0
    private var rightPart = 0

    fun clickOnScreen(xPosition: Float) = viewModelScope.launch {
        // Включен ли режим управления нажатиями на экран
        if (control.value.taps) {
            when {
                xPosition < leftPart -> // Нажатие на левую часть экрана
                    chaptersManager.prevPage() // Предыдущая страница
                xPosition > rightPart -> // Нажатие на правую часть
                    chaptersManager.nextPage() // Следущая страница
            }
        }

        // Если нажатие по центральной части
        // Переключение видимости баров
        if (xPosition.toInt() in (leftPart + 1) until rightPart) {
            toogleVisibilityUI()
        }
    }

    fun setScreenWidth(width: Int) {
        leftPart = width * 2 / 5
        rightPart = width * 3 / 5
    }

    // Обновление списка страниц для текущей главы
    fun updatePagesForChapter() = viewModelScope.launch {
        val chapter = chaptersManager.currentState.currentChapter
        chapter.pages = siteCatalogManager.pages(chapter)
        chapterDao.update(chapter)
        chaptersManager.updateCurrentChapter(chapter)
    }
}

