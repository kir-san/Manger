package com.san.kir.library.ui.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.startInsetsPadding
import com.san.kir.core.compose.systemBarBottomPadding
import com.san.kir.core.support.MainMenuType
import com.san.kir.core.utils.TestTags
import com.san.kir.library.R

// Боковое меню с выбором пунктов для навигации по приложению
@Composable
internal fun DrawerScreen(navigateToScreen: (MainMenuType) -> Unit) {

    val viewModel: DrawerViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current
    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: ""
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxHeight()
    ) {
        Row(
            modifier = Modifier
                .padding(Dimensions.small)
                .startInsetsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painterResource(R.mipmap.ic_launcher_foreground), "")

            Column {
                Text(stringResource(R.string.app_name_version, version))
                Text(stringResource(R.string.name))
            }
        }

        when (val current = state.menu) {
            MainMenuItemsState.Load ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            is MainMenuItemsState.Ok ->
                // Навигация по пунктам приложения
                LazyColumn(contentPadding = systemBarBottomPadding()) {
                    itemsIndexed(items = current.items) { index, item ->
                        MainMenuItemRows(
                            index = index,
                            max = current.items.size,
                            item = item,
                            editMode = state.hasEditMenu,
                            sendEvent = viewModel::sendEvent
                        ) {
                            navigateToScreen(item.item.type)
                        }
                    }
                }
        }

    }
}

// Шаблон пункта меню
@Composable
private fun MainMenuItemRows(
    index: Int,
    max: Int,
    item: MenuItem,
    editMode: Boolean,
    sendEvent: (DrawerEvent) -> Unit,
    action: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = action)
            .testTag(TestTags.Drawer.item)
    ) {
        Icon(
            item.item.type.icon,
            "",
            modifier = Modifier
                .padding(Dimensions.default)
                .startInsetsPadding()
        )

        Text(text = item.item.name, style = MaterialTheme.typography.h6)

        if (!editMode)
            Text(
                text = item.status,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.default)
            )
        else
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { sendEvent(DrawerEvent.Reorder(index, index - 1)) },
                    enabled = index > 0
                ) {
                    Icon(Icons.Default.ArrowDropUp, "")
                }

                IconButton(
                    onClick = { sendEvent(DrawerEvent.Reorder(index, index + 1)) },
                    enabled = index < max - 1
                ) {
                    Icon(Icons.Default.ArrowDropDown, "")
                }
            }
    }
}
