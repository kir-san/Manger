package com.san.kir.storage.ui.storages

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose.CircleLogo
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.RemoveItemMenuOnHold
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.animation.VectorConverter
import com.san.kir.core.compose.topBar
import com.san.kir.core.utils.format
import com.san.kir.core.utils.formatDouble
import com.san.kir.data.models.base.Storage
import com.san.kir.data.models.extend.MangaLogo
import com.san.kir.storage.R
import com.san.kir.storage.utils.StorageProgressBar
import kotlin.math.roundToInt

@Composable
fun StoragesScreen(
    navigateUp: () -> Boolean,
    navigateToItem: (Long) -> Unit,
) {
    val viewModel: StoragesViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    val size = remember { Animatable(0.0, Double.VectorConverter) }
    val count = remember { Animatable(0, Int.VectorConverter) }

    LaunchedEffect(state.size) { size.animateTo(state.size, TweenSpec(4000, 0, LinearEasing)) }
    LaunchedEffect(state.count) { count.animateTo(state.count, TweenSpec(4000, 100, LinearEasing)) }

    ScreenList(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = stringResource(R.string.main_menu_storage) + " " +
                    if (state.size > 0) {
                        stringResource(R.string.main_menu_storage_size_mb, size.value.format())
                    } else "",
            subtitle = LocalContext.current.resources.getQuantityString(
                R.plurals.storage_subtitle, count.value, count.value,
            ),
            hasAction = state.background is BackgroundState.Load
        ),
        additionalPadding = Dimensions.quarter,
        enableCollapsingBars = true,
    ) {
        items(state.items.size, key = { i -> state.items[i].id }) { index ->
            val item = state.items[index]
            val manga = state.mangas.getOrNull(index)

            ItemView(
                navigateToItem = navigateToItem,
                item = item,
                manga = manga,
                storageSize = state.size,
                sendEvent = viewModel::sendEvent
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.ItemView(
    navigateToItem: (Long) -> Unit,
    item: Storage,
    manga: MangaLogo?,
    storageSize: Double,
    sendEvent: (StoragesEvent) -> Unit,
) {
    RemoveItemMenuOnHold(
        removeText = stringResource(R.string.storage_item_menu_full_delete),
        cancelText = stringResource(R.string.storage_item_menu_cancel),
        onSuccess = { sendEvent(StoragesEvent.Delete(item)) },
        modifier = Modifier
            .fillMaxWidth()
            .animateItemPlacement()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onClick { manga?.let { navigateToItem(it.id) } ?: run { showMenu() } }
                .padding(vertical = Dimensions.quarter, horizontal = Dimensions.default)
        ) {
            // Иконка манги, если для этой папки она еще есть
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(Dimensions.Image.bigger)
            ) {
                if (manga != null) {
                    CircleLogo(logoUrl = manga.logo)
                } else {
                    NothingText()
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f, true)
                    .align(Alignment.CenterVertically)
                    .padding(Dimensions.quarter),
                verticalArrangement = Arrangement.Center
            ) {
                // Название папки с мангой
                Text(text = item.name, maxLines = 1)

                // Текстовая Информация о занимаемом месте
                UsedText(item.sizeFull, storageSize)

                // Прогрессбар занимаемого места
                StorageProgressBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimensions.smallest)
                        .height(Dimensions.half),
                    max = storageSize,
                    full = item.sizeFull,
                    read = item.sizeRead,
                )
            }
        }
    }
}

@Composable
private fun NothingText() {
    Text(
        text = stringResource(R.string.storage_not_in_bd),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun UsedText(sizeFull: Double, storageSize: Double) {
    Text(
        stringResource(
            R.string.storage_manga_item_size_text,
            formatDouble(sizeFull),
            if (storageSize != 0.0) (sizeFull / storageSize * 100).roundToInt() else 0
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.smallest),
        style = MaterialTheme.typography.subtitle1
    )
}
