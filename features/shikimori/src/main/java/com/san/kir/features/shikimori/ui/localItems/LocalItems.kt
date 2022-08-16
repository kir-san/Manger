package com.san.kir.features.shikimori.ui.localItems

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.ScreenList
import com.san.kir.core.compose_utils.topBar
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.ui.util.MangaItemContent

@Composable
fun LocalItemsScreen(
    navigateUp: () -> Unit,
    navigateToItem: (Long) -> Unit,
) {
    val viewModel: LocalItemsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    ScreenList(
        topBar = topBar(
            navigationListener = navigateUp,
            title = stringResource(R.string.local_items_title),
            subtitle = stringResource(R.string.local_items_subtitle, state.unbind.count()),
            hasAction = state.action.checkBind,
        ),
        additionalPadding = Dimensions.zero
    ) {
        items(state.unbind,
              key = { item -> item.item.id }
        ) { (item, bind) ->
            MangaItemContent(
                avatar = item.logo,
                mangaName = item.name,
                canBind = bind,
                readingChapters = item.read,
                allChapters = item.all,
                onClick = { navigateToItem(item.id) }
            )
        }
    }

}
