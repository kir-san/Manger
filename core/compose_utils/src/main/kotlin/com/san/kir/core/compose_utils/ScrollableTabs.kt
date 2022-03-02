package com.san.kir.core.compose_utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.san.kir.core.utils.TestTags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ScrollableTabs(
    pagerState: PagerState,
    items: List<String>,
    scope: CoroutineScope = rememberCoroutineScope(),
    onTabClick: suspend (index: Int) -> Unit = { pagerState.animateScrollToPage(it) },
) {
    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        },
        divider = {},
        modifier = Modifier
            .background(MaterialTheme.colors.primarySurface)
    ) {
        items.forEachIndexed { index, item ->
            Tab(
                modifier = Modifier.testTag(TestTags.Library.tab),
                selected = pagerState.currentPage == index,
                text = { Text(text = item) },
                onClick = { scope.launch { onTabClick(index) } }
            )
        }
    }
}
