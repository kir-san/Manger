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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.ui.application_navigation.categories.CategoriesNavTarget
import com.san.kir.manger.ui.utils.MenuIcon
import com.san.kir.manger.ui.utils.TopBarScreenList
import com.san.kir.manger.ui.utils.navigate

@Composable
fun CategoriesScreen(
    nav: NavHostController,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val cats by viewModel.categories.collectAsState(emptyList())

    TopBarScreenList(
        navHostController = nav,
        title = stringResource(R.string.main_menu_category),
        actions = {
            MenuIcon(
                icon = Icons.Default.Add,
                onClick = { nav.navigate(CategoriesNavTarget.Category, Category()) })

        }
    ) {
        itemsIndexed(items = cats, key = { _, c -> c.id }) { index, item ->
            CategoryItemView(index, cats.count(), item) {
                nav.navigate(CategoriesNavTarget.Category, item)
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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Название категории
        Text(
            category.name, modifier = Modifier
                .weight(1f, true)
                .clickable(onClick = onClick)
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
