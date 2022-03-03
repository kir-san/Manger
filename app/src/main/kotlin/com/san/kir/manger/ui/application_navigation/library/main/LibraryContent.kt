package com.san.kir.manger.ui.application_navigation.library.main

import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.san.kir.core.compose_utils.ScrollableTabs
import com.san.kir.manger.R

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ColumnScope.LibraryContent(
    navigateToCategories: () -> Unit,
    navigateToCatalogs: () -> Unit,
    navigateToInfo: (String) -> Unit,
    navigateToStorage: (String) -> Unit,
    navigateToStats: (String) -> Unit,
    navigateToChapters: (String) -> Unit,
    viewModel: LibraryViewModel,
) {
    val isAction by viewModel.isAction.collectAsState()
    val isEmpty by viewModel.isEmpty.collectAsState()

    val categories by viewModel.preparedCategories.collectAsState(emptyList())

    if (isAction) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

    val pagerState = rememberPagerState()

    LaunchedEffect(categories, pagerState.currentPage) {
        if (pagerState.currentPage >= categories.size)
            pagerState.animateScrollToPage(0)
        if (categories.isNotEmpty())
            viewModel.changeCurrentCategory(categories[pagerState.currentPage])
    }

    if (isEmpty.not() && categories.isNotEmpty() && pagerState.currentPage < categories.size) {

        val draged by pagerState.interactionSource.collectIsDraggedAsState()
        if (draged) viewModel.changeSelectedManga(false)

        // Название вкладок
        ScrollableTabs(
            pagerState,
            items = viewModel.categoryNames) {
            viewModel.changeSelectedManga(false)
            pagerState.animateScrollToPage(it)
        }

        // Перелистываемые вкладки
        HorizontalPager(
            count = viewModel.categoryNames.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f, true),
        ) { index ->
            LibraryPage(
                navigateToCatalogs = navigateToCatalogs,
                navigateToInfo = navigateToInfo,
                navigateToStorage = navigateToStorage,
                navigateToStats = navigateToStats,
                navigateToChapters = navigateToChapters,
                item = categories[index],
                viewModel = viewModel,
            )
        }

    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                Text(text = stringResource(id = R.string.library_no_categories))
                Button(onClick = navigateToCategories) {
                    Text(text = stringResource(id = R.string.library_to_categories))
                }
            }
        }
    }
}
