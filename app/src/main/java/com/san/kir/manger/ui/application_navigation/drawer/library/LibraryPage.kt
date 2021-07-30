package com.san.kir.manger.ui.application_navigation.drawer.library

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.pager.ExperimentalPagerApi
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.CategoryWithMangas
import com.san.kir.manger.ui.application_navigation.drawer.CatalogsNavigationDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalComposeUiApi
@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@Composable
fun LibraryPage(
    index: Int,
    state: LibraryViewState,
    nav: NavController,
    mainNav: NavHostController,
) {
    val item = state.categories[index]

    Column(
        Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (item.mangas.isEmpty()) {
                EmptyView(nav)
            } else {
                PageView(item, state, mainNav)
            }
        }
    }
}

@ExperimentalPagerApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@Composable
private fun EmptyView(nav: NavController) {
    Column(
        Modifier.padding(
            rememberInsetsPaddingValues(
                insets = LocalWindowInsets.current.systemBars,
                applyBottom = false, applyTop = false
            )
        ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CustomText(R.string.library_help)

        Button(
            modifier = Modifier.padding(16.dp),
            onClick = { nav.navigate(CatalogsNavigationDestination.route) }) {
            Text(
                text = stringResource(id = R.string.library_help_go)
            )
        }

        CustomText(R.string.library_help2)
    }
}

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
private fun PageView(
    item: CategoryWithMangas,
    state: LibraryViewState,
    mainNav: NavHostController
) {
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val span = if (isPortrait) item.category.spanPortrait else item.category.spanLandscape
    val isLarge = if (isPortrait) item.category.isLargePortrait else item.category.isLargeLandscape

    if (isLarge)
        LazyVerticalGrid(
            cells = GridCells.Fixed(span),
            modifier = Modifier.fillMaxSize(),
            contentPadding = rememberInsetsPaddingValues(
                insets = LocalWindowInsets.current.systemBars, applyTop = false
            ),
        ) {
            items(items = item.mangas) { manga ->
                LibraryLargeItemView(manga, item.category.name, mainNav, state)
            }
        }
    else
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = rememberInsetsPaddingValues(
                insets = LocalWindowInsets.current.systemBars, applyTop = false
            ),
        ) {
            items(items = item.mangas) { manga ->
                LibrarySmallItemView(manga, item.category.name, mainNav, state)
            }
        }
}

@Composable
private fun CustomText(@StringRes stringRes: Int) {
    Text(
        text = stringResource(id = stringRes),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.onBackground,
        style = MaterialTheme.typography.body1
    )
}
