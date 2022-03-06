package com.san.kir.manger.ui.application_navigation.statistic.main

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.TopBarScreenList
import com.san.kir.core.compose_utils.rememberImage
import com.san.kir.core.compose_utils.systemBarsHorizontalPadding
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.Statistic
import com.san.kir.manger.R
import com.san.kir.manger.utils.TimeFormat

@Composable
fun StatisticsScreen(
    navigateUp: () -> Unit,
    navigateToItem: (String) -> Unit,
    viewModel: StatisticViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val allTime by viewModel.allTime.collectAsState(0L)
    val allItems = viewModel.allItems.collectAsLazyPagingItems()

    TopBarScreenList(
        navigateUp = navigateUp,
        title = stringResource(R.string.main_menu_statistic),
        subtitle = stringResource(
            R.string.statistic_subtitle, TimeFormat(allTime).toString(context)
        ),
        additionalPadding = Dimensions.smaller
    ) {
        items(items = allItems, key = { stat -> stat.id }) { item ->
            item?.let {
                ItemView(item, viewModel) { navigateToItem(item.manga) }
            }
        }
    }
}

@Composable
private fun ItemView(
    item: Statistic,
    viewModel: StatisticViewModel,
    ctx: Context = LocalContext.current,
    onClick: () -> Unit,
) {
    val heightSize = 60.dp

    val manga by viewModel.manga(item).collectAsState(Manga())
    val allTime by viewModel.allTime.collectAsState(0L)
    val relativeTime by remember(allTime) {
        mutableStateOf(if (allTime != 0L) item.allTime.toFloat() / allTime.toFloat() else 0F)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dimensions.smaller, horizontal = Dimensions.default)
            .padding(systemBarsHorizontalPadding())
    ) {
        Image(
            rememberImage(manga.logo),
            modifier = Modifier
                .padding(end = 5.dp)
                .clip(CircleShape)
                .size(heightSize),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(item.manga, maxLines = 1)
            Text(
                stringResource(
                    R.string.statistic_item_time, TimeFormat(item.allTime).toString(ctx)
                )
            )
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(10.dp)
                    .fillMaxWidth(),
                progress = relativeTime,
            )
        }
    }
}
