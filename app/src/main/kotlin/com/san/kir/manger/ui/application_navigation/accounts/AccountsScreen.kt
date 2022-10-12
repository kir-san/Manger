package com.san.kir.manger.ui.application_navigation.accounts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.topBar
import com.san.kir.core.support.R
import com.san.kir.features.shikimori.ui.accountItem.AccountItem

@Composable
fun AccountsScreen(
    navigateUp: () -> Unit,
    navigateToShiki: () -> Unit,
) {
    ScreenList(
        topBar = topBar(
            navigationListener = navigateUp,
            title = stringResource(R.string.main_menu_accounts),
        ),
        additionalPadding = Dimensions.zero
    ) {
        item(key = "Shiki") {
            AccountItem(navigateToShiki)
        }

    }
}
