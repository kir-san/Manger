package com.san.kir.manger.ui.manga_screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.san.kir.ankofork.browse
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.ui.AboutMangaNavigationDestination
import com.san.kir.manger.ui.EditManga
import com.san.kir.manger.ui.utils.DialogText
import com.san.kir.manger.ui.utils.ImageWithStatus
import com.san.kir.manger.ui.utils.LabelText
import com.san.kir.manger.ui.utils.TopBarScreen
import com.san.kir.manger.ui.utils.getElement
import com.san.kir.manger.utils.extensions.formatDouble
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.lengthMb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.san.kir.manger.ui.utils.navigate


@ExperimentalAnimationApi
@Composable
fun AboutMangaScreen(nav: NavController) {
    val item = remember { mutableStateOf(nav.getElement(AboutMangaNavigationDestination) ?: Manga()) }

    TopBarScreen(
        nav = nav,
        title = stringResource(id = R.string.manga_info_dialog_title),
        actions = {
            IconButton(onClick = { nav.navigate(EditManga, item.value) }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "edit manga",
                    tint = MaterialTheme.colors.onBackground
                )
            }
        }

    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AboutMangaContent(item)
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun AboutMangaContent(item: MutableState<Manga>) {
    val ctx = LocalContext.current

    val manga by item

    val calculateString = stringResource(id = R.string.about_manga_dialog_calculate)
    var size by remember { mutableStateOf(calculateString) }



    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp)
    ) {
        LabelText(idRes = R.string.about_manga_dialog_name)
        DialogText(text = manga.name)

        LabelText(idRes = R.string.about_manga_dialog_category)
        DialogText(text = manga.categories)

        LabelText(idRes = R.string.about_manga_dialog_authors)
        DialogText(text = manga.authors)

        LabelText(idRes = R.string.about_manga_dialog_status_edition)
        DialogText(text = manga.status)

        LabelText(idRes = R.string.about_manga_dialog_genres)
        DialogText(text = manga.genres)

        LabelText(idRes = R.string.about_manga_dialog_storage)
        DialogText(text = manga.path)

        LabelText(idRes = R.string.about_manga_dialog_volume)
        DialogText(text = size)

        LabelText(idRes = R.string.about_manga_dialog_link)
        DialogText(
            text = manga.host + manga.shortLink,
            color = Color.Blue,
            onClick = { ctx.browse(manga.host + manga.shortLink) }
        )


        LabelText(idRes = R.string.about_manga_dialog_about)
        DialogText(manga.about)

        LabelText(idRes = R.string.about_manga_dialog_logo)
        ImageWithStatus(manga.logo)

    }

    LaunchedEffect(item) {
        size = withContext(Dispatchers.Default) {
            ctx.getString(
                R.string.library_page_item_size,
                formatDouble(getFullPath(manga.path).lengthMb)
            )
        }
    }
}
