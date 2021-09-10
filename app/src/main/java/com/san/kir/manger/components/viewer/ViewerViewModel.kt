package com.san.kir.manger.components.viewer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.utils.ChapterComparator
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.StatisticRepository
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.MangaStatistic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max

class ViewerViewModel(app: Application) : AndroidViewModel(app) {
    private val mStatisticRepository = StatisticRepository(app)
    private val mChapterRepository = ChapterRepository(app)

    fun updateStatisticInfo(mangaName: String, time: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            val stats = mStatisticRepository.getItem(mangaName)
            stats.lastTime = time
            stats.allTime = stats.allTime + time
            stats.maxSpeed =
                max(stats.maxSpeed, (stats.lastPages / (time.toFloat() / 60)).toInt())
            stats.openedTimes = stats.openedTimes + 1
            mStatisticRepository.update(stats)
        }
    }

    suspend fun getChapterItems(mangaName: String) = mChapterRepository.getItems(mangaName)
    suspend fun getStatisticItem(mangaName: String) = mStatisticRepository.getItem(mangaName)
    suspend fun statisticUpdate(stats: MangaStatistic) = mStatisticRepository.update(stats)
    suspend fun update(chapter: Chapter) = mChapterRepository.update(chapter)

    suspend fun getFirstNotReadChapter(manga: Manga): Chapter? {
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

        return list.firstOrNull { !it.isRead }
    }

    suspend fun getFirstChapter(manga: Manga): Chapter {
        var list = mChapterRepository.getItems(mangaUnic = manga.unic)

        if (manga.isAlternativeSort) {
            list = list.sortedWith(ChapterComparator())
        }

        val chapter = list.first()
        chapter.progress = 0
        update(chapter)

        return list.first()
    }
}

