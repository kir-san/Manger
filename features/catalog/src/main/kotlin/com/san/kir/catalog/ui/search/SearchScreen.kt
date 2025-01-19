package com.san.kir.catalog.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Filter1
import androidx.compose.material.icons.filled.Filter2
import androidx.compose.material.icons.filled.Filter3
import androidx.compose.material.icons.filled.Filter4
import androidx.compose.material.icons.filled.Filter5
import androidx.compose.material.icons.filled.Filter6
import androidx.compose.material.icons.filled.Filter7
import androidx.compose.material.icons.filled.Filter8
import androidx.compose.material.icons.filled.Filter9
import androidx.compose.material.icons.filled.Filter9Plus
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.san.kir.catalog.R
import com.san.kir.catalog.utils.ListItem
import com.san.kir.core.compose.CheckBoxText
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.TopSheets
import com.san.kir.core.compose.animation.SharedParams
import com.san.kir.core.compose.animation.animateToDelayed
import com.san.kir.core.compose.animation.rememberIntAnimatable
import com.san.kir.core.compose.endInsetsPadding
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.compose.topBar
import com.san.kir.core.compose.topInsetsPadding
import com.san.kir.core.utils.flow.collectAsStateWithLifecycle
import com.san.kir.core.utils.navigation.DialogState
import com.san.kir.core.utils.navigation.EmptyDialogData
import com.san.kir.core.utils.navigation.rememberDialogState
import com.san.kir.core.utils.navigation.rememberLambda
import com.san.kir.core.utils.navigation.show
import com.san.kir.core.utils.viewModel.Action
import com.san.kir.core.utils.viewModel.rememberSendAction
import com.san.kir.core.utils.viewModel.stateHolder
import com.san.kir.data.models.catalog.SiteCatalogElement
import com.san.kir.data.models.catalog.toFullItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreen(
    navigateUp: () -> Unit,
    navigateToInfo: (SiteCatalogElement, SharedParams) -> Unit,
    navigateToAdd: (String, SharedParams) -> Unit,
    searchText: String,
) {
    val holder: SearchStateHolder = stateHolder { SearchViewModel() }
    val state by holder.state.collectAsStateWithLifecycle()
    val items by holder.items.collectAsStateWithLifecycle()
    val sendAction = holder.rememberSendAction()

    val size = rememberIntAnimatable()
    LaunchedEffect(items) { size.animateToDelayed(items.size, duration = 500) }

    val catalogSelectorDialog = rememberDialogState<EmptyDialogData>()

    val query = remember { { arg: String -> holder.sendAction(SearchAction.Search(arg)) } }

    ScreenList(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = "${stringResource(R.string.search)}: ${size.value}",
            initSearchText = searchText,
            onSearchTextChange = query,
            hasAction = state.background,
            actions = {
                MenuIcon(
                    icon = state.selectedCatalogs.filterIcon(),
                    hasNotify = state.hasFilterChanges,
                ) { catalogSelectorDialog.show() }
            }
        ),
        additionalPadding = Dimensions.quarter,
    ) {
        items(items, key = { it.id }) { item ->
            ListItem(
                item, item.catalogName,
                toAdd = { params -> navigateToAdd(item.link, params) },
                toInfo = { params -> navigateToInfo(item.toFullItem(), params) },
                updateItem = rememberLambda { manga -> sendAction(SearchAction.UpdateManga(manga)) }
            )
        }
    }

    CatalogSelector(
        catalogSelectorDialog,
        state.catalog,
        state.hasFilterChanges,
        state.addedMangaVisible,
        sendAction
    )
}

@Composable
private fun CatalogSelector(
    dialogState: DialogState<EmptyDialogData>,
    list: List<SelectableCatalog>,
    hasFilterChanges: Boolean,
    showAddedManga: Boolean,
    sendAction: (Action) -> Unit,
) {
    TopSheets(dialogState = dialogState) {

        Column(
            modifier = Modifier.topInsetsPadding(
                horizontal = Dimensions.half,
                vertical = Dimensions.default
            )
        ) {
            LazyColumn(modifier = Modifier.weight(1f, false)) {
                items(list.size, key = { it }) { index ->
                    val catalog = list[index]

                    CheckBoxText(
                        state = catalog.selected,
                        onChange = { sendAction(SearchAction.ChangeCatalogSelect(catalog.name)) },
                        firstText = catalog.title,
                        modifier = Modifier.horizontalInsetsPadding()
                    )
                }

                item {
                    CheckBoxText(
                        state = showAddedManga,
                        onChange = { sendAction(SearchAction.ChangeAddMangaVisible(it)) },
                        firstText = if (showAddedManga) "Показывать добавленную мангу" else "Непоказывать добавленную мангу",
                        modifier = Modifier.horizontalInsetsPadding(),
                    )
                }
            }

            TextButton(
                onClick = {
                    sendAction(SearchAction.ApplyCatalogFilter)
                    dialogState.dismiss()
                },
                enabled = hasFilterChanges,
                modifier = Modifier.align(Alignment.End).endInsetsPadding()
            ) {
                Text(stringResource(R.string.apply))
            }
        }

    }
}

private fun Int.filterIcon(): ImageVector {
    return when (this) {
        0 -> Icons.Default.FilterNone
        1 -> Icons.Default.Filter1
        2 -> Icons.Default.Filter2
        3 -> Icons.Default.Filter3
        4 -> Icons.Default.Filter4
        5 -> Icons.Default.Filter5
        6 -> Icons.Default.Filter6
        7 -> Icons.Default.Filter7
        8 -> Icons.Default.Filter8
        9 -> Icons.Default.Filter9
        else -> Icons.Default.Filter9Plus
    }
}
