package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.san.kir.core.compose_utils.DialogText
import com.san.kir.core.compose_utils.ImageWithStatus
import com.san.kir.core.compose_utils.LabelText
import com.san.kir.core.compose_utils.SmallSpacer
import com.san.kir.core.compose_utils.ScreenContent
import com.san.kir.core.compose_utils.topBar
import com.san.kir.core.utils.browse
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.core.utils.formatDouble
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.lengthMb
import com.san.kir.data.models.base.Manga
import com.san.kir.manger.R


@Composable
fun MangaAboutScreen(
    navigateUp: () -> Unit,
    item: Manga,
    category: String,
) {
    ScreenContent(
        topBar = topBar(
            navigationListener = navigateUp,
            title = stringResource(R.string.manga_info_dialog_title),
        ),
    ) {
        MangaAboutContent(item, category)
    }
}

@Composable
private fun MangaAboutContent(
    manga: Manga,
    category: String,
    ctx: Context = LocalContext.current,
) {

    val calculateString = stringResource(id = R.string.about_manga_dialog_calculate)
    var size by remember { mutableStateOf(calculateString) }

    LabelText(idRes = R.string.about_manga_dialog_name)
    DialogText(text = manga.name)

    SmallSpacer()

    LabelText(idRes = R.string.about_manga_dialog_category)
    DialogText(text = category)

    SmallSpacer()

    LabelText(idRes = R.string.about_manga_dialog_authors)
    DialogText(text = manga.authorsList.toString())

    SmallSpacer()

    LabelText(idRes = R.string.about_manga_dialog_status_edition)
    DialogText(text = manga.status)

    SmallSpacer()

    LabelText(idRes = R.string.about_manga_dialog_genres)
    DialogText(text = manga.genresList.toString())

    SmallSpacer()

    LabelText(idRes = R.string.about_manga_dialog_storage)
    DialogText(text = manga.path)

    SmallSpacer()

    LabelText(idRes = R.string.about_manga_dialog_volume)
    DialogText(text = size)

    SmallSpacer()

    LabelText(idRes = R.string.about_manga_dialog_link)
    DialogText(
        text = manga.host + manga.shortLink,
        color = Color.Blue,
        onClick = { ctx.browse(manga.host + manga.shortLink) }
    )

    SmallSpacer()

    LabelText(idRes = R.string.about_manga_dialog_about)
    DialogText(manga.about)

    SmallSpacer()

    LabelText(idRes = R.string.about_manga_dialog_logo)
    ImageWithStatus(manga.logo)

    LaunchedEffect(manga) {
        size = withDefaultContext {
            ctx.getString(
                R.string.library_page_item_size,
                formatDouble(getFullPath(manga.path).lengthMb)
            )
        }
    }
}
