package com.san.kir.manger.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.StatisticRepository
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.MangaStatistic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.max

class ViewerViewModel(app: Application) : AndroidViewModel(app) {
    private val mStatisticRepository = StatisticRepository(app)
    private val mChapterRepository = ChapterRepository(app)

    fun updateStatisticInfo(mangaName: String, time: Long) {
        GlobalScope.launch(Dispatchers.Default) {
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
    fun getStatisticItem(mangaName: String) = mStatisticRepository.getItem(mangaName)
    fun statisticUpdate(stats: MangaStatistic) = mStatisticRepository.update(stats)
    suspend fun update(chapter: Chapter) = mChapterRepository.update(chapter)
}

