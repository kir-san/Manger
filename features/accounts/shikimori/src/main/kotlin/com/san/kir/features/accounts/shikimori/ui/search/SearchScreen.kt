package com.san.kir.features.accounts.shikimori.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.animation.SharedParams
import com.san.kir.core.compose.topBar
import com.san.kir.core.utils.flow.collectAsStateWithLifecycle
import com.san.kir.core.utils.viewModel.rememberSendAction
import com.san.kir.core.utils.viewModel.stateHolder
import com.san.kir.features.accounts.shikimori.R
import com.san.kir.features.accounts.shikimori.logic.models.AccountMangaItem
import com.san.kir.features.accounts.shikimori.logic.useCases.CanBind
import com.san.kir.features.accounts.shikimori.ui.util.MangaItemContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShikiSearchScreen(
    accountId: Long,
    navigateUp: () -> Unit,
    navigateToItem: (AccountMangaItem, SharedParams) -> Unit,
    searchText: String,
) {
    val holder: SearchStateHolder = stateHolder { SearchViewModel(accountId, searchText) }
    val state by holder.state.collectAsStateWithLifecycle()
    val sendAction = holder.rememberSendAction()

    ScreenList(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = stringResource(R.string.online_search_title),
            initSearchText = searchText,
            onSearchTextChange = { sendAction(SearchAction.Search(it)) },
            hasAction = state.search is SearchingState.Load,
        ),
    ) {
        when (val search = state.search) {
            is SearchingState.Ok -> {
                items(search.items, key = AccountMangaItem::id) { item ->
                    MangaItemContent(
                        avatar = item.logo,
                        mangaName = item.russian.ifEmpty { item.name },
                        readingChapters = 0,
                        secondaryText = stringResource(
                            R.string.volumes_and_chapters, item.volumes, item.all,
                        ),
                        canBind = CanBind.No,
                        onClick = { navigateToItem(item, it) },
                        inAccount = item.inAccount,
                    )
                }
            }

            SearchingState.None -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimensions.default)
                            .animateItem(fadeInSpec = null, fadeOutSpec = null),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.online_search_nothing),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            else -> {}
        }

    }
}
