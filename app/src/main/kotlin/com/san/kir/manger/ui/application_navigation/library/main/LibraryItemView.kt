package com.san.kir.manger.ui.application_navigation.library.main

import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.san.kir.core.compose_utils.rememberImage
import com.san.kir.core.support.CATEGORY_ALL
import com.san.kir.data.models.SimpleManga
import com.san.kir.manger.ui.application_navigation.library.LibraryNavTarget
import com.san.kir.core.utils.TestTags
import com.san.kir.manger.utils.compose.navigate
import com.san.kir.manger.utils.compose.rememberImage
import com.san.kir.manger.utils.compose.squareMaxSize

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemView(
    nav: NavHostController,
    manga: SimpleManga,
    viewModel: LibraryViewModel,
    content: @Composable () -> Unit,
) {

    val defaultColor = MaterialTheme.colors.primary
    val backgroundColor by remember {
        mutableStateOf(runCatching { Color(manga.color) }.getOrDefault(defaultColor))
    }
    Card(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(3.dp, backgroundColor),
        modifier = Modifier
            .testTag(TestTags.Library.item)
            .padding(3.dp)
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = { viewModel.changeSelectedManga(true, manga) },
                onClick = { nav.navigate(LibraryNavTarget.Chapters, manga.name) })
    ) {
        content()
    }
}

@Composable
fun LibraryLargeItemView(
    nav: NavHostController,
    manga: SimpleManga,
    cat: String,
    viewModel: LibraryViewModel,
    context: Context = LocalContext.current
) {
    val showCategory by viewModel.showCategory.collectAsState(false)
    val countNotRead by viewModel.countNotRead(manga.name).collectAsState(0)
    val primaryColor = MaterialTheme.colors.primary
    var backgroundColor by remember { mutableStateOf(primaryColor) }

    ItemView(nav, manga, viewModel) {
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
                    .padding(horizontal = 6.dp)
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopEnd
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$countNotRead",
                    maxLines = 1,
                    modifier = Modifier
                        .background(backgroundColor)
                        .padding(4.dp)
                )

                if (cat == context.CATEGORY_ALL && showCategory)
                    Text(
                        text = manga.categories,
                        color = primaryColor,
                        modifier = Modifier
                            .padding(end = 3.dp)
                            .background(MaterialTheme.colors.contentColorFor(primaryColor))
                            .padding(start = 3.dp, bottom = 1.dp, end = 3.dp)
                    )
            }
        }
    }

    LaunchedEffect(manga) {
        if (manga.color != 0) {
            backgroundColor = try {
                Color(manga.color)
            } catch (e: Exception) {
                primaryColor
            }
        }
    }
}

@Composable
fun LibrarySmallItemView(
    nav: NavHostController,
    manga: SimpleManga,
    cat: String,
    viewModel: LibraryViewModel,
    context: Context = LocalContext.current
) {
    val showCategory by viewModel.showCategory.collectAsState(false)
    val countNotRead by viewModel.countNotRead(manga.name).collectAsState(0)
    val primaryColor = MaterialTheme.colors.primary
    var backgroundColor by remember { mutableStateOf(primaryColor) }

    val heightSize = 70.dp

    ItemView(nav, manga, viewModel) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp)
        ) {
            Image(
                rememberImage(manga.logo),
                modifier = Modifier
                    .padding(2.dp)
                    .size(heightSize),
                contentDescription = null,
            )
            Text(
                text = manga.name,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f, true)
                    .padding(start = 5.dp)
                    .align(Alignment.CenterVertically),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "$countNotRead",
                maxLines = 1,
                modifier = Modifier
                    .padding(5.dp)
                    .align(Alignment.CenterVertically),
                fontWeight = FontWeight.Bold
            )
        }
        if (cat == context.CATEGORY_ALL && showCategory)
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .padding(3.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = manga.categories,
                    color = MaterialTheme.colors.contentColorFor(backgroundColor),
                    modifier = Modifier
                        .background(backgroundColor)
                        .padding(2.dp)
                )
            }
    }

    LaunchedEffect(manga) {
        if (manga.color != 0) {
            backgroundColor = try {
                Color(manga.color)
            } catch (e: Exception) {
                primaryColor
            }
        }
    }
}
