package com.san.kir.manger.components.viewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.di.DefaultDispatcher
import com.san.kir.manger.room.dao.ChapterDao
import com.san.kir.manger.room.dao.StatisticDao
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.utils.ChapterComparator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val chapterDao: ChapterDao,
    private val statisticDao: StatisticDao,
    @DefaultDispatcher private val default: CoroutineDispatcher
) : ViewModel() {
    private val chapterKey = "chapterKetSaveState"

    fun getChapter() = savedStateHandle.get<Chapter>(chapterKey)
    fun setChapter(chapter: Chapter) = savedStateHandle.set(chapterKey, chapter)
    fun clearChapter() = savedStateHandle.remove<Chapter>(chapterKey)

    fun updateStatisticInfo(mangaName: String, time: Long) {
        viewModelScope.launch(default) {
            val stats = statisticDao.getItem(mangaName)
            stats.lastTime = time
            stats.allTime = stats.allTime + time
            stats.maxSpeed =
                max(stats.maxSpeed, (stats.lastPages / (time.toFloat() / 60)).toInt())
            stats.openedTimes = stats.openedTimes + 1
            statisticDao.update(stats)
        }
    }

    suspend fun getChapterItems(mangaName: String) =
        withContext(default) { chapterDao.getItemsWhereManga(mangaName) }

    suspend fun getStatisticItem(mangaName: String) = statisticDao.getItem(mangaName)
    suspend fun statisticUpdate(stats: MangaStatistic) = statisticDao.update(stats)
    suspend fun update(chapter: Chapter) = chapterDao.update(chapter)

    suspend fun getFirstNotReadChapter(manga: Manga): Chapter? = withContext(default) {
        var list = chapterDao.getItemsWhereManga(manga.unic)

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
        var list = chapterDao.getItemsWhereManga(manga.unic)

        if (manga.isAlternativeSort) {
            list = list.sortedWith(ChapterComparator())
        }

        val chapter = list.first()
        chapter.progress = 0
        update(chapter)

        list.first()
    }
}

