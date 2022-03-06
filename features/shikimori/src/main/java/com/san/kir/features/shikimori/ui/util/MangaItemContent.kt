package com.san.kir.features.shikimori.ui.util

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.rememberImage
import com.san.kir.core.compose_utils.systemBarsHorizontalPadding
import com.san.kir.data.models.base.ShikimoriAccount
import com.san.kir.features.shikimori.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LazyItemScope.MangaItemContent(
    avatar: String,
    mangaName: String,
    readingChapters: Long,
    allChapters: Long,
    currentStatus: ShikimoriAccount.Status?,
    isSynced: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .animateItemPlacement()
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(vertical = Dimensions.small, horizontal = Dimensions.default)
            .padding(systemBarsHorizontalPadding()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            rememberImage(avatar),
            contentDescription = "manga avatar",
            modifier = Modifier
                .size(Dimensions.imageSize)
                .padding(end = Dimensions.small),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier.weight(1f, true)
        ) {
            Text(
                mangaName,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(stringResource(R.string.reading, readingChapters, allChapters))
            currentStatus?.let { StatusText(it) }
        }

        if (isSynced)
            Icon(
                Icons.Default.SyncAlt,
                contentDescription = "has synchronized item"
            )
    }
}

@Preview
@Composable
internal fun ListItemContentPreview() {
    LazyColumn {
        item {
            MangaItemContent(
                avatar = "",
                mangaName = "item.manga.russian",
                readingChapters = 10,
                allChapters = 99,
                isSynced = true,
                currentStatus = ShikimoriAccount.Status.Planned
            ) {}
        }
    }
}
