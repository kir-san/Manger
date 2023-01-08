package com.san.kir.manger.ui.accounts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.topBar
import com.san.kir.core.support.R
import com.san.kir.features.catalogs.allhen.ui.accountItem.AccountItem as AllHenItem
import com.san.kir.features.shikimori.ui.accountItem.AccountItem as ShikiItem

@Composable
fun AccountsScreen(
    navigateUp: () -> Boolean,
    navigateToShiki: () -> Unit,
    navigateToAllHen: () -> Unit,
) {
    ScreenList(
        topBar = topBar(
            title = stringResource(R.string.accounts),
            navigationButton = NavigationButton.Back(navigateUp)
        ),
        additionalPadding = Dimensions.zero
    ) {
        item(key = "Shiki") { ShikiItem(navigateToShiki) }
        item(key = "allhen") { AllHenItem(navigateToAllHen) }
    }
}
