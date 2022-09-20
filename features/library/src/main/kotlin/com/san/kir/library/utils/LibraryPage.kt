package com.san.kir.library.utils

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.endInsetsPadding
import com.san.kir.core.compose_utils.systemBarBottomPadding
import com.san.kir.core.utils.TestTags
import com.san.kir.data.models.extend.CategoryWithMangas
import com.san.kir.library.R
import com.san.kir.library.ui.library.LibraryEvent
import com.san.kir.library.ui.library.LibraryNavigation

@Composable
internal fun LibraryPage(
    navigation: LibraryNavigation,
    item: CategoryWithMangas,
    showCategory: Boolean,
    sendEvent: (LibraryEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTags.Library.page),
        contentAlignment = Alignment.Center
    ) {
        if (item.mangas.isEmpty()) {
            EmptyView(navigation.navigateToCatalogs)
        } else {
            PageView(
                navigateToChapters = navigation.navigateToChapters,
                item = item,
                showCategory = showCategory,
                sendEvent = sendEvent
            )
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
            modifier = Modifier.padding(Dimensions.default),
            onClick = navigateToCatalogs
        ) {
            Text(stringResource(R.string.library_help_go))
        }

        CustomText(R.string.library_help2)
    }
}

@Composable
private fun PageView(
    navigateToChapters: (String) -> Unit,
    item: CategoryWithMangas,
    showCategory: Boolean,
    sendEvent: (LibraryEvent) -> Unit,
) {
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val span = if (isPortrait) item.spanPortrait else item.spanLandscape
    val isLarge = if (isPortrait) item.isLargePortrait else item.isLargeLandscape


    if (isLarge)
        LazyVerticalGrid(
            columns = GridCells.Fixed(span),
            modifier = Modifier
                .fillMaxSize()
                .endInsetsPadding(),
            contentPadding = systemBarBottomPadding(),
        ) {
            items(item.mangas, key = { it.id }) { manga ->
                LibraryLargeItem(
                    onClick = navigateToChapters,
                    onLongClick = { sendEvent(LibraryEvent.SelectManga(it)) },
                    manga = manga,
                    cat = item.name,
                    showCategory = showCategory,
                )
            }
        }
    else
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = systemBarBottomPadding(),
        ) {
            items(items = item.mangas, key = { it.id }) { manga ->
                LibrarySmallItem(
                    onClick = navigateToChapters,
                    onLongClick = { sendEvent(LibraryEvent.SelectManga(it)) },
                    manga = manga,
                    cat = item.name,
                    showCategory = showCategory,
                )
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
