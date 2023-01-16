package com.san.kir.features.shikimori.ui.accountRate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenPadding
import com.san.kir.core.compose.ToolbarProgress
import com.san.kir.core.compose.horizontalAndBottomInsetsPadding
import com.san.kir.core.compose.topBar
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.logic.useCases.SyncState
import com.san.kir.features.shikimori.ui.util.AdditionalInfo
import com.san.kir.features.shikimori.ui.util.Description
import com.san.kir.features.shikimori.ui.util.DialogsSyncState
import com.san.kir.features.shikimori.ui.util.MangaNames
import com.san.kir.features.shikimori.ui.util.body

@Composable
fun AccountRateScreen(
    navigateUp: () -> Boolean,
    navigateToSearch: (String) -> Unit,
    mangaId: Long,
    //    rateId: Long,
) {
    val viewModel: AccountRateViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.sendEvent(AccountRateEvent.Update(id = mangaId))
    }

    val state by viewModel.state.collectAsState()

    ScreenPadding(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = stringResource(R.string.profile_item_title),
            actions = {
                if (state.manga is MangaState.Ok)
                    when (state.profile) {
                        // В зависимости от текущего состояния проверки наличия в профиле
                        // отображаются разные иконки
                        is ProfileState.Ok ->
                            MenuIcon(
                                icon = Icons.Default.Delete,
                                onClick = { viewModel.sendEvent(AccountRateEvent.ExistToggle) }
                            )
                        ProfileState.None  ->
                            MenuIcon(
                                icon = Icons.Default.Add,
                                onClick = { viewModel.sendEvent(AccountRateEvent.ExistToggle) }
                            )
                        ProfileState.Load  -> ToolbarProgress()
                    }
            }
        ),
        onRefresh = { viewModel.sendEvent(AccountRateEvent.Update()) }
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
                is MangaState.Ok -> {
                    content(
                        manga = manga,
                        profile = state.profile,
                        sync = state.sync,
                        sendEvent = viewModel::sendEvent,
                        navigateToSearch = navigateToSearch
                    )
                }
                MangaState.Load  -> {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(Dimensions.default)
                                .fillMaxWidth()
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                MangaState.Error -> {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(R.string.error),
                                style = MaterialTheme.typography.h6,
                                modifier = Modifier.padding(Dimensions.default)
                            )
                            Button(
                                onClick = {
                                    viewModel.sendEvent(AccountRateEvent.Update(id = mangaId))
                                }
                            ) {
                                Text(stringResource(R.string.try_again))
                            }
                        }
                    }
                }
            }
        }
    }

    DialogsSyncState(state.dialog) { viewModel.sendEvent(AccountRateEvent.Sync(it)) }
}

private fun LazyListScope.content(
    manga: MangaState.Ok,
    profile: ProfileState,
    sync: SyncState,
    sendEvent: (AccountRateEvent) -> Unit,
    navigateToSearch: (String) -> Unit,
) {
    item {
        MangaNames(
            name = manga.item.name,
            russianName = manga.item.russian,
        )
    }
    item {
        AdditionalInfo(
            manga = manga.item,
            state = profile,
            onChange = { sendEvent(AccountRateEvent.Update(it)) }
        )
    }
    item {
        Description(
            state = profile,
            description = manga.item.description,
        )
    }

    item {
        Divider(modifier = Modifier.padding(vertical = Dimensions.default))
    }

    body(
        state = sync,
        findTextId = R.string.local_search_searching,
        okTextId = R.string.local_search_sync,
        foundsTextId = R.string.local_search_founds,
        notFoundsTextId = R.string.local_search_not_founds,
        notFoundsSearchTextId = R.string.local_search_not_founds_ex,
        onSendEvent = { sendEvent(AccountRateEvent.Sync(it)) },
        onSearch = navigateToSearch
    )
}

