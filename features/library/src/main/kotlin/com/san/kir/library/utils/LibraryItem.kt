package com.san.kir.library.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.endInsetsPadding
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.compose.rememberImage
import com.san.kir.core.compose.squareMaxSize
import com.san.kir.core.support.CATEGORY_ALL
import com.san.kir.core.utils.TestTags
import com.san.kir.data.models.extend.SimplifiedManga

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyGridItemScope.LibraryLargeItem(
    onClick: (Long) -> Unit,
    onLongClick: (SimplifiedManga) -> Unit,
    manga: SimplifiedManga,
    cat: String,
    showCategory: Boolean
) {
    val context = LocalContext.current
    val defaultColor = MaterialTheme.colors.primary
    val backgroundColor = remember {
        runCatching { if (manga.color != 0) Color(manga.color) else null }
            .getOrNull() ?: defaultColor
    }

    Card(
        shape = RoundedCornerShape(Dimensions.small),
        border = BorderStroke(Dimensions.smaller, backgroundColor),
        elevation = 3.dp,
        modifier = Modifier
            .animateItemPlacement()
            .testTag(TestTags.Library.item)
            .padding(Dimensions.smallest)
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = { onLongClick(manga) },
                onClick = { onClick(manga.id) })
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.squareMaxSize()) {
                Image(
                    rememberImage(manga.logo),
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
            Text(
                text = manga.name,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(bottom = 5.dp)
                    .padding(horizontal = 6.dp),
                color = contentColorFor(backgroundColor),
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopEnd
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${manga.noRead}",
                    maxLines = 1,
                    modifier = Modifier
                        .background(backgroundColor)
                        .padding(4.dp),
                    color = contentColorFor(backgroundColor)
                )

                if (cat == context.CATEGORY_ALL && showCategory)
                    Text(
                        text = manga.category,
                        color = defaultColor,
                        modifier = Modifier
                            .padding(end = 3.dp)
                            .background(MaterialTheme.colors.contentColorFor(defaultColor))
                            .padding(start = 3.dp, bottom = 1.dp, end = 3.dp)
                    )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.LibrarySmallItem(
    onClick: (Long) -> Unit,
    onLongClick: (SimplifiedManga) -> Unit,
    manga: SimplifiedManga,
    cat: String,
    showCategory: Boolean,
) {
    val context = LocalContext.current
    val defaultColor = MaterialTheme.colors.primary
    val backgroundColor by remember {
        mutableStateOf(runCatching { Color(manga.color) }.getOrDefault(defaultColor))
    }

    Card(
        shape = RoundedCornerShape(Dimensions.small),
        border = BorderStroke(Dimensions.smaller, backgroundColor),
        modifier = Modifier
            .animateItemPlacement()
            .testTag(TestTags.Library.item)
            .padding(Dimensions.smallest)
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = { onLongClick(manga) },
                onClick = { onClick(manga.id) })
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalInsetsPadding()
                .padding(Dimensions.smaller)
        ) {
            Image(
                rememberImage(manga.logo),
                modifier = Modifier
                    .padding(Dimensions.smaller)
                    .size(Dimensions.Image.bigger),
                contentDescription = null,
            )
            Text(
                text = manga.name,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f, true)
                    .padding(start = Dimensions.small)
                    .align(Alignment.CenterVertically),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${manga.noRead}",
                maxLines = 1,
                modifier = Modifier
                    .padding(horizontal = Dimensions.small)
                    .align(Alignment.CenterVertically),
                fontWeight = FontWeight.Bold
            )
        }
        if (cat == context.CATEGORY_ALL && showCategory)
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .endInsetsPadding()
                    .padding(Dimensions.smallest)
                    .fillMaxWidth()
            ) {
                Text(
                    text = manga.category,
                    color = MaterialTheme.colors.contentColorFor(backgroundColor),
                    modifier = Modifier
                        .background(backgroundColor)
                        .padding(Dimensions.smallest)
                )
            }
    }
}
