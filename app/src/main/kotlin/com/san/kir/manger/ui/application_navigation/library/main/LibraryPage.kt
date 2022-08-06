package com.san.kir.manger.ui.application_navigation.library.main

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.san.kir.core.utils.TestTags
import com.san.kir.data.models.extend.CategoryWithMangas
import com.san.kir.manger.R

@Composable
fun LibraryPage(
    navigateToCatalogs: () -> Unit,
    navigateToInfo: (String) -> Unit,
    navigateToStorage: (String) -> Unit,
    navigateToStats: (String) -> Unit,
    navigateToChapters: (String) -> Unit,
    item: CategoryWithMangas,
    viewModel: LibraryViewModel,
) {
    Column(
        Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag(TestTags.Library.page),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (item.mangas.isEmpty()) {
                EmptyView(navigateToCatalogs)
            } else {
                PageView(
                    navigateToInfo = navigateToInfo,
                    navigateToStorage = navigateToStorage,
                    navigateToStats = navigateToStats,
                    navigateToChapters = navigateToChapters,
                    item = item,
                    viewModel = viewModel,
                )
            }
        }
    }
}

@Composable
private fun EmptyView(navigateToCatalogs: () -> Unit) {
    Column(
        Modifier
            .testTag(TestTags.Library.empty_view),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CustomText(R.string.library_help)

        Button(
            modifier = Modifier.padding(16.dp),
            onClick = navigateToCatalogs
        ) {
            Text(
                text = stringResource(id = R.string.library_help_go)
            )
        }

        CustomText(R.string.library_help2)
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun PageView(
    navigateToInfo: (String) -> Unit,
    navigateToStorage: (String) -> Unit,
    navigateToStats: (String) -> Unit,
    navigateToChapters: (String) -> Unit,
    item: CategoryWithMangas,
    viewModel: LibraryViewModel,
) {
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val span = if (isPortrait) item.spanPortrait else item.spanLandscape
    val isLarge = if (isPortrait) item.isLargePortrait else item.isLargeLandscape

    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden) { state ->
        if (state == ModalBottomSheetValue.Hidden) viewModel.changeSelectedManga(false)
        true
    }

    LaunchedEffect(viewModel.selectedManga) {
        if (viewModel.selectedManga.visible) sheetState.show()
        else sheetState.hide()
    }

    ModalBottomSheetLayout(
        modifier = Modifier.fillMaxSize(),
        sheetContent = {
            LibraryDropUpMenu(
                navigateToInfo = navigateToInfo,
                navigateToStorage = navigateToStorage,
                navigateToStats = navigateToStats,
                viewModel = viewModel,
            )
        },
        sheetState = sheetState
    ) {
        if (isLarge)
            LazyVerticalGrid(
                columns = GridCells.Fixed(span),
                modifier = Modifier.fillMaxSize(),
                contentPadding = WindowInsets
                    .systemBars
                    .only(WindowInsetsSides.Bottom)
                    .asPaddingValues(),
            ) {
                items(item.mangas) { manga ->
                    LibraryLargeItemView(navigateToChapters, manga, item.name, viewModel)
                }
            }
        else
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = WindowInsets
                    .systemBars
                    .only(WindowInsetsSides.Bottom)
                    .asPaddingValues(),
            ) {
                items(items = item.mangas) { manga ->
                    LibrarySmallItemView(navigateToChapters, manga, item.name, viewModel)
                }
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
