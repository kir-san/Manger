package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import android.content.Context
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.lengthMb
import com.san.kir.data.models.Manga
import com.san.kir.manger.R
import com.san.kir.core.compose_utils.DialogText
import com.san.kir.core.compose_utils.ImageWithStatus
import com.san.kir.core.compose_utils.LabelText
import com.san.kir.core.utils.browse
import com.san.kir.core.utils.formatDouble
import com.san.kir.core.compose_utils.TopBarScreenWithInsets


@Composable
fun MangaAboutScreen(
    navUp: () -> Unit,
    navTo: (mangaName: String) -> Unit,
    item: Manga,
) {
    TopBarScreenWithInsets(
        modifier = Modifier,
        navigationButtonListener = navUp,
        title = stringResource(id = R.string.manga_info_dialog_title),
        actions = {
            IconButton(onClick = { navTo(item.name) }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "edit manga",
                    tint = MaterialTheme.colors.onBackground
                )
            }
        }

    ) {
        MangaAboutContent(item)
    }
}

@Composable
private fun MangaAboutContent(
    manga: Manga,
    ctx: Context = LocalContext.current,
) {

    val calculateString = stringResource(id = R.string.about_manga_dialog_calculate)
    var size by remember { mutableStateOf(calculateString) }

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

    LaunchedEffect(manga) {
        size = com.san.kir.core.utils.coroutines.withDefaultContext {
            ctx.getString(
                R.string.library_page_item_size,
                formatDouble(getFullPath(manga.path).lengthMb)
            )
        }
    }
}
