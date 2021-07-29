package com.san.kir.manger.ui.manga_screens

import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.rememberInsetsPaddingValues
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
import com.san.kir.manger.ui.utils.navigate
import com.san.kir.manger.utils.extensions.formatDouble
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.lengthMb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@ExperimentalAnimationApi
@Composable
fun AboutMangaScreen(nav: NavController) {
    val item =
        remember { mutableStateOf(nav.getElement(AboutMangaNavigationDestination) ?: Manga()) }

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

    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    rememberInsetsPaddingValues(
                        insets = LocalWindowInsets.current.systemBars,
                        applyStart = true, applyEnd = true,
                        applyBottom = false, applyTop = false,
                        additionalTop = contentPadding.calculateTopPadding(),
                        additionalStart = 16.dp, additionalEnd = 16.dp
                    )
                )
                .verticalScroll(rememberScrollState())
        ) {
            AboutMangaContent(item)
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun AboutMangaContent(
    item: MutableState<Manga>,
    ctx: Context = LocalContext.current
) {
    val manga by item

    val calculateString = stringResource(id = R.string.about_manga_dialog_calculate)
    var size by remember { mutableStateOf(calculateString) }

    Spacer(modifier = Modifier.height(16.dp))

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

    LaunchedEffect(item) {
        size = withContext(Dispatchers.Default) {
            ctx.getString(
                R.string.library_page_item_size,
                formatDouble(getFullPath(manga.path).lengthMb)
            )
        }
    }

    Spacer(modifier = Modifier.navigationBarsHeight(16.dp))
}
