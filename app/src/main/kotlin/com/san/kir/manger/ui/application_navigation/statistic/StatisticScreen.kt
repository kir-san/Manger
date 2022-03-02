package com.san.kir.manger.ui.application_navigation.statistic

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.san.kir.core.compose_utils.DialogText
import com.san.kir.core.compose_utils.LabelText
import com.san.kir.core.compose_utils.SmallSpacer
import com.san.kir.core.compose_utils.TopBarScreenContent
import com.san.kir.core.utils.bytesToMb
import com.san.kir.core.utils.formatDouble
import com.san.kir.data.models.base.Statistic
import com.san.kir.manger.R
import com.san.kir.manger.utils.TimeFormat

@Composable
fun StatisticScreen(nav: NavHostController, item: Statistic) {

    TopBarScreenContent(
        navigateUp = nav::navigateUp,
        title = item.manga,
    ) {
        LabelText(R.string.statistic_item_full_last_session)
        DialogText(timePagesData(item.lastTime, item.lastPages, item.lastChapters))
        DialogText(speedReading(item))

        SmallSpacer()

        LabelText(R.string.statistic_item_full_all)
        DialogText(timePagesData(item.allTime, item.allPages, item.allChapters))
        DialogText(averageSpeedReading(item))
        DialogText(maxSpeedReading(item))
        DialogText(downloadData(item))
        DialogText(readTimes(item))
        DialogText(averageSession(item))
    }
}

@Composable
private fun timePagesData(
    time: Long, pages: Int, chapters: Int,
    context: Context = LocalContext.current,
): String {
    val timeString = TimeFormat(time).toString(context)
    return if (pages == 0) {
        context.getString(R.string.statistic_item_full_for_time_nothing, timeString)
    } else {
        val pageString = context.resources.getQuantityString(
            R.plurals.statistic_item_full_pages,
            pages
        )
        if (chapters == 0) {
            context.getString(
                R.string.statistic_item_full_for_time_with_pages,
                timeString,
                pages, pageString
            )
        } else {
            val chapterString = context.resources.getQuantityString(
                R.plurals.statistic_item_full_chapters,
                chapters
            )
            context.getString(
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

@Composable
private fun speedReading(item: Statistic, context: Context = LocalContext.current): String {
    val speed = (item.lastPages / (item.lastTime.toFloat() / 60)).toInt()
    return if (speed == 0) {
        context.getString(R.string.statistic_item_full_speed_nothing)
    } else {
        val pageString = context.resources.getQuantityString(
            R.plurals.statistic_item_full_pages,
            speed
        )
        context.getString(R.string.statistic_item_full_speed_reading, speed, pageString)
    }
}

@Composable
private fun averageSpeedReading(
    item: Statistic,
    context: Context = LocalContext.current,
): String {
    val speed = (item.allPages / (item.allTime.toFloat() / 60)).toInt()
    return if (speed == 0) {
        context.getString(R.string.statistic_item_full_average_speed_nothing)
    } else {
        val pageString = context.resources.getQuantityString(
            R.plurals.statistic_item_full_pages,
            speed
        )
        context.getString(R.string.statistic_item_full_average_speed_reading, speed, pageString)
    }
}

@Composable
private fun maxSpeedReading(item: Statistic, context: Context = LocalContext.current): String {
    val pageString = context.resources.getQuantityString(
        R.plurals.statistic_item_full_pages,
        item.maxSpeed
    )
    return context.getString(R.string.statistic_item_full_max_speed, item.maxSpeed, pageString)
}

@Composable
private fun downloadData(item: Statistic, context: Context = LocalContext.current): String {
    return context.getString(
        R.string.statistic_item_full_download_data,
        formatDouble(bytesToMb(item.downloadSize)),
        TimeFormat(item.downloadTime / 1000).toString(context)
    )
}

@Composable
private fun readTimes(item: Statistic, context: Context = LocalContext.current): String {
    val timesString = context.resources.getQuantityString(
        R.plurals.statistic_item_full_times,
        item.openedTimes
    )
    return context.getString(
        R.string.statistic_item_full_reading_times,
        item.openedTimes, timesString
    )
}

@Composable
private fun averageSession(item: Statistic, context: Context = LocalContext.current): String {
    val pageString = context.resources.getQuantityString(
        R.plurals.statistic_item_full_pages,
        item.allPages
    )
    return when {
        item.openedTimes == 0 -> context.getString(
            R.string.statistic_item_full_session_time_pages,
            TimeFormat(0).toString(context),
            0,
            pageString
        )
        item.allChapters / item.openedTimes == 0 -> context.getString(
            R.string.statistic_item_full_session_time_pages,
            TimeFormat(item.allTime / item.openedTimes).toString(context),
            item.allPages / item.openedTimes,
            pageString
        )
        else -> {
            val chapterString = context.resources.getQuantityString(
                R.plurals.statistic_item_full_chapters,
                item.allChapters
            )
            context.getString(
                R.string.statistic_item_full_session_time_pages_chapters,
                TimeFormat(item.allTime / item.openedTimes).toString(context),
                item.allPages / item.openedTimes,
                pageString,
                item.allChapters / item.openedTimes,
                chapterString
            )
        }
    }
}
