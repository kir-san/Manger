package com.san.kir.core.compose

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.san.kir.core.compose.animation.rememberNestedScrollConnection

@Composable
internal fun BaseScreen(
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState? = null,
    additionalPadding: Dp = Dimensions.default,
    enableCollapsingBars: Boolean = false,
    topBar: @Composable (Dp) -> Unit,
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

    val animatedHeight by animateDpAsState(targetValue = with(density) { height.toDp() })

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
                    Spacer(modifier = Modifier
                        .height(additionalPadding)
                        .bottomInsetsPadding())
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
        enableCollapsingBars = enableCollapsingBars
    )
}
