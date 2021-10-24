package com.san.kir.manger.ui.application_navigation.library.main

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.san.kir.manger.R
import com.san.kir.manger.ui.application_navigation.MainNavTarget
import com.san.kir.manger.ui.utils.TestTags
import com.san.kir.manger.ui.utils.navigate
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ColumnScope.LibraryContent(
    nav: NavHostController,
    viewModel: LibraryViewModel,
) {
    val isAction by viewModel.isAction.collectAsState()
    val isEmpty by viewModel.isEmpty.collectAsState()

    val categories by viewModel.preparedCategories.collectAsState(emptyList())

    val categoryNames by viewModel.categoryNames.collectAsState(emptyList())

    val scope = rememberCoroutineScope()

    if (isAction) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

    if (isEmpty.not() && categories.isNotEmpty()) {
        val pagerState = rememberPagerState()

        LaunchedEffect(pagerState.currentPage) {
            if (pagerState.currentPage == pagerState.pageCount)
                pagerState.animateScrollToPage(pagerState.pageCount - 1)
            viewModel.changeCurrentCategory(categories[pagerState.currentPage])
        }
        val draged by pagerState.interactionSource
            .collectIsDraggedAsState()
        if (draged) viewModel.changeSelectedManga(false)

        // Название вкладок
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            },
            divider = {},
            modifier = Modifier
                .height(40.dp)
                .background(MaterialTheme.colors.primarySurface)
                .padding(
                    rememberInsetsPaddingValues(
                        insets = LocalWindowInsets.current.systemBars,
                        applyBottom = false, applyTop = false
                    )
                )
        ) {
            categoryNames.forEachIndexed { index, item ->
                Tab(
                    modifier = Modifier.testTag(TestTags.Library.tab),
                    selected = pagerState.currentPage == index,
                    text = { Text(text = item) },
                    onClick = {
                        scope.launch {
                            viewModel.changeSelectedManga(false)
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        }

        // Перелистываемые вкладки
        HorizontalPager(
            count = categoryNames.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f, true),
        ) { index -> LibraryPage(nav, categories[index], viewModel) }

    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                Text(text = stringResource(id = R.string.library_no_categories))
                Button(onClick = { nav.navigate(MainNavTarget.Categories) }) {
                    Text(text = stringResource(id = R.string.library_to_categories))
                }
            }
        }
    }
}
