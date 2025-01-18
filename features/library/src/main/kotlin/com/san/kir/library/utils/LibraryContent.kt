package com.san.kir.library.utils

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.san.kir.core.utils.viewModel.Action
import com.san.kir.library.ui.library.ItemsState
import com.san.kir.library.ui.library.LibraryAction
import com.san.kir.library.ui.library.LibraryState

@Composable
internal fun ColumnScope.LibraryContent(
    pagerState: PagerState,
    state: LibraryState,
    itemsState: ItemsState.Ok,
    sendAction: (Action) -> Unit
) {
    LaunchedEffect(itemsState, pagerState.currentPage) {
        if (pagerState.currentPage == pagerState.pageCount)
            pagerState.animateScrollToPage(0)
        if (itemsState.items.isNotEmpty())
            sendAction(LibraryAction.SetCurrentCategory(itemsState.items[pagerState.currentPage]))
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        key = { itemsState.items[it].id }
    ) { page ->
        LibraryPage(
            item = itemsState.items[page],
            showCategory = state.showCategory,
            sendAction = sendAction
        )
    }

}
