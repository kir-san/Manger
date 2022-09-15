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
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.ScreenList
import com.san.kir.core.compose_utils.topBar
import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.internet.LocalConnectManager
import com.san.kir.core.support.R
import com.san.kir.features.shikimori.ui.accountItem.AccountItem
import com.san.kir.features.shikimori.ui.accountRate.AccountRateScreen
import com.san.kir.features.shikimori.ui.accountScreen.AccountScreen
import com.san.kir.features.shikimori.ui.localItem.LocalItemScreen
import com.san.kir.features.shikimori.ui.localItems.LocalItemsScreen
import com.san.kir.features.shikimori.ui.search.ShikiSearchScreen
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
    var nav: ShikiNavTarget by remember { mutableStateOf(ShikiNavTarget.LocalItems) }
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
                        AccountItem { nav = ShikiNavTarget.Catalog }
                    }

                }
            ShikiNavTarget.Catalog ->
                AccountScreen(
                    navigateUp = { nav = ShikiNavTarget.Start },
                    navigateToShikiItem = {
                        Timber.v(it.toString())
                        nav = ShikiNavTarget.AccountRate(it)
                    },
                    navigateToLocalItems = { nav = ShikiNavTarget.LocalItems },
                    navigateToSearch = { /*nav = ShikiNavTarget.Search*/ }
                )
            is ShikiNavTarget.AccountRate -> {
                AccountRateScreen(
                    navigateUp = { nav = ShikiNavTarget.Search },
                    navigateToSearch = {},
                    mangaId = target.id,
                    rateId = -1L,
                )
            }
            ShikiNavTarget.Search -> {
                ShikiSearchScreen(
                    navigateUp = { nav = ShikiNavTarget.Start },
                    navigateToItem = { nav = ShikiNavTarget.AccountRate(it) },
                    searchText = "Fetish na Yuu",
                )
            }
            ShikiNavTarget.LocalItems -> {
                LocalItemsScreen(
                    navigateUp = { nav = ShikiNavTarget.Catalog },
                    navigateToItem = { nav = ShikiNavTarget.LocalItem(it) }
                )
            }
            is ShikiNavTarget.LocalItem -> {
                LocalItemScreen(
                    mangaId = target.id,
                    navigateUp = { nav = ShikiNavTarget.LocalItems },
                    navigateToSearch = {})
            }
        }
    }
}

sealed interface ShikiNavTarget {
    object Start : ShikiNavTarget
    object Catalog : ShikiNavTarget
    data class AccountRate(val id: Long) : ShikiNavTarget
    object Search : ShikiNavTarget
    object LocalItems : ShikiNavTarget
    data class LocalItem(val id: Long) : ShikiNavTarget
}
