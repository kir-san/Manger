package com.san.kir.manger.components.statistics

import android.content.Context
import android.graphics.Typeface
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.room.models.MangaStatistic
import com.san.kir.manger.utils.TimeFormat
import com.san.kir.manger.utils.bytesToMb
import com.san.kir.manger.utils.formatDouble
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.applyRecursively
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.padding
import org.jetbrains.anko.support.v4.nestedScrollView
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout

class StatisticItemFullView(private val manga: MangaStatistic) :
    AnkoComponent<StatisticItemActivity> {
    override fun createView(ui: AnkoContext<StatisticItemActivity>) = with(ui) {
        frameLayout {
            nestedScrollView {
                verticalLayout {
                    padding = dip(10)

                    textView(R.string.statistic_item_full_last_session) {
                        textSize = 15f
                    }

                    textView(
                        context.timePagesData(manga.lastTime, manga.lastPages, manga.lastChapters)
                    ) {
                        setTypeface(typeface, Typeface.BOLD)
                    }

                    textView(context.speedReading(manga.lastPages, manga.lastTime)) {
                        setTypeface(typeface, Typeface.BOLD)
                    }

                    textView(R.string.statistic_item_full_all) {
                        topPadding = dip(15)
                    }
                    textView(
                        context.timePagesData(manga.allTime, manga.allPages, manga.allChapters)
                    ) {
                        setTypeface(typeface, Typeface.BOLD)
                    }
                    textView(context.averageSpeedReading(manga.allPages, manga.allTime)) {
                        setTypeface(typeface, Typeface.BOLD)
                    }
                    textView(context.maxSpeedReading(manga.maxSpeed)) {
                        setTypeface(typeface, Typeface.BOLD)
                    }
                    textView(context.downloadData(manga.downloadSize, manga.downloadTime)) {
                        setTypeface(typeface, Typeface.BOLD)
                    }
                    textView(context.readTimes(manga.openedTimes)) {
                        setTypeface(typeface, Typeface.BOLD)
                    }
                    textView(
                        context.averageSession(
                            manga.openedTimes,
                            manga.allTime,
                            manga.allPages,
                            manga.allChapters
                        )
                    ) {
                        setTypeface(typeface, Typeface.BOLD)
                    }
                }.applyRecursively {
                    when (it) {
                        is TextView -> {
                            it.bottomPadding = dip(8)
                            it.textSize = 16f
                        }
                    }
                }
            }
        }
    }

    private fun Context.timePagesData(time: Long, pages: Int, chapters: Int): String {
        val timeString = TimeFormat(time).toString(this@timePagesData)
        return if (pages == 0) {
            getString(R.string.statistic_item_full_for_time_nothing, timeString)
        } else {
            val pageString = resources.getQuantityString(
                R.plurals.statistic_item_full_pages,
                pages
            )
            if (chapters == 0) {
                getString(
                    R.string.statistic_item_full_for_time_with_pages,
                    timeString,
                    pages, pageString
                )
            } else {
                val chapterString = resources.getQuantityString(
                    R.plurals.statistic_item_full_chapters,
                    chapters
                )
                getString(
                    R.string.statistic_item_full_for_time_with_pages_chapters,
                    timeString,
                    pages,
                    pageString,
                    chapters,
                    chapterString
                )
            }
        }
    }

    private fun Context.speedReading(pages: Int, time: Long): String {
        val speed = (pages / (time.toFloat() / 60)).toInt()
        return if (speed == 0) {
            getString(R.string.statistic_item_full_speed_nothing)
        } else {
            val pageString = resources.getQuantityString(
                R.plurals.statistic_item_full_pages,
                speed
            )
            getString(R.string.statistic_item_full_speed_reading, speed, pageString)
        }
    }

    private fun Context.averageSpeedReading(pages: Int, time: Long): String {
        val speed = (pages / (time.toFloat() / 60)).toInt()
        return if (speed == 0) {
            getString(R.string.statistic_item_full_average_speed_nothing)
        } else {
            val pageString = resources.getQuantityString(
                R.plurals.statistic_item_full_pages,
                speed
            )
            getString(R.string.statistic_item_full_average_speed_reading, speed, pageString)
        }
    }

    private fun Context.maxSpeedReading(speed: Int): String {
        val pageString = resources.getQuantityString(
            R.plurals.statistic_item_full_pages,
            speed
        )
        return getString(R.string.statistic_item_full_max_speed, speed, pageString)
    }

    private fun Context.downloadData(size: Long, time: Long): String {
        return getString(
            R.string.statistic_item_full_download_data,
            formatDouble(bytesToMb(size)),
            TimeFormat(time / 1000).toString(this)
        )
    }

    private fun Context.readTimes(times: Int): String {
        val timesString = resources.getQuantityString(
            R.plurals.statistic_item_full_times,
            times
        )
        return getString(
            R.string.statistic_item_full_reading_times,
            times, timesString
        )
    }

    private fun Context.averageSession(times: Int, time: Long, pages: Int, chapters: Int): String {
        val pageString = resources.getQuantityString(
            R.plurals.statistic_item_full_pages,
            pages
        )
        return when {
            times == 0 -> getString(
                R.string.statistic_item_full_session_time_pages,
                TimeFormat(0).toString(this@averageSession),
                0,
                pageString
            )
            chapters / times == 0 -> getString(
                R.string.statistic_item_full_session_time_pages,
                TimeFormat(time / times).toString(this@averageSession),
                pages / times,
                pageString
            )
            else -> {
                val chapterString = resources.getQuantityString(
                    R.plurals.statistic_item_full_chapters,
                    chapters
                )
                getString(
                    R.string.statistic_item_full_session_time_pages_chapters,
                    TimeFormat(time / times).toString(this@averageSession),
                    pages / times,
                    pageString,
                    chapters / times,
                    chapterString
                )
            }
        }
    }
}
