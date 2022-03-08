package com.san.kir.manger.ui.application_navigation.catalog.global_search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.systemBarsPadding
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.PreparedTopBar
import com.san.kir.core.compose_utils.SearchTextField
import com.san.kir.core.compose_utils.TopBarScreenList
import com.san.kir.manger.R
import com.san.kir.manger.utils.compose.ListItem

// TODO добавить меню для исключения ненужных каталогов
@Composable
fun GlobalSearchScreen(
    navigateUp: () -> Unit,
    navigateToInfo: (String) -> Unit,
    navigateToAdd: (String) -> Unit,
    viewModel: GlobalSearchViewModel = hiltViewModel(),
    initSearchText: String,
) {
    val action by viewModel.action.collectAsState()
    val viewState by viewModel.state.collectAsState()

    TopBarScreenList(
        topBar = { height ->
            Column(modifier = Modifier.fillMaxWidth()) {
                PreparedTopBar(
                    navigateUp,
                    title = "${stringResource(R.string.main_menu_search)}: ${viewState.items.size}",
                    height = height,
                )

                SearchTextField(
                    inititalValue = initSearchText,
                    onChangeValue = viewModel::setSearchText,
                )

                if (action || viewState.items.isEmpty()) LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .systemBarsPadding(top = false, bottom = false)
                )
            }
        },
        additionalPadding = Dimensions.smaller,
        enableCollapsingBars = true
    ) {
        items(items = viewState.items) { item ->
            ListItem(
                item, item.name, item.catalogName,
                navAddAction = navigateToAdd,
                navInfoAction = navigateToInfo
            )
        }
    }
}




