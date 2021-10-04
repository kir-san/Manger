package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.san.kir.ankofork.browse
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.ui.SuppotMangaViewModel
import com.san.kir.manger.ui.application_navigation.catalog.CatalogsNavTarget
import com.san.kir.manger.ui.utils.DialogText
import com.san.kir.manger.ui.utils.ImageWithStatus
import com.san.kir.manger.ui.utils.LabelText
import com.san.kir.manger.ui.utils.TopBarScreenWithInsets
import com.san.kir.manger.ui.utils.navigate
import com.san.kir.manger.utils.extensions.listStrToString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MangaInfoScreen(
    nav: NavHostController,
    item: SiteCatalogElement,
    viewModel: SuppotMangaViewModel = hiltViewModel(),
) {
    var isAdded by remember { mutableStateOf(false) }

    LaunchedEffect(item) {
        isAdded = !withContext(Dispatchers.Default) {
            viewModel.isContainManga(item)
        }
    }

    TopBarScreenWithInsets(
        navigationButtonListener = { nav.navigateUp() },
        title = stringResource(id = R.string.manga_info_dialog_title),
        actions = {
            AnimatedVisibility(visible = isAdded) {
                IconButton(onClick = { nav.navigate(CatalogsNavTarget.AddLocal, item.link) }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "add manga",
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }
        }
    ) {
        MangaInfoContent(item, viewModel)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun MangaInfoContent(
    item: SiteCatalogElement,
    viewModel: SuppotMangaViewModel,
    ctx: Context = LocalContext.current
) {
    var element by rememberSaveable { mutableStateOf(item) }

    var isUpdate by remember { mutableStateOf(false) }

    if (isUpdate) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

    LabelText(R.string.manga_info_dialog_name)
    DialogText(element.name)

    LabelText(R.string.manga_info_dialog_authors)
    DialogText(listStrToString(element.authors))

    LabelText(R.string.manga_info_dialog_type)
    DialogText(element.type)

    LabelText(R.string.manga_info_dialog_status_edition)
    DialogText(element.statusEdition)

    LabelText(R.string.manga_info_dialog_volume)
    DialogText(stringResource(R.string.catalog_for_one_site_prefix_volume, element.volume))

    LabelText(R.string.manga_info_dialog_status_translate)
    DialogText(element.statusTranslate)

    LabelText(R.string.manga_info_dialog_genres)
    DialogText(listStrToString(element.genres))

    LabelText(R.string.manga_info_dialog_link)
    DialogText(element.link, color = Color.Blue) { ctx.browse(element.link) }

    LabelText(R.string.manga_info_dialog_about)
    DialogText(element.about)

    LabelText(R.string.manga_info_dialog_logo)
    ImageWithStatus(element.logo)

    LaunchedEffect(true) {
        kotlin.runCatching {
            isUpdate = true
            element = viewModel.fullElement(element)
        }.fold(
            onSuccess = { isUpdate = false },
            onFailure = {}
        )
    }
}
