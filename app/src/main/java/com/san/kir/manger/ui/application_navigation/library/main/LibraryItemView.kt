package com.san.kir.manger.ui.application_navigation.library.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.ui.application_navigation.library.LibraryNavTarget
import com.san.kir.manger.ui.utils.navigate
import com.san.kir.manger.ui.utils.squareMaxSize
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.loadImage
import com.san.kir.manger.workmanager.MangaDeleteWorker

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemView(
    nav: NavHostController,
    manga: Manga,
    viewModel: LibraryViewModel,
    content: @Composable () -> Unit,
) {

    val defaultColor = MaterialTheme.colors.primary
    var backgroundColor by remember {
        mutableStateOf(runCatching { Color(manga.color) }.getOrDefault(defaultColor))
    }
    var expandedMenu by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(3.dp, backgroundColor),
        modifier = Modifier
            .padding(3.dp)
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = { viewModel.changeSelectedManga(true, manga) },
                onClick = { nav.navigate(LibraryNavTarget.Chapters, manga.unic) })
    ) {
        content()
    }

    // Выпадающее меню по долгому нажатию на элемент
    DropdownMenu(expanded = expandedMenu, onDismissRequest = { expandedMenu = false }) {
        MenuText(id = R.string.library_popupmenu_about, onClick = {
            expandedMenu = false
            mainNav.navigate(AboutManga, manga)
        })

        MenuText(id = R.string.library_popupmenu_set_category, onClick = {
            expandedMenu = false
            expandedCategory = true
        })

        MenuText(id = R.string.library_popupmenu_storage, onClick = {
            expandedMenu = false
            mainNav.navigate(StorageManga, manga)
        })

        MenuText(id = R.string.library_popupmenu_delete, onClick = {
            expandedMenu = false
            deleteDialog = true
        })
    }

    // Выпадающее меню для выбора категории
    DropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }) {
        state.categories.forEach { item ->
            MenuText(item.category.name) {
                manga.categories = item.category.name
                viewModel.update(manga)
                expandedCategory = false
            }
        }
    }

    // Окно полного удаления манги
    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = { deleteDialog = false },
            buttons = {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    val pad = PaddingValues(4.dp, 4.dp)

                    OutlinedButton(onClick = {
                        deleteDialog = false
                        MangaDeleteWorker.addTask(context, manga, true)
                    }, modifier = Modifier.padding(pad)) {
                        Text(text = stringResource(id = R.string.library_popupmenu_delete_ok_with_files))
                    }

                    OutlinedButton(onClick = {
                        deleteDialog = false
                        MangaDeleteWorker.addTask(context, manga)
                    }, modifier = Modifier.padding(pad)) {
                        Text(text = stringResource(id = R.string.library_popupmenu_delete_ok))
                    }

                    OutlinedButton(
                        onClick = { deleteDialog = false },
                        modifier = Modifier.padding(pad)
                    ) {
                        Text(text = stringResource(id = R.string.library_popupmenu_delete_no))
                    }
                }
            },
            title = { Text(text = stringResource(id = R.string.library_popupmenu_delete_title)) },
            text = { Text(text = stringResource(id = R.string.library_popupmenu_delete_message)) }
        )
    }
}

@Composable
fun LibraryLargeItemView(
    nav: NavHostController,
    manga: Manga,
    cat: String,
    viewModel: LibraryViewModel
) {
    var logoManga by remember { mutableStateOf(ImageBitmap(60, 60)) }
    var countNotRead by remember { mutableStateOf(0) }
    val primaryColor = MaterialTheme.colors.primary
    var backgroundColor by remember { mutableStateOf(primaryColor) }

    ItemView(manga, mainNav, state) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.squareMaxSize()) {
                Image(
                    BitmapPainter(logoManga),
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

                if (cat == CATEGORY_ALL && state.isShowCategory)
                    Text(
                        text = manga.categories,
                        color = backgroundColor,
                        modifier = Modifier
                            .padding(end = 3.dp)
                            .background(MaterialTheme.colors.contentColorFor(backgroundColor))
                            .padding(horizontal = 3.dp)
                    )
            }
        }
    }

    LaunchedEffect(manga) {
        loadImage(manga.logo) {
            onSuccess { image ->
                logoManga = image
            }
            start()
        }
        if (manga.color != 0) {
            backgroundColor = try {
                Color(manga.color)
            } catch (e: Exception) {
                primaryColor
            }
        }
        countNotRead = viewModel.countNotRead(manga.unic)
    }
}

@Composable
fun LibrarySmallItemView(
    nav: NavHostController,
    manga: Manga,
    cat: String,
    viewModel: LibraryViewModel
) {
    var logoManga by remember { mutableStateOf(ImageBitmap(60, 60)) }
    var countNotRead by remember { mutableStateOf(0) }
    val primaryColor = MaterialTheme.colors.primary
    var backgroundColor by remember { mutableStateOf(primaryColor) }

    val heightSize = 70.dp

    ItemView(manga, mainNav, state) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp)
        ) {
            Image(
                logoManga,
                modifier = Modifier
                    .padding(2.dp)
                    .clip(CircleShape)
                    .size(heightSize),
                contentDescription = null,
                contentScale = ContentScale.Crop,
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
        if (cat == CATEGORY_ALL && state.isShowCategory)
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
        loadImage(manga.logo) {
            onSuccess { image ->
                logoManga = image
            }
            start()
        }
        if (manga.color != 0) {
            backgroundColor = try {
                Color(manga.color)
            } catch (e: Exception) {
                primaryColor
            }
        }
        countNotRead = viewModel.countNotRead(manga.unic)
    }
}
