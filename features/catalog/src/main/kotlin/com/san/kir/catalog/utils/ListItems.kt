package com.san.kir.catalog.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Update
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.systemBarsHorizontalPadding
import com.san.kir.data.models.extend.MiniCatalogItem

@Composable
fun ListItem(
    item: MiniCatalogItem,
    secondName: String,
    toAdd: (link: String) -> Unit,
    toInfo: (link: String) -> Unit,
    updateItem: (MiniCatalogItem) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { toInfo(item.link) }
            .padding(vertical = Dimensions.quarter, horizontal = Dimensions.default)
            .padding(systemBarsHorizontalPadding())
    ) {
        Column(
            Modifier
                .padding(end = Dimensions.default)
                .weight(1f, true)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = secondName,
                style = MaterialTheme.typography.subtitle2
            )
        }

        when (item.state) {
            MiniCatalogItem.State.Added ->
                Image(
                    imageVector = Icons.Default.Add, "",
                    colorFilter = ColorFilter.tint(Color.Green),
                    modifier = Modifier
                        .size(Dimensions.Image.small)
                        .align(Alignment.CenterVertically)
                        .clickable { toAdd(item.link) }
                )

            MiniCatalogItem.State.Update ->
                Image(
                    imageVector = Icons.Default.Update, "",
                    colorFilter = ColorFilter.tint(MaterialTheme.colors.onBackground),
                    modifier = Modifier
                        .size(Dimensions.Image.small)
                        .align(Alignment.CenterVertically)
                        .clickable(onClick = { updateItem(item) })
                )

            MiniCatalogItem.State.None -> {
            }
        }
    }
}
