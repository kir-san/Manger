package com.san.kir.core.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlin.math.abs
import kotlin.math.roundToInt

internal enum class TopBarLayoutId {
    Navigation,
    Actions,
    ExpandedTitle,
    CollapsedTitle,
    ExpandedSubtitle,
    CollapsedSubtitle,
    Additional;
}

private val HorizontalPadding = Dimensions.quarter
private val ExpandedHorizontalPadding = Dimensions.default
private val AdditionalOffsetForExpanded = ExpandedHorizontalPadding - HorizontalPadding
private val CollapsedTitleLineHeight = 29.sp
private val CollapsedSubtitleLineHeight = 18.sp
private val DefaultCollapsedElevation = 3.dp
private val ExpandedTitleBottomPadding = Dimensions.default
private val MinCollapsedHeight = 64.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopBarLayout(
    modifier: Modifier,
    title: AnnotatedString? = null,
    subtitle: AnnotatedString? = null,
    subtitleMaxLines: Int = Int.MAX_VALUE,
    expandedTitleStyle: TextStyle = MaterialTheme.typography.headlineLarge,
    expandedSubTitleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    additional: (@Composable () -> Unit)? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    collapsedElevation: Dp = DefaultCollapsedElevation,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = LocalContentColor.current,
) {
    val collapsedFraction = scrollBehavior?.state?.collapsedFraction?.coerceIn(0.0f, 1.0f) ?: 1.0f
    val fullyCollapsedTitleScale = if (title != null) {
        CollapsedTitleLineHeight.value / expandedTitleStyle.lineHeight.value
    } else 1.0f
    val fullyCollapsedSubtitleScale = if (subtitle != null) {
        CollapsedSubtitleLineHeight.value / expandedSubTitleStyle.lineHeight.value
    } else 1.0f
    val collapsingTitleScale = abs(lerp(1.0f, fullyCollapsedTitleScale, collapsedFraction))
    val collapsingSubtitleScale = abs(lerp(1.0f, fullyCollapsedSubtitleScale, collapsedFraction))

    val showElevation = title != null
            && scrollBehavior != null
            && scrollBehavior.state.contentOffset <= 0.0f
            && collapsedFraction == 1.0f

    val elevationState by animateDpAsState(
        if (showElevation) collapsedElevation else 0.dp,
        label = "toolbar_elevation"
    )

    val color by animateColorAsState(containerColor, label = "")
    var previousAdditionalHeight by remember { mutableIntStateOf(0) }

    Surface(
        color = color,
        shadowElevation = elevationState
    ) {
        Layout(
            content = {
                title?.let { title ->
                    Text(
                        title,
                        modifier = Modifier
                            .layoutId(TopBarLayoutId.ExpandedTitle)
                            .graphicsLayer {
                                scaleX = collapsingTitleScale
                                scaleY = collapsingTitleScale
                                transformOrigin = TransformOrigin(0f, 0f)
                            }
                            .padding(start = HorizontalPadding, end = ExpandedHorizontalPadding),
                        style = expandedTitleStyle
                    )
                    Text(
                        title,
                        modifier = Modifier
                            .layoutId(TopBarLayoutId.CollapsedTitle)
                            .graphicsLayer {
                                scaleX = collapsingTitleScale
                                scaleY = collapsingTitleScale
                                transformOrigin = TransformOrigin(0f, 0f)
                            }
                            .padding(horizontal = HorizontalPadding),
                        style = expandedTitleStyle,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )

                    subtitle?.let { subtitle ->
                        Text(
                            subtitle,
                            modifier = Modifier
                                .layoutId(TopBarLayoutId.ExpandedSubtitle)
                                .graphicsLayer {
                                    scaleX = collapsingSubtitleScale
                                    scaleY = collapsingSubtitleScale
                                    transformOrigin = TransformOrigin(0f, 0f)
                                }
                                .padding(start = HorizontalPadding, end = ExpandedHorizontalPadding),
                            color = contentColor.copy(alpha = 0.8f),
                            style = expandedSubTitleStyle,
                            maxLines = subtitleMaxLines
                        )

                        Text(
                            subtitle,
                            modifier = Modifier
                                .layoutId(TopBarLayoutId.CollapsedSubtitle)
                                .graphicsLayer {
                                    scaleX = collapsingSubtitleScale
                                    scaleY = collapsingSubtitleScale
                                    transformOrigin = TransformOrigin(0f, 0f)
                                }
                                .padding(horizontal = HorizontalPadding),
                            color = contentColor.copy(alpha = 0.7f),
                            overflow = TextOverflow.Ellipsis,
                            style = expandedSubTitleStyle,
                            maxLines = 1,
                        )
                    }
                }
                navigationIcon?.let { icon ->
                    Box(
                        Modifier
                            .layoutId(TopBarLayoutId.Navigation)
                            .padding(start = HorizontalPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        icon.invoke()
                    }
                }
                actions?.let { actions ->
                    Row(
                        modifier = Modifier
                            .layoutId(TopBarLayoutId.Actions)
                            .padding(end = HorizontalPadding),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        actions()
                    }
                }
                additional?.let { additional ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .layoutId(TopBarLayoutId.Additional)
                    ) {
                        additional()
                    }
                }
            },
            modifier = modifier
                .heightIn(MinCollapsedHeight)
                .windowInsetsPadding(windowInsets),
            measurePolicy = { measurables, constraints ->
                val expandedTitleBottomPaddingPx = ExpandedTitleBottomPadding.toPx()

                val navigationIconPlaceable = measurables
                    .firstOrNull { it.layoutId == TopBarLayoutId.Navigation }
                    ?.measure(constraints.copy(minWidth = 0, minHeight = 0))

                val actionsPlaceable = measurables
                    .firstOrNull { it.layoutId == TopBarLayoutId.Actions }
                    ?.measure(constraints.copy(minWidth = 0, minHeight = 0))

                val additionalPlaceable = measurables
                    .firstOrNull { it.layoutId == TopBarLayoutId.Additional }
                    ?.measure(constraints.copy(minHeight = 0))

                val expandedTitlePlaceable = measurables
                    .firstOrNull { it.layoutId == TopBarLayoutId.ExpandedTitle }
                    ?.measure(constraints.copy(minHeight = 0))

                val expandedSubtitlePlaceable = measurables
                    .firstOrNull { it.layoutId == TopBarLayoutId.ExpandedSubtitle }
                    ?.measure(constraints.copy(minHeight = 0))

                val navigationIconOffset = navigationIconPlaceable?.width ?: 0
                val actionsOffset = actionsPlaceable?.width ?: 0
                val freeMaxWidthPx = constraints.maxWidth - navigationIconOffset - actionsOffset
                val collapsedTitleMaxWidthPx = freeMaxWidthPx / fullyCollapsedTitleScale
                val collapsedSubtitleMaxWidthPx = freeMaxWidthPx / fullyCollapsedSubtitleScale

                val collapsedTitlePlaceable = measurables
                    .firstOrNull { it.layoutId == TopBarLayoutId.CollapsedTitle }
                    ?.measure(
                        constraints.copy(
                            maxWidth = collapsedTitleMaxWidthPx.roundToInt(),
                            minHeight = 0,
                            minWidth = 0
                        )
                    )

                val collapsedSubtitlePlaceable = measurables
                    .firstOrNull { it.layoutId == TopBarLayoutId.CollapsedSubtitle }
                    ?.measure(
                        constraints.copy(
                            maxWidth = collapsedSubtitleMaxWidthPx.roundToInt(),
                            minHeight = 0,
                            minWidth = 0
                        )
                    )

                val collapsedHeightPx = MinCollapsedHeight.toPx()
                val layoutHeightPx = collapsedHeightPx + (additionalPlaceable?.height ?: 0)
                val navigationIconY =
                    ((collapsedHeightPx - (navigationIconPlaceable?.height ?: 0)) / 2f).roundToInt()
                val actionsX = constraints.maxWidth - actionsOffset
                val actionsY =
                    ((collapsedHeightPx - (actionsPlaceable?.height ?: 0)) / 2f).roundToInt()

                var collapsingTitleX = 0
                var collapsingTitleY = 0
                var collapsingSubtitleX = 0
                var collapsingSubtitleY = 0
                var additionalY = collapsedHeightPx.roundToInt()
                val fullyExpandedTitleX: Float

                if (expandedTitlePlaceable == null || collapsedTitlePlaceable == null) {
                    scrollBehavior?.state?.heightOffsetLimit = -1.0f
                    fullyExpandedTitleX = layoutHeightPx
                } else {
                    val additionalHeight = additionalPlaceable?.height ?: 0
                    val heightOffsetLimitPx = expandedTitlePlaceable.height +
                            expandedTitleBottomPaddingPx +
                            additionalHeight +
                            (expandedSubtitlePlaceable?.height ?: 0)
                    scrollBehavior?.state?.heightOffsetLimit = -heightOffsetLimitPx

                    if (previousAdditionalHeight != additionalHeight) {
                        if (collapsedFraction == 1f) {
                            scrollBehavior?.let {
                                it.state.heightOffset += previousAdditionalHeight - additionalHeight
                            }
                        }
                        previousAdditionalHeight = additionalHeight
                    }

                    val fullyExpandedHeightPx = MinCollapsedHeight.toPx() + heightOffsetLimitPx
                    val fullyExpandedTitleX2 = AdditionalOffsetForExpanded.toPx()
                    val fullyExpandedTitleY = MinCollapsedHeight.toPx()
                    val fullyExpandedSubtitleX = AdditionalOffsetForExpanded.toPx()
                    val fullyExpandedSubtitleY = expandedTitlePlaceable.height + fullyExpandedTitleY
                    val collapsedSubtitleHeight = if (expandedSubtitlePlaceable != null) {
                        CollapsedSubtitleLineHeight.toPx().roundToInt()
                    } else {
                        0
                    }
                    val fullyCollapsedTitleX = navigationIconOffset.toFloat()
                    val fullyCollapsedTitleY = collapsedHeightPx / 2f -
                            CollapsedTitleLineHeight.toPx().roundToInt() / 2 -
                            collapsedSubtitleHeight / 2

                    val fullyCollapsedSubtitleX = navigationIconOffset.toFloat()
                    val fullyCollapsedSubtitleY =
                        CollapsedTitleLineHeight.toPx().roundToInt() + fullyCollapsedTitleY
                    val layoutHeightPx2 =
                        lerp(fullyExpandedHeightPx, layoutHeightPx, collapsedFraction)

                    collapsingTitleX = lerp(
                        fullyExpandedTitleX2, fullyCollapsedTitleX, collapsedFraction
                    ).roundToInt()

                    collapsingTitleY = lerp(
                        fullyExpandedTitleY, fullyCollapsedTitleY, collapsedFraction
                    ).roundToInt()

                    collapsingSubtitleX = lerp(
                        fullyExpandedSubtitleX, fullyCollapsedSubtitleX, collapsedFraction
                    ).roundToInt()

                    collapsingSubtitleY = lerp(
                        fullyExpandedSubtitleY, fullyCollapsedSubtitleY, collapsedFraction
                    ).roundToInt()

                    additionalY = lerp(
                        (expandedSubtitlePlaceable?.height ?: 0)
                                + fullyExpandedSubtitleY
                                + expandedTitleBottomPaddingPx,
                        MinCollapsedHeight.toPx(),
                        collapsedFraction
                    ).roundToInt()

                    fullyExpandedTitleX = layoutHeightPx2
                }

                layout(constraints.maxWidth, fullyExpandedTitleX.roundToInt()) {
                    navigationIconPlaceable?.placeRelative(0, navigationIconY)
                    actionsPlaceable?.placeRelative(actionsX, actionsY)

                    if (expandedTitlePlaceable?.width == collapsedTitlePlaceable?.width) {
                        expandedTitlePlaceable?.placeRelative(collapsingTitleX, collapsingTitleY)
                    } else {
                        expandedTitlePlaceable?.placeRelativeWithLayer(
                            collapsingTitleX, collapsingTitleY
                        ) { alpha = 1 - collapsedFraction }

                        collapsedTitlePlaceable?.placeRelativeWithLayer(
                            collapsingTitleX, collapsingTitleY
                        ) { alpha = collapsedFraction }
                    }


                    if (expandedSubtitlePlaceable?.width == collapsedSubtitlePlaceable?.width) {
                        expandedSubtitlePlaceable?.placeRelative(collapsingSubtitleX, collapsingSubtitleY)
                    } else {
                        expandedSubtitlePlaceable?.placeRelativeWithLayer(
                            collapsingSubtitleX, collapsingSubtitleY
                        ) { alpha = 1 - collapsedFraction }

                        collapsedSubtitlePlaceable?.placeRelativeWithLayer(
                            collapsingSubtitleX, collapsingSubtitleY
                        ) { alpha = collapsedFraction }
                    }

                    additionalPlaceable?.placeRelative(0, additionalY)
                }
            }
        )
    }
}
