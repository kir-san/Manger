package com.san.kir.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.san.kir.core.utils.TestTags
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
private fun indicator(pagerState: PagerState): @Composable (tabPositions: List<TabPosition>) -> Unit =
    { tabPositions ->
        TabRowDefaults.Indicator(
            Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
        )
    }

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ScrollableTabs(
    pagerState: PagerState,
    items: ImmutableList<String>,
    modifier: Modifier = Modifier,
    onTabClick: suspend (index: Int) -> Unit = { pagerState.animateScrollToPage(it) },
) {
    val scope: CoroutineScope = rememberCoroutineScope()
    val tabClicker: (Int) -> Unit = remember { { scope.launch { onTabClick(it) } } }

    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        indicator = indicator(pagerState),
        divider = {},
        modifier = modifier.background(MaterialTheme.colors.primarySurface)
    ) {
        items.forEachIndexed { index, item ->
            Tab(
                modifier = Modifier.testTag(TestTags.Library.tab),
                selected = pagerState.currentPage == index,
                text = { Text(text = item) },
                onClick = { tabClicker(index) }
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Tabs(
    pagerState: PagerState,
    items: ImmutableList<Int>,
    onTabClick: suspend (index: Int) -> Unit = { pagerState.animateScrollToPage(it) },
) {
    val scope: CoroutineScope = rememberCoroutineScope()
    val tabClicker: (Int) -> Unit = remember { { scope.launch { onTabClick(it) } } }

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        indicator = indicator(pagerState),
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
    ) {
        items.forEachIndexed { index, item ->
            Tab(
                selected = pagerState.currentPage == index,
                text = { Text(text = stringResource(id = item)) },
                onClick = { tabClicker(index) }
            )
        }
    }
}

