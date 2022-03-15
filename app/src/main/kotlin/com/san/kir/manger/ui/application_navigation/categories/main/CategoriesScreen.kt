package com.san.kir.manger.ui.application_navigation.categories.main

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
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.ScreenList
import com.san.kir.core.compose_utils.systemBarsHorizontalPadding
import com.san.kir.core.compose_utils.topBar
import com.san.kir.data.models.base.Category
import com.san.kir.manger.R

@Composable
fun CategoriesScreen(
    navigateUp: () -> Unit,
    navigateToItem: (String) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val cats by viewModel.categories.collectAsState(emptyList())

    ScreenList(
        topBar = topBar(
            navigationListener = navigateUp,
            title = stringResource(R.string.main_menu_category),
            actions = {
                MenuIcon(
                    icon = Icons.Default.Add,
                    onClick = { navigateToItem("") })
            },
        ),
        additionalPadding = Dimensions.smaller
    ) {
        itemsIndexed(items = cats, key = { _, c -> c.id }) { index, item ->
            CategoryItemView(index, cats.count(), item) {
                navigateToItem(item.name)
            }
        }
    }
}

@Composable
fun CategoryItemView(index: Int, max: Int, category: Category, onClick: () -> Unit) {
    val viewModel: CategoriesViewModel = hiltViewModel()
    var visibleState by remember { mutableStateOf(category.isVisible) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dimensions.smaller, horizontal = Dimensions.default)
            .padding(systemBarsHorizontalPadding()),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Название категории
        Text(
            category.name,
            modifier = Modifier.weight(1f, true)
        )

        Row(
            horizontalArrangement = Arrangement.End,
        ) {

            // Переключение видимости категории в библиотеке
            IconButton(onClick = {
                visibleState = visibleState.not()
                category.isVisible = category.isVisible.not()
                viewModel.update(category)
            }) {
                if (visibleState)
                    Icon(Icons.Default.Visibility, "")
                else
                    Icon(Icons.Default.VisibilityOff, "")
            }

            // Кнопки изменения порядка расположения
            IconButton(
                onClick = { viewModel.swapMenuItems(index, index - 1) },
                enabled = index > 0
            ) {
                Icon(Icons.Default.ArrowDropUp, "")
            }

            IconButton(
                onClick = { viewModel.swapMenuItems(index, index + 1) },
                enabled = index < max - 1
            ) {
                Icon(Icons.Default.ArrowDropDown, "")
            }
        }
    }
}
