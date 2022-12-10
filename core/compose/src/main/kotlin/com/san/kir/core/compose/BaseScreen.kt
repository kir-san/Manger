@file:OptIn(ExperimentalMaterialApi::class)

package com.san.kir.core.compose

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.san.kir.core.compose.animation.rememberNestedScrollConnection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun BaseScreen(
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState? = null,
    additionalPadding: Dp = Dimensions.default,
    enableCollapsingBars: Boolean = false,
    topBar: @Composable (Dp) -> Unit,
    onRefresh: (() -> Unit)? = null,
    listContent: (LazyListScope.() -> Unit)? = null,
    bottomBar: @Composable (Dp) -> Unit = {},
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    paddingContent: @Composable ((PaddingValues) -> Unit)? = null,
    fab: @Composable (() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null,
) {
    val density = LocalDensity.current
    val pixelValue = with(density) { Dimensions.appBarHeight.toPx() }
    val (height, heightChanger) = remember { mutableStateOf(pixelValue) }
    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    val animatedHeight by animateDpAsState(targetValue = with(density) { height.toDp() })

    fun refresh() = scope.launch {
        refreshing = true
        onRefresh?.invoke()
        delay(1500)
        refreshing = false
    }

    val refreshState = onRefresh?.let {
        rememberPullRefreshState(refreshing = refreshing, onRefresh = ::refresh)
    }


    Scaffold(
        modifier = modifier
            .nestedScroll(
                rememberNestedScrollConnection(
                    onHeightChanged = heightChanger,
                    maxHeight = pixelValue,
                    enable = enableCollapsingBars,
                )
            ),
        scaffoldState = scaffoldState ?: rememberScaffoldState(),
        drawerContent = drawerContent,
        drawerGesturesEnabled = true,
        bottomBar = { bottomBar(animatedHeight) },
        topBar = { topBar(animatedHeight) },
        floatingActionButton = fab ?: {},
    ) { contentPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (refreshState == null) Modifier
                    else Modifier.pullRefresh(refreshState)
                )
        ) {
            paddingContent?.invoke(contentPadding)

            content?.let { con ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            PaddingValues(
                                top = contentPadding.calculateTopPadding(),
                                bottom = contentPadding.calculateBottomPadding(),
                                start = additionalPadding, end = additionalPadding
                            )
                        )
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(additionalPadding))
                    con()
                    if (WindowInsets.ime.getBottom(LocalDensity.current) <= 0 && additionalPadding > 0.dp)
                        Spacer(
                            modifier = Modifier.windowInsetsBottomHeight(
                                WindowInsets.navigationBars.add(
                                    WindowInsets(top = additionalPadding)
                                )
                            )
                        )
                    else
                        Spacer(
                            modifier = Modifier
                                .height(additionalPadding)
                                .bottomInsetsPadding()
                        )
                }
            }

            listContent?.let { listCon ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = contentPadding.calculateTopPadding())
                        .imePadding(),
                    contentPadding = PaddingValues(
                        top = additionalPadding,
                        bottom = systemBarBottomPadding(additionalPadding).calculateBottomPadding()
                    ),
                ) {
                    listCon()
                }
            }

            if (refreshState != null)
                PullRefreshIndicator(
                    refreshing = refreshing,
                    state = refreshState,
                    modifier.align(Alignment.TopCenter)
                )
        }
    }
}

@Composable
fun ScreenPadding(
    modifier: Modifier = Modifier,
    additionalPadding: Dp = Dimensions.default,
    enableCollapsingBars: Boolean = false,
    topBar: @Composable (Dp) -> Unit,
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    fab: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    BaseScreen(
        modifier = modifier,
        topBar = topBar,
        paddingContent = content,
        drawerContent = drawerContent,
        additionalPadding = additionalPadding,
        enableCollapsingBars = enableCollapsingBars,
        fab = fab,
    )
}

@Composable
fun ScreenContent(
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState? = null,
    additionalPadding: Dp = Dimensions.default,
    enableCollapsingBars: Boolean = false,
    topBar: @Composable (Dp) -> Unit,
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null,
) {
    BaseScreen(
        modifier = modifier,
        topBar = topBar,
        content = content,
        scaffoldState = scaffoldState,
        drawerContent = drawerContent,
        additionalPadding = additionalPadding,
        enableCollapsingBars = enableCollapsingBars
    )
}

@Composable
fun ScreenList(
    modifier: Modifier = Modifier,
    additionalPadding: Dp = Dimensions.default,
    scaffoldState: ScaffoldState? = null,
    enableCollapsingBars: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    topBar: @Composable (Dp) -> Unit,
    bottomBar: @Composable (Dp) -> Unit = {},
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    listContent: (LazyListScope.() -> Unit)? = null,
) {
    BaseScreen(
        modifier = modifier,
        additionalPadding = additionalPadding,
        topBar = topBar,
        scaffoldState = scaffoldState,
        drawerContent = drawerContent,
        bottomBar = bottomBar,
        listContent = listContent,
        enableCollapsingBars = enableCollapsingBars,
        onRefresh = onRefresh,
    )
}
