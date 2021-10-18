package com.san.kir.manger.components.viewer

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.di.DefaultDispatcher
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.StatisticRepository
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.utils.ChapterComparator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class ViewerViewModel @Inject constructor(
    app: Application,
    private val savedStateHandle: SavedStateHandle,
    @DefaultDispatcher private val default: CoroutineDispatcher
) : ViewModel() {
    private val mStatisticRepository = StatisticRepository(app)
    private val mChapterRepository = ChapterRepository(app)

    private val chapterKey = "chapterKetSaveState"

    fun getChapter() = savedStateHandle.get<Chapter>(chapterKey)
    fun setChapter(chapter: Chapter) = savedStateHandle.set(chapterKey, chapter)
    fun clearChapter() = savedStateHandle.remove<Chapter>(chapterKey)

    fun updateStatisticInfo(mangaName: String, time: Long) {
        viewModelScope.launch(default) {
            val stats = mStatisticRepository.getItem(mangaName)
            stats.lastTime = time
            stats.allTime = stats.allTime + time
            stats.maxSpeed =
                max(stats.maxSpeed, (stats.lastPages / (time.toFloat() / 60)).toInt())
            stats.openedTimes = stats.openedTimes + 1
            mStatisticRepository.update(stats)
        }
    }

    suspend fun getChapterItems(mangaName: String) =
        withContext(default) { mChapterRepository.getItems(mangaName) }

    suspend fun getStatisticItem(mangaName: String) = mStatisticRepository.getItem(mangaName)
    suspend fun statisticUpdate(stats: MangaStatistic) = mStatisticRepository.update(stats)
    suspend fun update(chapter: Chapter) = mChapterRepository.update(chapter)

    suspend fun getFirstNotReadChapter(manga: Manga): Chapter? = withContext(default) {
        var list = mChapterRepository.getItems(mangaUnic = manga.unic)

        list = if (manga.isAlternativeSort) {
            try {
                list.sortedWith(ChapterComparator())
            } catch (e: Exception) {
                list
            }
        } else {
            list
        }

        list.firstOrNull { !it.isRead }
    }

    suspend fun getFirstChapter(manga: Manga): Chapter = withContext(default) {
        var list = mChapterRepository.getItems(mangaUnic = manga.unic)

        if (manga.isAlternativeSort) {
            list = list.sortedWith(ChapterComparator())
        }

        val chapter = list.first()
        chapter.progress = 0
        update(chapter)

        list.first()
    }
}

