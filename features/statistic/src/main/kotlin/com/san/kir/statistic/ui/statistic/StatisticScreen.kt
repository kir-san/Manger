package com.san.kir.statistic.ui.statistic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose_utils.DefaultSpacer
import com.san.kir.core.compose_utils.DialogText
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.LabelText
import com.san.kir.core.compose_utils.ScreenContent
import com.san.kir.core.compose_utils.horizontalInsetsPadding
import com.san.kir.core.compose_utils.topBar
import com.san.kir.core.utils.TimeFormat
import com.san.kir.core.utils.bytesToMb
import com.san.kir.core.utils.formatDouble
import com.san.kir.data.models.base.Statistic
import com.san.kir.statistic.R

@Composable
fun StatisticScreen(navigateUp: () -> Unit, itemId: Long) {
    val viewModel: StatisticViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.sendEvent(StatisticEvent.Set(itemId)) }

    ScreenContent(
        topBar = topBar(
            navigationListener = navigateUp,
            title = state.mangaName,
        ),
    ) {
        Column(modifier = Modifier.horizontalInsetsPadding()) {
            LabelText(R.string.statistic_item_full_last_session)
            DialogText(
                timePagesData(
                    state.item.lastTime,
                    state.item.lastPages,
                    state.item.lastChapters
                ),
                modifier = Modifier.padding(top = Dimensions.smallest)
            )
            DialogText(
                speedReading(state.item.lastPages, state.item.lastTime),
                modifier = Modifier.padding(top = Dimensions.smallest)
            )

            DefaultSpacer()

            LabelText(R.string.statistic_item_full_all)
            DialogText(
                timePagesData(state.item.allTime, state.item.allPages, state.item.allChapters),
                modifier = Modifier.padding(top = Dimensions.smallest)
            )
            DialogText(
                averageSpeedReading(state.item.allPages, state.item.allTime),
                modifier = Modifier.padding(top = Dimensions.smallest)
            )
            DialogText(
                maxSpeedReading(state.item.maxSpeed),
                modifier = Modifier.padding(top = Dimensions.smallest)
            )
            DialogText(
                downloadData(state.item.downloadSize, state.item.downloadTime),
                modifier = Modifier.padding(top = Dimensions.smallest)
            )
            DialogText(
                readTimes(state.item.openedTimes),
                modifier = Modifier.padding(top = Dimensions.smallest)
            )
            DialogText(
                averageSession(state.item),
                modifier = Modifier.padding(top = Dimensions.smallest)
            )
        }
    }
}

@Composable
private fun timePagesData(time: Long, pages: Int, chapters: Int): String {
    val context = LocalContext.current
    val timeString = TimeFormat(time).toString(context)
    return if (pages == 0) {
        stringResource(R.string.statistic_item_full_for_time_nothing, timeString)
    } else {
        val pageString = context.resources.getQuantityString(
            R.plurals.statistic_item_full_pages, pages
        )
        if (chapters == 0) {
            stringResource(
                R.string.statistic_item_full_for_time_with_pages, timeString, pages, pageString
            )
        } else {
            val chapterString = context.resources.getQuantityString(
                R.plurals.statistic_item_full_chapters, chapters
            )
            stringResource(
                R.string.statistic_item_full_for_time_with_pages_chapters,
                timeString, pages, pageString, chapters, chapterString
            )
        }
    }
}

@Composable
private fun speedReading(lastPages: Int, lastTime: Long): String {
    val speed = (lastPages / (lastTime.toFloat() / 60)).toInt()
    return if (speed == 0) {
        stringResource(R.string.statistic_item_full_speed_nothing)
    } else {
        val pageString = LocalContext.current.resources.getQuantityString(
            R.plurals.statistic_item_full_pages, speed
        )
        stringResource(R.string.statistic_item_full_speed_reading, speed, pageString)
    }
}

@Composable
private fun averageSpeedReading(allPages: Int, allTime: Long): String {
    val speed = (allPages / (allTime.toFloat() / 60)).toInt()
    return if (speed == 0) {
        stringResource(R.string.statistic_item_full_average_speed_nothing)
    } else {
        val pageString = LocalContext.current.resources.getQuantityString(
            R.plurals.statistic_item_full_pages, speed
        )
        stringResource(R.string.statistic_item_full_average_speed_reading, speed, pageString)
    }
}

@Composable
private fun maxSpeedReading(maxSpeed: Int): String {
    val pageString = LocalContext.current.resources.getQuantityString(
        R.plurals.statistic_item_full_pages, maxSpeed
    )
    return stringResource(R.string.statistic_item_full_max_speed, maxSpeed, pageString)
}

@Composable
private fun downloadData(downloadSize: Long, downloadTime: Long): String {
    return stringResource(
        R.string.statistic_item_full_download_data,
        formatDouble(bytesToMb(downloadSize)),
        TimeFormat(downloadTime / 1000).toString(LocalContext.current)
    )
}

@Composable
private fun readTimes(openedTimes: Int): String {
    val timesString = LocalContext.current.resources.getQuantityString(
        R.plurals.statistic_item_full_times, openedTimes
    )
    return stringResource(R.string.statistic_item_full_reading_times, openedTimes, timesString)
}

@Composable
private fun averageSession(item: Statistic): String {
    val context = LocalContext.current
    val pageString = context.resources.getQuantityString(
        R.plurals.statistic_item_full_pages, item.allPages
    )
    return when {
        item.openedTimes == 0 -> stringResource(
            R.string.statistic_item_full_session_time_pages,
            TimeFormat(0).toString(context), 0, pageString
        )

        item.allChapters / item.openedTimes == 0 -> stringResource(
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
            stringResource(
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
