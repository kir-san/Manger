package com.san.kir.features.shikimori.ui.util

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.Fonts
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.compose.rememberImage
import com.san.kir.data.models.base.ShikimoriStatus
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.logic.useCases.CanBind

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LazyItemScope.MangaItemContent(
    avatar: String,
    mangaName: String,
    canBind: CanBind,
    readingChapters: Long = 0,
    allChapters: Long = 0,
    secondaryText: String? = null,
    currentStatus: ShikimoriStatus? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .animateItemPlacement()
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(vertical = Dimensions.half, horizontal = Dimensions.default)
            .horizontalInsetsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            rememberImage(avatar),
            contentDescription = "manga avatar",
            modifier = Modifier
                .size(Dimensions.Image.default)
                .padding(end = Dimensions.half),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier.weight(1f, true)
        ) {
            Text(
                mangaName,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                secondaryText ?: stringResource(R.string.reading, readingChapters, allChapters),
                fontSize = Fonts.Size.less,
            )
            currentStatus?.let { StatusText(it) }
        }

        Box(modifier = Modifier.size(Dimensions.Image.small), contentAlignment = Alignment.Center) {
            when (canBind) {
                CanBind.Already -> Icon(
                    Icons.Default.SyncAlt,
                    contentDescription = "has synchronized item"
                )
                CanBind.Ok -> Icon(
                    Icons.Default.NotificationImportant,
                    contentDescription = "has synchronized item"
                )
                CanBind.No -> {}
                CanBind.Check -> Icon(
                    Icons.Default.HelpOutline,
                    contentDescription = "has synchronized item"
                )
            }
        }
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
                canBind = CanBind.Already,
                currentStatus = ShikimoriStatus.Planned
            ) {}
        }
    }
}
