package com.san.kir.manger.ui.application_navigation.catalog.global_search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.san.kir.manger.R
import com.san.kir.manger.ui.application_navigation.catalog.CatalogsNavTarget
import com.san.kir.manger.ui.utils.ListItem
import com.san.kir.manger.ui.utils.MenuIcon
import com.san.kir.manger.ui.utils.navigate

// TODO добавить меню для исключения ненужных каталогов
@Composable
fun GlobalSearchScreen(
    nav: NavHostController,
    viewModel: GlobalSearchViewModel = hiltViewModel()
) {
    val action by viewModel.action.collectAsState()
    val viewState by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopBar(nav, viewState, viewModel) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding())
        ) {
            if (action || viewState.items.isEmpty()) LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        rememberInsetsPaddingValues(
                            insets = LocalWindowInsets.current.systemBars,
                            applyTop = false, applyBottom = false,
                        )
                    )
            )
            LazyColumn(
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.systemBars,
                    applyTop = false,
                )
            ) {
                items(items = viewState.items, key = { item -> item.id }) { item ->
                    ListItem(item, item.name, item.catalogName,
                             navAddAction = {
                                 nav.navigate(CatalogsNavTarget.AddLocal, item.link)
                             },
                             navInfoAction = {
                                 nav.navigate(CatalogsNavTarget.Info, item.link)
                             })
                }
            }
        }
    }
}

// Верхняя панель
@Composable
private fun TopBar(
    nav: NavHostController,
    viewState: GlobalSearchViewState,
    viewModel: GlobalSearchViewModel,
) {
    var searchText by rememberSaveable { mutableStateOf("") }

    viewModel.setSearchText(searchText)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
            .statusBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text(text = "${stringResource(id = R.string.main_menu_search)}: ${viewState.items.size}")
            },
            navigationIcon = {
                MenuIcon(icon = Icons.Default.ArrowBack) {
                    nav.navigateUp()
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),

            contentPadding = rememberInsetsPaddingValues(
                insets = LocalWindowInsets.current.systemBars,
                applyBottom = false, applyTop = false
            )
        )

        TextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            leadingIcon = { Icon(Icons.Default.Search, "search") },
            trailingIcon = {
                MenuIcon(icon = Icons.Default.Close) {
                    searchText = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    rememberInsetsPaddingValues(
                        insets = LocalWindowInsets.current.systemBars,
                        applyBottom = false, applyTop = false
                    )
                )
        )
    }
}




