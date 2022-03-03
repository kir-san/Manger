package com.san.kir.manger.ui.application_navigation.accounts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.san.kir.core.compose_utils.TopBarScreenList
import com.san.kir.core.support.R
import com.san.kir.features.shikimori.ui.syncItem.ShikimoriItem

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AccountsScreen(
    navigateUp: () -> Unit,
    navigateToShiki: () -> Unit,
) {
    TopBarScreenList(
        navigateUp = navigateUp,
        title = stringResource(R.string.main_menu_accounts),
        additionalPadding = 0.dp
    ) {
        item {
            ShikimoriItem(hiltViewModel(), navigateToShiki)
        }
    }
}
