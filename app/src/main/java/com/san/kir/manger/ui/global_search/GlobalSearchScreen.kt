package com.san.kir.manger.ui.global_search

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.san.kir.manger.R
import com.san.kir.manger.ui.utils.ListItem
import com.san.kir.manger.ui.utils.MenuIcon
import com.san.kir.manger.ui.utils.navigationBarsWithImePadding
import com.san.kir.manger.ui.utils.statusBarsPadding

@ExperimentalAnimationApi
@Composable
fun GlobalSearchScreen(nav: NavHostController) {
//    doFromSdk(Build.VERSION_CODES.LOLLIPOP) {
//        window.statusBarColor = ContextCompat.getColor(this, R.color.transparent_dark)
//        window.navigationBarColor = ContextCompat.getColor(this, R.color.transparent_dark2)
//    }


    val viewModel: GlobalSearchViewModel = viewModel()
    val action by viewModel.action.collectAsState()
    val viewState by viewModel.state.collectAsState()

    Scaffold(
        modifier = Modifier.navigationBarsWithImePadding(),
        topBar = { TopBar(nav, viewState, viewModel) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (action) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            LazyColumn {
                items(items = viewState.items, key = { item -> item.id }) { item ->
                    ListItem(viewModel,item, item.name, item.catalogName, nav)
                }
            }
        }
    }
}

// Верхняя панель
@ExperimentalAnimationApi
@Composable
private fun TopBar(
    nav: NavHostController,
    viewState: GlobalSearchViewModel.GlobalSearchViewState,
    viewModel: GlobalSearchViewModel
) {
    var search by rememberSaveable { mutableStateOf(false) }
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
                    nav.popBackStack()
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
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
                .padding(0.dp)
        )
    }
}




