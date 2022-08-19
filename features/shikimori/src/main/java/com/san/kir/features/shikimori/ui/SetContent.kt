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
import com.san.kir.features.shikimori.ui.accountItem.ShikimoriAccountItem
import com.san.kir.features.shikimori.ui.accountRate.AccountRateScreen
import com.san.kir.features.shikimori.ui.accountScreen.ShikimoriScreen
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
    var nav: ShikiNavTarget by remember { mutableStateOf(ShikiNavTarget.AccountRate(58999)) }
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
                    viewModel = hiltViewModel(),
                )
            }
            ShikiNavTarget.LocalItems -> {
                LocalItemsScreen(
                    navigateUp = { nav = ShikiNavTarget.Catalog },
                    navigateToItem = {}
                )
            }
            else -> {}
        }
    }
}

sealed class ShikiNavTarget(val id: Long = 0) {
    object Start : ShikiNavTarget()
    object Catalog : ShikiNavTarget()
    class AccountRate(id: Long) : ShikiNavTarget(id)
    object Search : ShikiNavTarget()
    object LocalItems : ShikiNavTarget()
}
