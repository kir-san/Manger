package com.san.kir.statistic.ui.statistics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.san.kir.core.compose.CircleLogo
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.SwipeToDelete
import com.san.kir.core.compose.SwipeToDeleteDefaults
import com.san.kir.core.compose.animation.SharedParams
import com.san.kir.core.compose.animation.animateToDelayed
import com.san.kir.core.compose.animation.rememberIntAnimatable
import com.san.kir.core.compose.animation.rememberLongAnimatable
import com.san.kir.core.compose.animation.rememberSharedParams
import com.san.kir.core.compose.animation.saveParams
import com.san.kir.core.compose.createDismissStates
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.compose.topBar
import com.san.kir.core.utils.flow.collectAsStateWithLifecycle
import com.san.kir.core.utils.formatTime
import com.san.kir.core.utils.viewModel.Action
import com.san.kir.core.utils.viewModel.OnEvent
import com.san.kir.core.utils.viewModel.ReturnEvents
import com.san.kir.core.utils.viewModel.rememberSendAction
import com.san.kir.core.utils.viewModel.stateHolder
import com.san.kir.data.models.main.SimplifiedStatistic
import com.san.kir.statistic.R
import com.san.kir.statistic.utils.appendAndHighlightDigits


private const val DurationDismissConfirmation = 2000
private val ImageSize = Dimensions.Image.bigger
private val HorizontalItemPadding = Dimensions.default
private val VerticalItemPadding = Dimensions.half
private val HorizontalItemContentPadding = Dimensions.half
private val DismissEndPadding = ImageSize + HorizontalItemPadding + HorizontalItemContentPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StatisticsScreen(
    navigateUp: () -> Unit,
    navigateToItem: (Long, SharedParams) -> Unit,
) {
    val holder: StatisticsStateHolder = stateHolder { StatisticsViewModel() }
    val state by holder.state.collectAsStateWithLifecycle()
    val sendAction = holder.rememberSendAction()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val dismissStates = remember(state.items.size) {
        createDismissStates(state.items.size, density, scope)
    }

    holder.OnEvent { event ->
        when (event) {
            is StatisticsEvent.ToStatistic -> navigateToItem(event.id, event.params)
        }
    }

    ScreenList(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = AnnotatedString(stringResource(R.string.main_menu_statistic)),
            subtitle = buildSubtitle(
                allReadingItems = state.allReadingItems,
                allTime = state.allTime
            ),
            subtitleMaxLines = 1,
        ),
        additionalPadding = Dimensions.quarter
    ) {
        if (state.items.isEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.empty_stats))
                }
            }
        } else {
            itemsIndexed(items = state.items, key = { _, stat -> stat.id }) { index, item ->
                dismissStates.getOrNull(index)?.let { dismissState ->
                    ItemView(item, state.allTime, dismissState, sendAction)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun LazyItemScope.ItemView(
    item: SimplifiedStatistic,
    allTime: Long,
    dismissState: SwipeToDismissBoxState,
    sendAction: (Action) -> Unit,
) {
    val params = rememberSharedParams()
    val timeCounter = rememberLongAnimatable()
    LaunchedEffect(item.allTime) { timeCounter.animateToDelayed(item.allTime) }

    SwipeToDelete(
        state = dismissState,
        resetText = R.string.reset,
        agreeText = R.string.yes,
        durationDismissConfirmation = DurationDismissConfirmation,
        onSuccessDismiss = { sendAction(StatisticsAction.Delete(item.id)) },
        dismissEndPadding = DismissEndPadding,
        endButtonPadding = ImageSize,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalInsetsPadding(HorizontalItemPadding, VerticalItemPadding)
            .animateItem(fadeInSpec = null, fadeOutSpec = null)
            .saveParams(params)
    ) {
        Row(
            modifier = Modifier
                .clip(SwipeToDeleteDefaults.MainItemShape)
                .clickable {
                    sendAction(ReturnEvents(StatisticsEvent.ToStatistic(item.id, params)))
                },
            verticalAlignment = Alignment.CenterVertically
        ) {

            CircleLogo(logoUrl = item.logo, size = ImageSize)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = HorizontalItemContentPadding)
            ) {
                Title(name = item.name)
                ReadStatus(time = item.allTime)
                Progress(allTime = allTime, itemTime = timeCounter)
            }
        }
    }
}

@Composable
private fun Title(name: String) {
    Text(name, maxLines = 1, fontWeight = FontWeight.Bold, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun ReadStatus(time: Long) {
    Text(
        buildAnnotatedString {
            appendAndHighlightDigits(stringResource(R.string.time_reading) + " " + time.formatTime())
        },
        modifier = Modifier.padding(vertical = Dimensions.smallest),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun Progress(allTime: Long, itemTime: Animatable<Long, AnimationVector1D>) {
    LinearProgressIndicator(
        modifier = Modifier
            .padding(top = Dimensions.quarter, end = Dimensions.half)
            .height(Dimensions.half)
            .fillMaxWidth(),
        progress = { if (allTime != 0L) itemTime.value / allTime.toFloat() else 0F },
        drawStopIndicator = {},
    )
}

@Composable
private fun buildSubtitle(allReadingItems: Int, allTime: Long): AnnotatedString {
    val allReadingCounter = rememberIntAnimatable(0)
    val allTimeCounter = rememberLongAnimatable(0L)
    LaunchedEffect(allReadingItems) { allReadingCounter.animateToDelayed(allReadingItems) }
    LaunchedEffect(allTime) { allTimeCounter.animateToDelayed(allTime) }

    return buildAnnotatedString {
        val pluralManga = pluralStringResource(R.plurals.manga, allReadingCounter.value)
        val forStr: String = stringResource(R.string.for_f)

        appendAndHighlightDigits("${allReadingCounter.value} $pluralManga $forStr ${allTimeCounter.value.formatTime()}")
    }
}
