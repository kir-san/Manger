package com.san.kir.statistic.ui.statistics

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose.CircleLogo
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.RemoveItemMenuOnHold
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.topBar
import com.san.kir.core.utils.TimeFormat
import com.san.kir.data.models.extend.SimplifiedStatistic
import com.san.kir.statistic.R

@Composable
fun StatisticsScreen(
    navigateUp: () -> Boolean,
    navigateToItem: (Long) -> Unit,
) {
    val viewModel: StatisticsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val ctx = LocalContext.current

    ScreenList(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = stringResource(R.string.main_menu_statistic),
            subtitle = stringResource(
                R.string.statistic_subtitle, TimeFormat(state.allTime).toString(ctx)
            ),
        ),
        additionalPadding = Dimensions.quarter
    ) {
        items(items = state.items, key = { stat -> stat.id }) { item ->
            ItemView(item, state.allTime, viewModel::sendEvent) { navigateToItem(item.id) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.ItemView(
    item: SimplifiedStatistic,
    allTime: Long,
    sendEvent: (StatisticsEvent) -> Unit,
    onClick: () -> Unit,
) {
    RemoveItemMenuOnHold(
        removeText = stringResource(R.string.statistic_delete),
        cancelText = stringResource(R.string.statistic_cancel),
        onSuccess = { sendEvent(StatisticsEvent.Delete(item.id)) },
        modifier = Modifier
            .fillMaxWidth()
            .animateItemPlacement(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onClick(onClick = onClick)
                .padding(vertical = Dimensions.quarter, horizontal = Dimensions.default)
        ) {

            CircleLogo(logoUrl = item.logo)

            Column(modifier = Modifier.weight(1f)) {
                Title(name = item.name)
                ReadStatus(time = item.allTime)
                Progress(allTime = allTime, itemTime = item.allTime)
            }
        }
    }
}

@Composable
private fun Title(name: String) {
    Text(name, maxLines = 1)
}

@Composable
private fun ReadStatus(time: Long) {
    val ctx = LocalContext.current
    Text(stringResource(R.string.statistic_item_time, TimeFormat(time).toString(ctx)))
}

@Composable
fun Progress(allTime: Long, itemTime: Long) {
    LinearProgressIndicator(
        modifier = Modifier
            .padding(top = Dimensions.quarter)
            .height(Dimensions.half)
            .fillMaxWidth(),
        progress = if (allTime != 0L) itemTime.toFloat() / allTime.toFloat() else 0F,
    )
}
