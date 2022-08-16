package com.san.kir.features.shikimori.ui.accountScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.ScreenPadding
import com.san.kir.core.compose_utils.SmallSpacer
import com.san.kir.core.compose_utils.topBar
import com.san.kir.features.shikimori.BackgroundTasks
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.ui.accountItem.LoginState
import com.san.kir.features.shikimori.ui.util.LogOutDialog
import com.san.kir.features.shikimori.ui.util.MangaItemContent
import com.san.kir.features.shikimori.ui.util.TextLoginOrNot
import com.san.kir.features.shikimori.useCases.CanBind

@Composable
fun ShikimoriScreen(
    navigateUp: () -> Unit,
    navigateToShikiItem: (id: Long) -> Unit,
    navigateToLocalItems: () -> Unit,
    navigateToSearch: () -> Unit,
) {
    val viewModel: ShikimoriViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    ScreenPadding(
        topBar = topBar(
            viewModel = viewModel,
            navigateUp = navigateUp,
            navigateToSearch = navigateToSearch,
            state = state.login,
            hasAction = state.action
        ),
        additionalPadding = Dimensions.zero,
        fab = {
            if (state.login is LoginState.LogIn)
                FloatingActionButton(onClick = navigateToLocalItems) {
                    Icon(Icons.Default.LocalLibrary, contentDescription = "local library")
                }
        }
    ) { contentPadding ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(false),
            onRefresh = { viewModel.sendEvent(UIEvent.Update) },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                    .padding(
                        top = contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding()
                    )
                    .imePadding()
            ) {
                CatalogContent(state.items, navigateToShikiItem)
            }
        }
    }

    LogOutDialog(
        state = state.dialog,
        onDismiss = { viewModel.sendEvent(UIEvent.CancelLogOut) },
        onConfirm = { viewModel.sendEvent(UIEvent.LogOut) }
    )
}

@Composable
private fun topBar(
    viewModel: ShikimoriViewModel,
    navigateUp: () -> Unit,
    navigateToSearch: () -> Unit,
    state: LoginState,
    hasAction: BackgroundTasks,
) = topBar(
    navigationListener = navigateUp,
    title = stringResource(R.string.site_name),
    subtitleContent = { TextLoginOrNot(state) },
    actions = {
        when (state) {
            is LoginState.LogIn -> {
                MenuIcon(icon = Icons.Default.Search, onClick = navigateToSearch)

                ExpandedMenu {
                    MenuText(
                        R.string.update_data,
                        onClick = { viewModel.sendEvent(UIEvent.Update) })
                    MenuText(R.string.logout, onClick = { viewModel.sendEvent(UIEvent.LogOut) })
                }
            }
            else -> {}
        }

    },
    hasAction = if (state is LoginState.LogIn) hasAction.loading || hasAction.checkBind else false,
)

@Composable
private fun CatalogContent(
    state: ScreenItems,
    navigateToItem: (id: Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = WindowInsets
            .systemBars
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            .asPaddingValues()
    ) {
        if (state.bind.isNotEmpty()) {
            stickyHeader(
                textRes = R.string.synced_catalog_items,
                count = state.bind.size
            )

            items(state.bind, key = { item -> item.id }) { item ->
                MangaItemContent(
                    avatar = item.logo,
                    mangaName = item.name,
                    readingChapters = item.read,
                    allChapters = item.all,
                    currentStatus = item.status,
                    canBind = CanBind.Already,
                    onClick = { navigateToItem(item.id) }
                )
            }
        }
        if (state.unBind.isNotEmpty()) {
            stickyHeader(
                textRes = R.string.nonsynced_catalog_items,
                count = state.unBind.size,
                secondaryCount = state.unBind.count { (_, bind) -> bind == CanBind.Ok }
            )

            items(
                state.unBind,
                key = { item -> item.item.id },
                contentType = { item -> item.status }
            ) { (item, canBind) ->
                MangaItemContent(
                    avatar = item.logo,
                    mangaName = item.name,
                    readingChapters = item.read,
                    allChapters = item.all,
                    currentStatus = item.status,
                    canBind = canBind,
                    onClick = { navigateToItem(item.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.stickyHeader(
    textRes: Int,
    count: Int,
    secondaryCount: Int = 0,
) {
    stickyHeader {
        Card(elevation = Dimensions.small, modifier = Modifier.padding(Dimensions.smaller)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.smaller),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(textRes, count))

                if (secondaryCount > 0) {
                    Text(
                        "-$secondaryCount",
                        modifier = Modifier
                            .padding(horizontal = Dimensions.small)
                            .background(
                                color = Color.Magenta,
                                shape = CircleShape
                            )
                            .padding(horizontal = Dimensions.smaller),
                    )
                }
            }
        }
    }
}

@Preview(
    showSystemUi = true,
)
@Composable
internal fun PreviewHeader() {
    MaterialTheme(darkColors()) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            stickyHeader(textRes = R.string.nonsynced_catalog_items, 34, secondaryCount = 5)

            item {
                SmallSpacer()
            }

            stickyHeader(textRes = R.string.nonsynced_catalog_items, 34, secondaryCount = 35)

            item {
                SmallSpacer()
            }

            stickyHeader(textRes = R.string.nonsynced_catalog_items, 4, secondaryCount = 235)
        }
    }
}
