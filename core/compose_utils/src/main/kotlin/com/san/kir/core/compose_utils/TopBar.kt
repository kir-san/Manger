package com.san.kir.core.compose_utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import com.san.kir.core.utils.TestTags
import kotlinx.coroutines.launch

@Composable
fun PreparedTopBar(
    navigationListener: () -> Unit = { },
    title: String = "",
    subtitleContent: @Composable (() -> Unit)? = null,
    subtitle: String = "",
    height: Dp = Dimensions.appBarHeight,
    scaffoldState: ScaffoldState? = null,
    actions: @Composable TopBarActions.() -> Unit = {},
    hasAction: Boolean = false,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
) {
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                Column {
                    Text(text = title, maxLines = 1)

                    ProvideTextStyle(value = MaterialTheme.typography.subtitle2) {
                        if (subtitleContent != null) {
                            subtitleContent()
                        } else
                            if (subtitle.isNotEmpty()) {
                                Text(
                                    text = subtitle,
                                    maxLines = 1
                                )
                            }
                    }
                }
            },
            navigationIcon = {
                Box(modifier = Modifier.startInsetsPadding()) {
                    if (scaffoldState == null) {
                        IconButton(
                            modifier = Modifier.testTag(TestTags.Drawer.nav_back),
                            onClick = navigationListener
                        ) {
                            Icon(Icons.Default.ArrowBack, "")
                        }
                    } else {
                        IconButton(
                            modifier = Modifier.testTag(TestTags.Drawer.drawer_open),
                            onClick = {
                                coroutineScope.launch {
                                    scaffoldState.drawerState.open()
                                }
                            }) { Icon(Icons.Default.Menu, "") }
                    }
                }
            },
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(height),
            actions = {
                Row(modifier = Modifier.endInsetsPadding()) {
                    TopBarActions().actions()
                }
            },
            backgroundColor = backgroundColor,
        )

        if (hasAction)
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
            )
    }
}

@Composable
fun topBar(
    title: String = "",
    subtitle: String = "",
    subtitleContent: @Composable (() -> Unit)? = null,
    scaffoldState: ScaffoldState? = null,
    actions: @Composable TopBarActions.() -> Unit = {},
    navigationListener: () -> Unit = {},
    enableSearchField: Boolean = false,
    initSearchText: String = "",
    onSearchTextChange: (String) -> Unit = {},
    hasAction: Boolean = false,
    progressAction: Float? = null,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
): @Composable (Dp) -> Unit = {
    Column(modifier = Modifier.fillMaxWidth()) {
        PreparedTopBar(
            title = title,
            subtitle = subtitle,
            subtitleContent = subtitleContent,
            scaffoldState = scaffoldState,
            actions = actions,
            navigationListener = navigationListener,
            height = it,
            backgroundColor = backgroundColor,
        )

        AnimatedVisibility(visible = enableSearchField) {
            SearchTextField(
                initialValue = initSearchText,
                onChangeValue = onSearchTextChange
            )
        }

        if (hasAction) {
            if (progressAction != null) {
                LinearProgressIndicator(
                    progress = progressAction,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

class TopBarActions internal constructor() {

    @Composable
    fun MenuIcon(
        icon: ImageVector,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
        onClick: () -> Unit,
    ) {
        IconButton(onClick = onClick, modifier = modifier, enabled = enabled) {
            Icon(icon, "", tint = tint)
        }
    }

    @Composable
    fun ExpandedMenu(
        actions: @Composable ExpandedMenuScope.() -> Unit,
    ) {
        var expanded by remember { mutableStateOf(false) }

        MenuIcon(icon = Icons.Default.MoreVert) {
            expanded = true
        }

        ExpandedMenu(
            expanded = expanded,
            onCloseMenu = {
                expanded = false
            },
            actions = actions,
        )
    }
}
