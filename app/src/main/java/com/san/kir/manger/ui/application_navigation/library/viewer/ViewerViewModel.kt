package com.san.kir.manger.ui.application_navigation.library.viewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.san.kir.core.utils.coroutines.defaultLaunchInVM
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.StatisticDao
import com.san.kir.data.models.Chapter
import com.san.kir.data.models.Manga
import com.san.kir.data.models.MangaStatistic
import com.san.kir.manger.utils.ChapterComparator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val chapterDao: ChapterDao,
    private val statisticDao: StatisticDao,
) : ViewModel() {
    private val chapterKey = "chapterKetSaveState"

    fun getChapter() = savedStateHandle.get<Chapter>(chapterKey)
    fun setChapter(chapter: Chapter) = savedStateHandle.set(chapterKey, chapter)
    fun clearChapter() = savedStateHandle.remove<Chapter>(chapterKey)

    fun updateStatisticInfo(mangaName: String, time: Long) {
        defaultLaunchInVM {
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
        com.san.kir.core.utils.coroutines.withDefaultContext {
            chapterDao.getItemsWhereManga(mangaName)
        }

    suspend fun getStatisticItem(mangaName: String) = statisticDao.getItem(mangaName)
    suspend fun statisticUpdate(stats: MangaStatistic) = statisticDao.update(stats)
    suspend fun update(chapter: Chapter) = chapterDao.update(chapter)

    suspend fun getFirstNotReadChapter(manga: Manga): Chapter? =
        com.san.kir.core.utils.coroutines.withDefaultContext {
            var list = chapterDao.getItemsWhereManga(manga.name)

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

    suspend fun getFirstChapter(manga: Manga): Chapter =
        com.san.kir.core.utils.coroutines.withDefaultContext {
            var list = chapterDao.getItemsWhereManga(manga.name)

            if (manga.isAlternativeSort) {
                list = list.sortedWith(ChapterComparator())
            }

            val chapter = list.first()
            chapter.progress = 0
            update(chapter)

            list.first()
        }
}

