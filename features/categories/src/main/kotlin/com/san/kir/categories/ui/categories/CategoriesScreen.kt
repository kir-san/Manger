package com.san.kir.categories.ui.categories

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.categories.R
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.systemBarsHorizontalPadding
import com.san.kir.core.compose.topBar
import com.san.kir.data.models.base.Category

@Composable
fun CategoriesScreen(
    navigateUp: () -> Boolean,
    navigateToItem: (String) -> Unit,
) {

    val viewModel: CategoriesViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    ScreenList(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = stringResource(R.string.categories),
            actions = {
                MenuIcon(
                    icon = Icons.Default.Add,
                    onClick = { navigateToItem("") })
            },
        ),
        additionalPadding = Dimensions.quarter
    ) {
        itemsIndexed(items = state.items, key = { _, c -> c.id }) { index, item ->
            CategoryItemView(
                index = index,
                max = state.items.count(),
                category = item,
                sendEvent = viewModel::sendEvent,
                onClick = { navigateToItem(item.name) }
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CategoryItemView(
    index: Int,
    max: Int,
    category: Category,
    sendEvent: (CategoriesEvent) -> Unit,
    onClick: () -> Unit
) {
    var visibleState by remember { mutableStateOf(category.isVisible) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dimensions.quarter, horizontal = Dimensions.default)
            .padding(systemBarsHorizontalPadding()),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Название категории
        Text(
            category.name,
            modifier = Modifier.weight(1f, true)
        )

        Row(horizontalArrangement = Arrangement.End) {

            // Переключение видимости категории в библиотеке
            IconButton(onClick = {
                visibleState = visibleState.not()
                sendEvent(CategoriesEvent.ChangeVisibility(category, visibleState))
            }) {
                AnimatedContent(targetState = visibleState) {
                    when (it) {
                        true -> Icon(Icons.Default.Visibility, "")
                        false -> Icon(Icons.Default.VisibilityOff, "")
                    }
                }
            }

            // Кнопки изменения порядка расположения
            IconButton(
                onClick = { sendEvent(CategoriesEvent.Reorder(index, index - 1)) },
                enabled = index > 0
            ) {
                Icon(Icons.Default.ArrowDropUp, "")
            }

            IconButton(
                onClick = { sendEvent(CategoriesEvent.Reorder(index, index + 1)) },
                enabled = index < max - 1
            ) {
                Icon(Icons.Default.ArrowDropDown, "")
            }
        }
    }
}
