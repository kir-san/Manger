package com.san.kir.manger.ui.drawer.categories

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.pager.ExperimentalPagerApi
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.ui.DrawerNavigationDestination
import com.san.kir.manger.ui.EditCategoryNavigationDestination
import com.san.kir.manger.ui.utils.MenuIcon
import com.san.kir.manger.ui.utils.navigate
import com.san.kir.manger.view_models.TitleViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalPagerApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun CategoriesScreen(
    mainNav: NavController,
    contentPadding: PaddingValues,
    vm: TitleViewModel = hiltViewModel(mainNav.getBackStackEntry(DrawerNavigationDestination.route)),
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    vm.setTitle(stringResource(id = R.string.main_menu_category))

    val cats by viewModel.categories.collectAsState(emptyList())

    LazyColumn(
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.systemBars,
            applyTop = false,
        ),
        modifier = Modifier.padding(top = contentPadding.calculateTopPadding())
    ) {
        itemsIndexed(items = cats, key = { _, c -> c.id }) { index, item ->
            CategoryItemView(index, cats.count(), item) {
                mainNav.navigate(EditCategoryNavigationDestination, item)
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

@ExperimentalAnimationApi
@Composable
fun CategoriesActions(mainNav: NavHostController) {
    MenuIcon(
        icon = Icons.Default.Add,
        onClick = { mainNav.navigate(EditCategoryNavigationDestination, Category()) })
}

