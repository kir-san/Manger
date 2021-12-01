package com.san.kir.manger.utils.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Update
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.manger.data.room.entities.SiteCatalogElement
import com.san.kir.manger.ui.SuppotMangaViewModel
import com.san.kir.manger.ui.application_navigation.catalog.catalog.btnSizeAddUpdate

@Composable
fun ListItem(
    item: SiteCatalogElement,
    firstName: String,
    secondName: String,
    viewModel: SuppotMangaViewModel = hiltViewModel(),
    navAddAction: () -> Unit,
    navInfoAction: () -> Unit,
) {
    var isAdded by remember { mutableStateOf(ItemState.ADDED) }

    LaunchedEffect(item) {
        if (viewModel.isContainManga(item)) isAdded = ItemState.UPDATE
    }

    Row(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp)
            .clickable { navInfoAction() }
    ) {
        Column(
            Modifier
                .padding(end = 16.dp)
                .weight(1f, true)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = firstName,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1
            )
            Text(
                text = secondName,
                style = MaterialTheme.typography.subtitle2
            )
        }

        when (isAdded) {
            ItemState.ADDED ->
                Image(
                    imageVector = Icons.Default.Add, "",
                    colorFilter = ColorFilter.tint(Color.Green),
                    modifier = Modifier
                        .size(btnSizeAddUpdate)
                        .align(Alignment.CenterVertically)
                        .clickable { navAddAction() }
                )
            ItemState.UPDATE ->
                Image(
                    imageVector = Icons.Default.Update, "",
                    colorFilter = ColorFilter.tint(MaterialTheme.colors.onBackground),
                    modifier = Modifier
                        .size(btnSizeAddUpdate)
                        .align(Alignment.CenterVertically)
                        .clickable(onClick = {
                            isAdded = ItemState.NONE
                            viewModel.onlineUpdate(item)
                        })
                )
            ItemState.NONE -> {
            }
        }
    }
}

private enum class ItemState {
    ADDED, UPDATE, NONE
}
