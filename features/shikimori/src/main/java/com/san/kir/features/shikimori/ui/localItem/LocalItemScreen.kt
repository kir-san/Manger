package com.san.kir.features.shikimori.ui.localItem

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenPadding
import com.san.kir.core.compose.horizontalAndBottomInsetsPadding
import com.san.kir.core.compose.rememberImage
import com.san.kir.core.compose.topBar
import com.san.kir.data.models.extend.SimplifiedMangaWithChapterCounts
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.logic.useCases.SyncState
import com.san.kir.features.shikimori.ui.util.Chapters
import com.san.kir.features.shikimori.ui.util.Description
import com.san.kir.features.shikimori.ui.util.DialogsSyncState
import com.san.kir.features.shikimori.ui.util.MangaNames
import com.san.kir.features.shikimori.ui.util.body

@Composable
fun LocalItemScreen(
    mangaId: Long,
    navigateUp: () -> Unit,
    navigateToSearch: (String) -> Unit,
) {
    val viewModel: LocalItemViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.sendEvent(LocalItemEvent.Update(mangaId))
    }

    val state by viewModel.state.collectAsState()

    ScreenPadding(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = stringResource(R.string.profile_item_title),
            hasAction = state.manga is MangaState.Load
        ),
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(
                    top = it.calculateTopPadding(),
                    start = Dimensions.default,
                    end = Dimensions.default,
                ),
            contentPadding = horizontalAndBottomInsetsPadding()
        ) {
            when (val manga = state.manga) {
                is MangaState.Ok -> content(
                    manga,
                    state.sync,
                    viewModel::sendEvent,
                    navigateToSearch
                )
                MangaState.Error -> {}
                MangaState.Load -> {}
            }
        }
    }

    DialogsSyncState(state.dialog) { viewModel.sendEvent(LocalItemEvent.Sync(it)) }
}

private fun LazyListScope.content(
    manga: MangaState.Ok,
    sync: SyncState,
    sendEvent: (LocalItemEvent) -> Unit,
    navigateToSearch: (String) -> Unit,
) {
    item { MangaNames(name = manga.item.name) }
    item { Chapters(all = manga.item.all, read = manga.item.read) }
    item { AdditionalInfo(manga = manga.item) }
    item { Divider(modifier = Modifier.padding(vertical = Dimensions.default)) }

    body(
        state = sync,
        findTextId = R.string.online_search_searching,
        okTextId = R.string.online_search_sync,
        foundsTextId = R.string.online_search_founds,
        notFoundsTextId = R.string.online_search_not_founds,
        notFoundsSearchTextId = R.string.online_search_not_founds_ex,
        onSendEvent = { sendEvent(LocalItemEvent.Sync(it)) },
        onSearch = navigateToSearch
    )
}

// Дополнительная информация о манге
@Composable
private fun AdditionalInfo(manga: SimplifiedMangaWithChapterCounts) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.default)
    ) {
        // Лого
        Image(
            rememberImage(manga.logo),
            contentDescription = "manga logo",
            modifier = Modifier
                .weight(2f)
                .padding(horizontal = Dimensions.default),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.weight(3f)) {
            Description(description = manga.description)
        }
    }
}

