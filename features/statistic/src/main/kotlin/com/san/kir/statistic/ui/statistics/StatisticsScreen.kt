package com.san.kir.statistic.ui.statistics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.OutlinedButton
import com.san.kir.core.compose_utils.ScreenList
import com.san.kir.core.compose_utils.animation.BottomAnimatedVisibility
import com.san.kir.core.compose_utils.holdPress
import com.san.kir.core.compose_utils.rememberImage
import com.san.kir.core.compose_utils.systemBarsHorizontalPadding
import com.san.kir.core.compose_utils.topBar
import com.san.kir.core.utils.TimeFormat
import com.san.kir.data.models.extend.SimplifiedStatistic
import com.san.kir.statistic.R
import kotlinx.coroutines.launch

@Composable
fun StatisticsScreen(
    navigateUp: () -> Unit,
    navigateToItem: (Long) -> Unit,
) {
    val viewModel: StatisticsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val ctx = LocalContext.current

    ScreenList(
        topBar = topBar(
            navigationListener = navigateUp,
            title = stringResource(R.string.main_menu_statistic),
            subtitle = stringResource(
                R.string.statistic_subtitle, TimeFormat(state.allTime).toString(ctx)
            ),
        ),
        additionalPadding = Dimensions.smaller
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
    var deleteRequest by remember { mutableStateOf(false) }
    val deleteAnimation = remember { Animatable(0.dp, Dp.VectorConverter) }
    var max by remember { mutableStateOf(0.dp) }
    var height by remember { mutableStateOf(0) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .animateItemPlacement()
    ) {
        max = maxWidth

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(systemBarsHorizontalPadding())
                .onSizeChanged { height = it.height }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = onClick, onLongClick = { deleteRequest = true })
                    .padding(vertical = Dimensions.smaller, horizontal = Dimensions.default)
            ) {

                Logo(logoUrl = item.logo)

                Column(modifier = Modifier.weight(1f)) {
                    Title(name = item.name)
                    ReadStatus(time = item.allTime)
                    Progress(allTime = allTime, itemTime = item.allTime)
                }
            }

            BottomAnimatedVisibility(visible = deleteRequest) {
                val scope = rememberCoroutineScope()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        text = stringResource(R.string.statistic_delete),
                        modifier = Modifier
                            .padding(Dimensions.small)
                            .weight(1f)
                            .holdPress(
                                onDown = {
                                    scope.launch {
                                        if (deleteAnimation
                                                .animateTo(max, TweenSpec(2000, 0, LinearEasing))
                                                .endReason == AnimationEndReason.Finished) {
                                            sendEvent(StatisticsEvent.Delete(item.id))
                                        }
                                    }
                                },
                                onUp = {
                                    scope.launch {
                                        if (deleteAnimation.isRunning) {
                                            deleteAnimation.stop()
                                            deleteAnimation.animateTo(0.dp, TweenSpec(500))
                                        }
                                    }
                                }
                            ),
                        borderColor = Color(0x88ff0000),
                    )

                    OutlinedButton(
                        onClick = { deleteRequest = false },
                        modifier = Modifier
                            .padding(Dimensions.small)
                            .weight(1f)
                    ) {
                        Text(stringResource(R.string.statistic_cancel))
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .then(with(LocalDensity.current) { Modifier.height(height.toDp()) })
                .background(Color(0x6DFF0000))
                .width(deleteAnimation.value)
        )
    }
}

@Composable
private fun Logo(logoUrl: String) {
    Image(
        rememberImage(logoUrl),
        modifier = Modifier
            .padding(end = Dimensions.smaller)
            .clip(CircleShape)
            .size(Dimensions.Image.bigger),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
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
            .padding(top = Dimensions.smaller)
            .height(Dimensions.small)
            .fillMaxWidth(),
        progress = if (allTime != 0L) itemTime.toFloat() / allTime.toFloat() else 0F,
    )
}
