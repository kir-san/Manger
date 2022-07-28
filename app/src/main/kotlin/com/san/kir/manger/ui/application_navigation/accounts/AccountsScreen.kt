package com.san.kir.manger.ui.application_navigation.accounts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.ScreenList
import com.san.kir.core.compose_utils.topBar
import com.san.kir.core.support.R
import com.san.kir.features.shikimori.ui.accountItem.ShikimoriAccountItem

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
        item {
            ShikimoriAccountItem(hiltViewModel(), navigateToShiki)
        }
    }
}
