package com.san.kir.features.shikimori.ui.catalog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.imePadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.MenuIcon
import com.san.kir.core.compose_utils.ScrollableTabs
import com.san.kir.core.compose_utils.TopBarScreen
import com.san.kir.data.models.base.ShikiManga
import com.san.kir.data.models.base.ShikimoriAccount
import com.san.kir.data.models.extend.SimplefiedMangaWithChapterCounts
import com.san.kir.features.shikimori.AuthActivity
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.avatar
import com.san.kir.features.shikimori.ui.util.IconLoginOrNot
import com.san.kir.features.shikimori.ui.util.MangaItemContent
import com.san.kir.features.shikimori.ui.util.textLoginOrNot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalFoundationApi::class, ExperimentalPagerApi::class)
@Composable
fun ShikimoriScreen(
    viewModel: ShikimoriViewModel,
    navigateUp: () -> Unit,
    navigateToShikiItem: (id: Long) -> Unit,
    navigateToLocalItem: (id: Long) -> Unit,
) {
    val authData by viewModel.auth.collectAsState()
    val onlineItems by viewModel.onlineCatalog.collectAsState()
    val localItems by viewModel.localCatalog.collectAsState()

    val ctx = LocalContext.current

    TopBarScreen(navigateUp = navigateUp,
        title = stringResource(R.string.site_name),
        subtitle = textLoginOrNot(isLogin = authData.isLogin, nickname = authData.whoami.nickname),
        actions = {
            IconLoginOrNot(isLogin = authData.isLogin,
                login = { AuthActivity.start(ctx) },
                logout = viewModel::logout)
        },
        additionalPadding = 0.dp
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(top = false, bottom = false)
                .padding(top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding())
                .imePadding()) {

            val pagerState = rememberPagerState()

            // Название вкладок
            ScrollableTabs(
                pagerState,
                items = listOf(
                    stringResource(R.string.online_catalog),
                    stringResource(R.string.local_catalog)
                ))

            HorizontalPager(
                count = 2,
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (it == 0) {
                    CatalogContent(viewModel, onlineItems, navigateToShikiItem, authData.isLogin)
                } else {
                    CatalogContent(viewModel, localItems, navigateToLocalItem)
                }
            }
        }
    }
}

@Composable
internal fun CatalogContent(
    viewModel: ShikimoriViewModel,
    catalogItems: List<ShikimoriAccount.AbstractMangaItem>,
    navigateToItem: (id: Long) -> Unit,
    isLogin: Boolean = true,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.small),
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.systemBars,
            applyStart = true, applyEnd = true,
            applyBottom = true, applyTop = false,
        )
    ) {
        item {
            if (catalogItems.isEmpty() && isLogin)
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
        }
        items(catalogItems) { item ->
            val isSynced by viewModel.checkSyncedItem(item).collectAsState()

            MangaItemContent(
                avatar = item.logo,
                mangaName = item.name,
                readingChapters = item.read,
                allChapters = item.all,
                currentStatus = ShikimoriAccount.Status.Watching,
                isSynced = isSynced,
                onClick = { navigateToItem(item.id) }
            )
        }
    }
}
