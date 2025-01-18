package com.san.kir.categories.ui.categories

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.san.kir.categories.R
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.FabButton
import com.san.kir.core.compose.FabButtonHeight
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenList
import com.san.kir.core.compose.animation.SharedParams
import com.san.kir.core.compose.animation.rememberSharedParams
import com.san.kir.core.compose.animation.saveParams
import com.san.kir.core.compose.bottomInsetsPadding
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.compose.topBar
import com.san.kir.core.utils.flow.collectAsStateWithLifecycle
import com.san.kir.core.utils.viewModel.rememberSendAction
import com.san.kir.core.utils.viewModel.stateHolder
import com.san.kir.data.models.main.Category


private val DefaultRoundedShape: RoundedCornerShape = RoundedCornerShape(50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoriesScreen(
    navigateUp: () -> Unit,
    navigateToItem: (String, SharedParams) -> Unit,
) {

    val stateHolder: CategoriesStateHolder = stateHolder { CategoriesViewModel() }
    val state by stateHolder.state.collectAsStateWithLifecycle()
    val sendAction = stateHolder.rememberSendAction()

    ScreenList(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = stringResource(R.string.categories),
        ),
        additionalPadding = Dimensions.zero,
        contentPadding = bottomInsetsPadding(bottom = FabButtonHeight),
        bottomContent = { FabButton { navigateToItem("", it) } }
    ) {
        itemsIndexed(items = state.items, key = { _, c -> c.id }) { index, item ->
            CategoryItemView(
                index = index,
                max = state.items.count(),
                category = item,
                sendAction = sendAction,
                onClick = { navigateToItem(item.name, it) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.CategoryItemView(
    index: Int,
    max: Int,
    category: Category,
    sendAction: (CategoriesAction) -> Unit,
    onClick: (SharedParams) -> Unit,
) {
    var visibleState by remember { mutableStateOf(category.isVisible) }
    val params = rememberSharedParams()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick(params) })
            .padding(vertical = Dimensions.quarter, horizontal = Dimensions.default)
            .horizontalInsetsPadding()
            .animateItem(fadeInSpec = null, fadeOutSpec = null)
            .saveParams(params),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Название категории
        Text(
            category.name,
            modifier = Modifier.weight(1f, true)
        )

        // Переключение видимости категории в библиотеке
        IconButton(onClick = {
            visibleState = visibleState.not()
            sendAction(CategoriesAction.ChangeVisibility(category, visibleState))
        }) {
            AnimatedContent(targetState = visibleState, label = "") {
                when (it) {
                    true -> Icon(Icons.Default.Visibility, "")
                    false -> Icon(Icons.Default.VisibilityOff, "")
                }
            }
        }

        Row(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceVariant,
                DefaultRoundedShape
            )
        ) {

            // Кнопки изменения порядка расположения
            IconButton(
                onClick = { sendAction(CategoriesAction.Reorder(index, index - 1)) },
                enabled = index > 0
            ) {
                Icon(Icons.Default.ArrowDropUp, "")
            }

            IconButton(
                onClick = { sendAction(CategoriesAction.Reorder(index, index + 1)) },
                enabled = index < max - 1
            ) {
                Icon(Icons.Default.ArrowDropDown, "")
            }
        }
    }
}
