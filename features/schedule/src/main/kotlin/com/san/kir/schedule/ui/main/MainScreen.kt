package com.san.kir.schedule.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenContent
import com.san.kir.core.compose.ScrollableTabs
import com.san.kir.core.compose.topBar
import com.san.kir.schedule.R
import com.san.kir.schedule.utils.pages
import kotlinx.collections.immutable.toPersistentList

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MainScreen(
    navigateUp: () -> Boolean,
    navigateToItem: (Long) -> Unit,
) {
    ScreenContent(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = stringResource(R.string.schedule),
            actions = {
                MenuIcon(
                    icon = Icons.Default.Add,
                    onClick = { navigateToItem(-1L) }
                )
            },
        ),
        additionalPadding = 0.dp
    ) {
        val pagerState = rememberPagerState()
        val pages = pages()

        ScrollableTabs(
            pagerState,
            items = pages.map { stringResource(it.nameId) }.toPersistentList()
        )

        HorizontalPager(
            count = pages.size,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { index ->
            pages[index].content(navigateToItem)
        }
    }
}
