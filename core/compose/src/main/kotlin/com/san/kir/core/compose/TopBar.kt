package com.san.kir.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import com.san.kir.core.compose.animation.BottomAnimatedVisibility
import com.san.kir.core.compose.animation.FromStartToStartAnimContent
import com.san.kir.core.compose.animation.TopAnimatedVisibility
import com.san.kir.core.compose.animation.TopEndAnimatedVisibility
import com.san.kir.core.utils.TestTags
import com.san.kir.core.utils.navigation.DialogState
import com.san.kir.core.utils.navigation.EmptyDialogData
import com.san.kir.core.utils.navigation.show
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun topBar(
    title: String? = null,
    subtitle: String? = null,
    actions: @Composable TopBarActions.() -> Unit = {},
    additionalContent: @Composable (ColumnScope.() -> Unit)? = null,
    onSearchTextChange: ((String) -> Unit)? = null,
    navigationButton: NavigationButton,
    initSearchText: String = "",
    hasAction: Boolean = false,
    progressAction: Float? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    titleTextStyle: TextStyle = MaterialTheme.typography.headlineLarge,
): @Composable (TopAppBarScrollBehavior?, MenuDialogState) -> Unit = { behavior, dialogState ->
    val topBarActions = remember { TopBarActions(dialogState) }

    TopBarLayout(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .startInsetsPadding(),
        title = title?.let(::AnnotatedString),
        subtitle = subtitle?.let(::AnnotatedString),
        expandedTitleStyle = titleTextStyle,
        navigationIcon = { NavigationIcon(navigationButton) },
        actions = {
            Row(modifier = Modifier.endInsetsPadding()) {
                topBarActions.actions()
            }
        },
        additional = {
            Column(Modifier.fillMaxWidth()) {
                additionalContent?.invoke(this)
                TopAnimatedVisibility(
                    visible = onSearchTextChange != null,
                    modifier = Modifier.padding(vertical = Dimensions.quarter)
                ) {
                    SearchTextField(initialValue = initSearchText) {
                        onSearchTextChange?.invoke(it)
                    }
                }

                BottomAnimatedVisibility(
                    visible = hasAction,
                    modifier = Modifier.padding(vertical = Dimensions.smallest)
                ) {
                    if (progressAction == null) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            strokeCap = StrokeCap.Round,
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = { progressAction },
                            modifier = Modifier.fillMaxWidth(),
                            strokeCap = StrokeCap.Round,
                            drawStopIndicator = {},
                        )
                    }
                }
            }
        },
        scrollBehavior = behavior,
        containerColor = containerColor,
        contentColor = contentColor,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun topBar(
    title: AnnotatedString? = null,
    subtitle: AnnotatedString? = null,
    subtitleMaxLines: Int = Int.MAX_VALUE,
    actions: @Composable TopBarActions.() -> Unit = {},
    additionalContent: @Composable (ColumnScope.() -> Unit)? = null,
    onSearchTextChange: ((String) -> Unit)? = null,
    navigationButton: NavigationButton,
    initSearchText: String = "",
    hasAction: Boolean = false,
    progressAction: Float? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    titleTextStyle: TextStyle = MaterialTheme.typography.headlineLarge,
): @Composable (TopAppBarScrollBehavior?, MenuDialogState) -> Unit = { behavior, dialogState ->
    val topBarActions = remember { TopBarActions(dialogState) }

    TopBarLayout(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .startInsetsPadding(),
        title = title,
        subtitle = subtitle,
        subtitleMaxLines = subtitleMaxLines,
        expandedTitleStyle = titleTextStyle,
        navigationIcon = { NavigationIcon(navigationButton) },
        actions = {
            Row(modifier = Modifier.endInsetsPadding()) {
                topBarActions.actions()
            }
        },
        additional = {
            Column(Modifier.fillMaxWidth()) {
                additionalContent?.invoke(this)
                TopAnimatedVisibility(
                    visible = onSearchTextChange != null,
                    modifier = Modifier.padding(vertical = Dimensions.quarter)
                ) {
                    SearchTextField(initialValue = initSearchText) {
                        onSearchTextChange?.invoke(it)
                    }
                }

                BottomAnimatedVisibility(
                    visible = hasAction,
                    modifier = Modifier.padding(vertical = Dimensions.smallest)
                ) {
                    if (progressAction == null) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            strokeCap = StrokeCap.Round
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = { progressAction },
                            modifier = Modifier.fillMaxWidth(),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        },
        scrollBehavior = behavior,
        containerColor = containerColor,
    )
}

@Composable
private fun NavigationIcon(state: NavigationButton) {
    val coroutineScope = rememberCoroutineScope()

    FromStartToStartAnimContent(targetState = state) {
        when (it) {
            is NavigationButton.Back ->
                IconButton(
                    modifier = Modifier.testTag(TestTags.Drawer.nav_back),
                    onClick = { it.onClick() }
                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") }

            is NavigationButton.Close ->
                IconButton(onClick = it.onClick) {
                    Icon(Icons.Default.Close, "")
                }

            is NavigationButton.Scaffold ->
                Box {
                    IconButton(
                        modifier = Modifier.testTag(TestTags.Drawer.drawer_open),
                        onClick = { coroutineScope.launch { it.state.open() } }
                    ) { Icon(Icons.Default.Menu, "") }
                    if (it.hasNotify) {
                        NotifyIcon()
                    }
                }
        }
    }
}

@Composable
private fun BoxScope.NotifyIcon() {
    Box(
        Modifier
            .align(Alignment.TopEnd)
            .padding(top = Dimensions.half, end = Dimensions.half)
            .size(Dimensions.half)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
    )
}


internal typealias MenuDialogState = DialogState<EmptyDialogData>

public class TopBarActions internal constructor(private val menuState: MenuDialogState) {

    @Composable
    public fun MenuIcon(
        icon: ImageVector,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        hasNotify: Boolean = false,
        tint: Color = LocalContentColor.current,
        onClick: () -> Unit,
    ) {
        Box {
            IconButton(onClick = onClick, modifier = modifier, enabled = enabled) {
                Icon(icon, "", tint = tint)
            }

            TopEndAnimatedVisibility(hasNotify, Modifier.align(Alignment.TopEnd)) {
                NotifyIcon()
            }
        }
    }


    @Composable
    public fun MenuIcon(
        icon: BitmapPainter,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        hasNotify: Boolean = false,
        onClick: () -> Unit,
    ) {
        Box {
            IconButton(onClick = onClick, modifier = modifier, enabled = enabled) {
                Icon(icon, "")
            }

            TopEndAnimatedVisibility(hasNotify, Modifier.align(Alignment.TopEnd)) {
                NotifyIcon()
            }
        }
    }

    @Composable
    public fun ExpandedMenu(): Unit =
        MenuIcon(icon = Icons.Default.MoreVert, onClick = menuState::show)
}

@Stable
public sealed interface NavigationButton {
    @Stable
    public data class Scaffold(val state: DrawerState, val hasNotify: Boolean = false) : NavigationButton

    @Stable
    public data class Back(val onClick: () -> Unit) : NavigationButton

    @Stable
    public data class Close(val onClick: () -> Unit) : NavigationButton
}
