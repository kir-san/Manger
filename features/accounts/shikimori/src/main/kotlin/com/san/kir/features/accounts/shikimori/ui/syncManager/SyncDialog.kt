package com.san.kir.features.accounts.shikimori.ui.syncManager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.san.kir.core.compose.BottomSheets
import com.san.kir.core.compose.DefaultSpacer
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.Fonts
import com.san.kir.core.compose.FullWeightSpacer
import com.san.kir.core.compose.HalfSpacer
import com.san.kir.core.compose.ThemedPreview
import com.san.kir.core.compose.ThemedPreviewContainer
import com.san.kir.core.compose.animation.animateToDelayed
import com.san.kir.core.compose.animation.rememberColorAnimatable
import com.san.kir.core.compose.animation.rememberFloatAnimatable
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.utils.append
import com.san.kir.core.utils.navigation.DialogState
import com.san.kir.core.utils.navigation.EmptyDialogData
import com.san.kir.core.utils.viewModel.Action
import com.san.kir.features.accounts.shikimori.R
import com.san.kir.features.accounts.shikimori.logic.models.AccountMangaItem
import com.san.kir.features.accounts.shikimori.logic.models.LibraryMangaItem
import com.san.kir.features.accounts.shikimori.logic.models.MangaItem
import com.san.kir.features.accounts.shikimori.ui.util.MangaLogo
import com.san.kir.features.accounts.shikimori.ui.util.MangaName

private const val DefaultDuration = 350


private val DefaultProgressTextColor: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurface

private val HighlightProgressTextColor: Color
    @Composable
    get() = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
    } else {
        MaterialTheme.colorScheme.error
    }

private val SortSelectedContainerColor: Color
    @Composable
    get() = MaterialTheme.colorScheme.primaryContainer

@Composable
internal fun SyncManagerDialog(state: DialogState<EmptyDialogData>, sendAction: (Action) -> Unit) {
    BottomSheets(state) { SyncContent(emptyList(), state::dismiss, sendAction) }
}

@Composable
private fun SyncContent(
    list: List<SyncItemState>,
    onDismiss: () -> Unit,
    sendAction: (Action) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(Dimensions.default),
        ) {
            Text(
                stringResource(R.string.manga_sync),
                modifier = Modifier.weight(1f),
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
            )

            IconButton(onDismiss) {
                Icon(Icons.Default.Close, "Close")
            }
        }

        LazyColumn {
            items(list, key = SyncItemState::id) { item ->
                HorizontalDivider()
                SyncItem(
                    item,
                    modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                    canHide = true,
                    canApply = false,
                    sendAction = sendAction,
                )
            }
        }
    }
}


@Composable
internal fun SyncItem(
    item: SyncItemState,
    modifier: Modifier = Modifier,
    canHide: Boolean = false,
    canApply: Boolean = true,
    sendAction: (Action) -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth().then(modifier)) {

        Column {
            SyncItemContent(item) { sendAction(SyncManagerAction.Update(it)) }

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                if (canHide) {
                    Button({ sendAction(SyncManagerAction.Hide(item.id)) }) {
                        Text("Скрыть")
                    }
                    DefaultSpacer()
                }

                if (canApply) {
                    Button({ sendAction(SyncManagerAction.ApplySync(item.id)) }) {
                        Text("Применить")
                    }
                    DefaultSpacer()
                }
            }

            DefaultSpacer()
        }
    }
}

@Composable
private fun SyncItemContent(item: SyncItemState, onChange: (SyncItemState) -> Unit = {}) {
    Column {

        var from by remember { mutableStateOf(item.from) }
        val bottomRotate = rememberFloatAnimatable(
            if (item.top is LibraryMangaItem || from != SyncItemState.From.Library) 180f else 0f
        )
        var bottomRead by remember { mutableIntStateOf(item.bottom.first().read) }
        val isTopMain by remember {
            derivedStateOf {
                when (item.top) {
                    is AccountMangaItem -> from == SyncItemState.From.Account
                    is LibraryMangaItem -> from == SyncItemState.From.Library
                    else -> false
                }
            }
        }

        LaunchedEffect(Unit) {
            snapshotFlow { from }.collect {
                bottomRotate.animateToDelayed(bottomRotate.value + 180f, duration = DefaultDuration)
            }
        }

        DefaultSpacer()

        TopElement(item.top, isTopMain, bottomRead)

        HalfSpacer()

        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            HalfSpacer()

            Text(
                stringResource(R.string.where_apply_it),
                modifier = Modifier.align(Alignment.CenterVertically),
                style = MaterialTheme.typography.bodyMedium,
            )

            FullWeightSpacer()

            HelpText(item.top is AccountMangaItem, highlight = isTopMain)

            Icon(
                Icons.Default.ArrowUpward, "Sync direction",
                modifier = Modifier
                    .size(Dimensions.Image.small + Dimensions.half)
                    .graphicsLayer { rotationZ = bottomRotate.value }
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        from = when (from) {
                            SyncItemState.From.Library -> SyncItemState.From.Account
                            SyncItemState.From.Account -> SyncItemState.From.Library
                        }
                        onChange(item.copy(from = from))
                    }
                    .padding(Dimensions.half),
            )

            HelpText(
                item.top is LibraryMangaItem,
                modifier = Modifier.align(Alignment.Bottom),
                highlight = isTopMain,
            )

            FullWeightSpacer()
        }

        HalfSpacer()

        var currentButtonIndex by remember { mutableIntStateOf(item.selectedIndex) }
        val buttonOffset = rememberFloatAnimatable(currentButtonIndex.toFloat())
        LaunchedEffect(currentButtonIndex) {
            buttonOffset.animateToDelayed(currentButtonIndex.toFloat(), duration = DefaultDuration)
        }

        val selectedContainerColor = SortSelectedContainerColor
        Column(
            modifier = Modifier
                .padding(horizontal = Dimensions.half)
                .fillMaxWidth()
                .clip(RoundedCornerShape(1.dp))
                .selectableGroup()
                .width(IntrinsicSize.Max)
                .drawBehind {
                    if (currentButtonIndex >= 0) {
                        drawRoundRect(
                            color = selectedContainerColor,
                            topLeft = Offset(
                                x = 0f,
                                y = buttonOffset.value * (size.height / item.bottom.size),
                            ),
                            size = size.copy(height = size.height / item.bottom.size),
                            cornerRadius = CornerRadius(x = size.height / item.bottom.size / 2)
                        )
                    }
                }
        ) {
            item.bottom.forEachIndexed { index, manga ->
                BottomElement(
                    item = manga,
                    topRead = item.top.read,
                    isTopMain = isTopMain,
                    isSelected = currentButtonIndex == index,
                    elementCount = item.bottom.size
                ) {
                    currentButtonIndex = index
                    bottomRead = manga.read
                    onChange(item.copy(selectedId = manga.id))
                }
            }
        }

        HalfSpacer()
    }
}

@Composable
private fun TopElement(
    item: MangaItem,
    isTopMain: Boolean,
    bottomRead: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalInsetsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DefaultSpacer()

        MangaLogo(item.logo)

        DefaultSpacer()

        Column {
            MangaName(item.name, modifier = Modifier.padding(end = Dimensions.default))
            ProgressText(
                read = if (isTopMain.not()) item.read else bottomRead,
                all = item.all,
                highlightProgress = isTopMain.not()
            )
        }

        DefaultSpacer()
    }
}

@Composable
private fun BottomElement(
    item: MangaItem,
    topRead: Int,
    isTopMain: Boolean,
    isSelected: Boolean,
    elementCount: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .selectable(isSelected, enabled = elementCount > 1, role = Role.RadioButton, onClick)
            .horizontalInsetsPadding(vertical = Dimensions.half)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DefaultSpacer()

        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            MangaName(item.name)
            ProgressText(
                read = if ((isTopMain || !isSelected)) item.read else topRead,
                all = item.all,
                highlightProgress = !isTopMain && isSelected,
            )
        }

        DefaultSpacer()

        MangaLogo(item.logo)

        DefaultSpacer()
    }
}

@Composable
private fun ProgressText(
    read: Int,
    all: Int,
    highlightProgress: Boolean,
    modifier: Modifier = Modifier,
) {
    val defaultColor = DefaultProgressTextColor
    val highlightColor = HighlightProgressTextColor
    val highlightColorAnimator = rememberColorAnimatable(
        if (highlightProgress) highlightColor else defaultColor
    )
    LaunchedEffect(highlightProgress) {
        highlightColorAnimator.animateToDelayed(
            if (highlightProgress) highlightColor else defaultColor,
            duration = DefaultDuration
        )
    }

    Text(
        buildAnnotatedString {
            append(
                stringResource(R.string.reading, read, all),
                SpanStyle(
                    color = highlightColorAnimator.value,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        },
        fontSize = Fonts.Size.less,
        modifier = modifier,
    )
}

@Composable
private fun HelpText(
    isAccount: Boolean,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    val defaultColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val highlightColor = MaterialTheme.colorScheme.primary
    val highlightColorAnimator = rememberColorAnimatable(
        if (highlight) highlightColor else defaultColor
    )
    LaunchedEffect(highlight) {
        highlightColorAnimator.animateToDelayed(
            if (highlight) highlightColor else defaultColor,
            duration = DefaultDuration
        )
    }

    Text(
        stringResource(if (isAccount) R.string.from_account else R.string.from_library),
        modifier,
        color = highlightColorAnimator.value,
        style = MaterialTheme.typography.bodySmall,
    )
}

@ThemedPreview
@Composable
private fun PreviewSyncItem() {
    ThemedPreviewContainer {
        SyncContent(
            list = listOf(
                SyncItemState(
                    top = LibraryMangaItem(
                        1L,
                        "Very Very Very Very Very Long Library Name",
                        read = 5,
                        all = 15,
                    ),
                    bottom = listOf(
                        AccountMangaItem(2L, name = "Account Name", read = 9, all = 20)
                    )
                ),
                SyncItemState(
                    top = AccountMangaItem(id = 1L, name = "Account Name", read = 5, all = 15),
                    bottom = listOf(
                        LibraryMangaItem(
                            id = 2L,
                            name = "Very Very Very Very Very Long Library Name 1",
                            read = 8,
                            all = 20
                        ),
                        LibraryMangaItem(id = 3L, name = "Library Name 2", read = 9, all = 20)
                    )
                ),
            ),
            onDismiss = {},
            sendAction = {}
        )
    }
}
