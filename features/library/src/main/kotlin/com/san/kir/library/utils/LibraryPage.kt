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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.bottomInsetsPadding
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.utils.TestTags
import com.san.kir.core.utils.viewModel.Action
import com.san.kir.core.utils.viewModel.returned
import com.san.kir.data.models.main.CategoryWithMangas
import com.san.kir.data.models.utils.MainMenuType
import com.san.kir.library.R
import com.san.kir.library.ui.library.LibraryEvent

private val ContentPadding = Dimensions.quarter

@Composable
internal fun LibraryPage(
    item: CategoryWithMangas,
    showCategory: Boolean,
    sendAction: (Action) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTags.Library.page),
        contentAlignment = Alignment.Center
    ) {
        if (item.mangas.isEmpty()) {
            EmptyView { sendAction(LibraryEvent.ToScreen(MainMenuType.Catalogs).returned()) }
        } else {
            PageView(item, showCategory, sendAction)
        }
    }
}

@Composable
private fun EmptyView(navigateToCatalogs: () -> Unit) {
    Column(
        modifier = Modifier.testTag(TestTags.Library.empty_view),
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
    item: CategoryWithMangas,
    showCategory: Boolean,
    sendAction: (Action) -> Unit,
) {
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val span = if (isPortrait) item.spanPortrait else item.spanLandscape

    if (span > 1) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(span),
            modifier = Modifier
                .fillMaxSize()
                .horizontalInsetsPadding(),
            contentPadding = bottomInsetsPadding(ContentPadding),
        ) {
            items(item.mangas, key = { it.id }) { manga ->
                LibraryLargeItem(
                    onClick = { id, params -> sendAction(LibraryEvent.ToChapters(id, params).returned()) },
                    onLongClick = { sendAction(LibraryEvent.ShowSelectedMangaDialog(it).returned()) },
                    manga = manga,
                    cat = item.name,
                    showCategory = showCategory,
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = bottomInsetsPadding(ContentPadding),
        ) {
            items(items = item.mangas, key = { it.id }) { manga ->
                LibrarySmallItem(
                    onClick = { id, params -> sendAction(LibraryEvent.ToChapters(id, params).returned()) },
                    onLongClick = { sendAction(LibraryEvent.ShowSelectedMangaDialog(it).returned()) },
                    manga = manga,
                    cat = item.name,
                    showCategory = showCategory,
                )
            }
        }
    }
}


@Composable
private fun CustomText(@StringRes stringRes: Int) {
    Text(
        text = stringResource(stringRes),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodyLarge
    )
}
