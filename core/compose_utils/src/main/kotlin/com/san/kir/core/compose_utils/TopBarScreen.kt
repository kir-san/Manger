package com.san.kir.core.compose_utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.imePadding
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.san.kir.core.utils.TestTags
import kotlinx.coroutines.launch


@Composable
fun TopBarScreenWithInsets(
    modifier: Modifier = Modifier,
    navigationButtonListener: () -> Unit = { },
    scaffoldState: ScaffoldState? = null,
    title: String = "",
    subtitle: String = "",
    additionalPadding: Dp = Dimensions.small,
    actions: @Composable RowScope.() -> Unit = {},
    topBar: @Composable () -> Unit = {
        PreparedTopBar(navigationButtonListener, title, subtitle, scaffoldState, actions)
    },
    listContent: (LazyListScope.() -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    paddingContent: @Composable ((PaddingValues) -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null,
) {
    Scaffold(modifier = modifier,
        scaffoldState = scaffoldState ?: rememberScaffoldState(),
        drawerContent = drawerContent,
        drawerGesturesEnabled = true,
        bottomBar = bottomBar,
        topBar = topBar) { contentPadding ->

        paddingContent?.invoke(contentPadding)

        content?.let { con ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        rememberInsetsPaddingValues(
                            insets = LocalWindowInsets.current.systemBars,
                            applyStart = true, applyEnd = true,
                            applyBottom = false, applyTop = false,
                            additionalTop = contentPadding.calculateTopPadding(),
                            additionalBottom = contentPadding.calculateBottomPadding(),
                            additionalStart = additionalPadding, additionalEnd = additionalPadding
                        )
                    )
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(additionalPadding))
                con()
                if (LocalWindowInsets.current.ime.bottom <= 0 && additionalPadding > 0.dp)
                    Spacer(modifier = Modifier.navigationBarsHeight(additionalPadding))
                else
                    Spacer(modifier = Modifier.height(additionalPadding))
            }
        }

        listContent?.let { listCon ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        rememberInsetsPaddingValues(
                            insets = LocalWindowInsets.current.systemBars,
                            applyStart = true, applyEnd = true,
                            applyBottom = false, applyTop = false,
                            additionalTop = contentPadding.calculateTopPadding(),
                            additionalBottom = contentPadding.calculateBottomPadding(),
                            additionalStart = additionalPadding, additionalEnd = additionalPadding
                        )
                    )
                    .imePadding(),
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.systemBars,
                    applyTop = false,
                    additionalTop = additionalPadding
                ),
            ) {
                listCon()
            }
        }
    }
}

@Composable
fun TopBarScreen(
    modifier: Modifier = Modifier,
    navHostController: NavHostController? = null,
    navigateUp: () -> Unit = { navHostController?.navigateUp() },
    scaffoldState: ScaffoldState? = null,
    title: String = "",
    subtitle: String = "",
    additionalPadding: Dp = Dimensions.small,
    topBar: @Composable () -> Unit = {
        PreparedTopBar(navigateUp, title, subtitle, scaffoldState, actions)
    },
    actions: @Composable RowScope.() -> Unit = {},
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    TopBarScreenWithInsets(
        modifier = modifier,
        topBar = topBar,
        paddingContent = content,
        drawerContent = drawerContent,
        additionalPadding = additionalPadding
    )
}

@Composable
fun TopBarScreenContent(
    modifier: Modifier = Modifier,
    navHostController: NavHostController? = null,
    navigateUp: () -> Unit = { navHostController?.navigateUp() },
    scaffoldState: ScaffoldState? = null,
    title: String = "",
    subtitle: String = "",
    additionalPadding: Dp = Dimensions.small,
    topBar: @Composable () -> Unit = {
        PreparedTopBar(navigateUp, title, subtitle, scaffoldState, actions)
    },
    actions: @Composable RowScope.() -> Unit = {},
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null,
) {
    TopBarScreenWithInsets(
        modifier = modifier,
        topBar = topBar,
        content = content,
        drawerContent = drawerContent,
        additionalPadding = additionalPadding
    )
}

@Composable
fun TopBarScreenList(
    modifier: Modifier = Modifier,
    navHostController: NavHostController? = null,
    navigateUp: () -> Unit = { navHostController?.navigateUp() },
    additionalPadding: Dp = Dimensions.small,
    title: String = "",
    subtitle: String = "",
    scaffoldState: ScaffoldState? = null,
    topBar: @Composable () -> Unit = {
        PreparedTopBar(navigateUp, title, subtitle, null, actions)
    },
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    listContent: (LazyListScope.() -> Unit)? = null,
) {
    TopBarScreenWithInsets(
        modifier = modifier,
        additionalPadding = additionalPadding,
        topBar = topBar,
        scaffoldState = scaffoldState ,
        drawerContent = drawerContent,
        bottomBar = bottomBar,
        listContent = listContent,
    )
}

@Composable
fun PreparedTopBar(
    navigationButtonListener: () -> Unit = { },
    title: String = "",
    subtitle: String = "",
    scaffoldState: ScaffoldState? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()

    TopAppBar(
        title = {
            Column {
                Text(text = title, maxLines = 1)
                if (subtitle.isNotEmpty()) Text(
                    text = subtitle,
                    style = MaterialTheme.typography.subtitle2,
                    maxLines = 1
                )
            }
        },
        navigationIcon = {
            if (scaffoldState == null) {
                IconButton(
                    modifier = Modifier.testTag(TestTags.Drawer.nav_back),
                    onClick = navigationButtonListener
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
        },
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(0.dp),
        actions = actions,
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.systemBars,
            applyBottom = false, applyTop = false
        )
    )
}
