package com.san.kir.features.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.ManualDI
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.data.chapterRepository
import com.san.kir.data.db.main.repo.ChapterRepository
import com.san.kir.data.db.main.repo.MangaRepository
import com.san.kir.data.db.main.repo.SettingsRepository
import com.san.kir.data.db.main.repo.StatisticsRepository
import com.san.kir.data.mangaRepository
import com.san.kir.data.models.main.Settings
import com.san.kir.data.settingsRepository
import com.san.kir.data.statisticsRepository
import com.san.kir.features.viewer.logic.ChaptersManager
import com.san.kir.features.viewer.logic.di.chaptersManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlin.concurrent.timer
import kotlin.math.max
import kotlin.math.roundToInt

internal class ViewerViewModel(
    val chaptersManager: ChaptersManager = ManualDI.chaptersManager(),
    val settingsRepository: SettingsRepository = ManualDI.settingsRepository(),
    private val chapterRepository: ChapterRepository = ManualDI.chapterRepository(),
    private val statisticsRepository: StatisticsRepository = ManualDI.statisticsRepository(),
    private val mangaRepository: MangaRepository = ManualDI.mangaRepository(),
) : ViewModel() {

    // Переключение видимости интерфейса
    private val _visibleUI = MutableStateFlow(VisibleState())
    val visibleUI = _visibleUI.asStateFlow()

    fun toggleVisibilityUI(state: Boolean = _visibleUI.value.isShown.not(), force: Boolean = false) {
        _visibleUI.value = VisibleState(state, force)
    }

    // Хранение способов листания глав
    val control = settingsRepository.control.stateIn(viewModelScope, SharingStarted.Lazily, Settings.Viewer.Control())

    val hasScrollbars = settingsRepository.useScrollbars.stateIn(viewModelScope, SharingStarted.Lazily, false)

    // инициализация данных
    private var isInitManager = false
    fun init(chapterId: Long) = viewModelScope.defaultLaunch {
        if (isInitManager) return@defaultLaunch // единовременная инициализация
        isInitManager = true

        val mangaId = chapterRepository.mangaIdById(chapterId)
        val manga = mangaRepository.item(mangaId)!!

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
            statisticsRepository.save(
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

        timer(name = "readTimeStopWatch", period = 60_000) { timerFlow.value++ }

        return timerFlow.asStateFlow()
    }

    // Хранение границ экрана в пределах которых работают нажатия на экран
    private var leftPart = 0
    private var rightPart = 0

    fun clickOnScreen(xPosition: Float) = viewModelScope.defaultLaunch {
        // Включен ли режим управления нажатиями на экран
        if (control.value.taps) {
            when {
                xPosition < leftPart -> chaptersManager.prevPage()
                xPosition > rightPart -> chaptersManager.nextPage()
            }
        }

        // Если нажатие по центральной части
        // Переключение видимости баров
        if (xPosition.toInt() in (leftPart + 1) until rightPart) toggleVisibilityUI()
    }

    fun setScreenWidth(width: Int) {
        leftPart = (width * LEFT_SCREEN_PART).roundToInt()
        rightPart = (width * RIGHT_SCREEN_PART).roundToInt()
    }

    // Обновление списка страниц для текущей главы
    fun updatePagesForChapter() = viewModelScope.defaultLaunch { chaptersManager.updatePagesForChapter(force = true) }

    companion object {
        private const val LEFT_SCREEN_PART = 2 / 5f
        private const val RIGHT_SCREEN_PART = 3 / 5f
    }
}

internal data class VisibleState(val isShown: Boolean = true, val force: Boolean = true)
