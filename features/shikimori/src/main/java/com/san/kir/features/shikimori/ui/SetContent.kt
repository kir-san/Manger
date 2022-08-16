package com.san.kir.features.shikimori.ui

import androidx.compose.animation.Crossfade
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.core.app.ComponentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.ScreenList
import com.san.kir.core.compose_utils.topBar
import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.internet.LocalConnectManager
import com.san.kir.core.support.R
import com.san.kir.features.shikimori.ui.profile_item.ProfileItemScreen
import com.san.kir.features.shikimori.ui.search.ShikiSearchScreen
import com.san.kir.features.shikimori.ui.accountItem.ShikimoriAccountItem
import com.san.kir.features.shikimori.ui.accountScreen.ShikimoriScreen
import timber.log.Timber

fun ComponentActivity.setContent() {
    val connectManager = ConnectManager(this.application)
    setContentView(
        ComposeView(this).apply {
            setContent {
                MaterialTheme {

                    CompositionLocalProvider(LocalConnectManager provides connectManager) {
                        ShikimoriContent()
                    }
                }
            }
        }
    )
}


@Composable
internal fun ShikimoriContent() {
//    val viewModelItem = hiltViewModel<ShikiItemViewModel>()
    var nav: ShikiNavTarget by remember { mutableStateOf(ShikiNavTarget.Catalog) }
    Timber.plant(Timber.DebugTree())

    Crossfade(targetState = nav) { target ->
        when (target) {
            ShikiNavTarget.Start ->
                ScreenList(
                    topBar = topBar(
                        navigationListener = {},
                        title = stringResource(R.string.main_menu_accounts),
                    ),
                    additionalPadding = Dimensions.zero
                ) {
                    item(key = "Shiki") {
                        ShikimoriAccountItem { nav = ShikiNavTarget.Catalog }
                    }

                }
            ShikiNavTarget.Catalog ->
                ShikimoriScreen(
                    navigateUp = { nav = ShikiNavTarget.Start },
                    navigateToShikiItem = { /*nav = ShikiNavTarget.ShikiItem(it)*/ },
                    navigateToLocalItems = {},
                    navigateToSearch = { /*nav = ShikiNavTarget.Search*/ }
                )
//            is ShikiNavTarget.ShikiItem -> {
//                viewModelItem.update(target.id)
//                ShikiItemScreen(
//                    viewModel = viewModelItem,
//                    navigateUp = { nav = ShikiNavTarget.Catalog },
//                    navigateToSearch = {}
//                )
//            }
            ShikiNavTarget.Search -> {
                ShikiSearchScreen(
                    navigateUp = { nav = ShikiNavTarget.Start },
                    navigateToItem = { nav = ShikiNavTarget.SearchItem(it) },
                    searchText = "Fetish na Yuu",
                    viewModel = hiltViewModel(),
                )
            }
            is ShikiNavTarget.SearchItem -> {
                ProfileItemScreen(
                    navigateUp = { nav = ShikiNavTarget.Search },
                    navigateToSearch = {},
                    mangaId = target.id,
                    rateId = -1L,
                    viewModel = hiltViewModel(),
                )
            }
            else -> {}
        }
    }
}

sealed class ShikiNavTarget(val id: Long = 0) {
    object Start : ShikiNavTarget()
    object Catalog : ShikiNavTarget()
    class ShikiItem(id: Long) : ShikiNavTarget(id)
    object Search : ShikiNavTarget()
    class SearchItem(id: Long) : ShikiNavTarget(id)
}
