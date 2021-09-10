package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.san.kir.ankofork.browse
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.ui.SuppotMangaViewModel
import com.san.kir.manger.ui.application_navigation.catalog.CatalogsNavTarget
import com.san.kir.manger.ui.utils.DialogText
import com.san.kir.manger.ui.utils.LabelText
import com.san.kir.manger.ui.utils.TopBarScreenWithInsets
import com.san.kir.manger.ui.utils.getElement
import com.san.kir.manger.ui.utils.navigate
import com.san.kir.manger.utils.extensions.listStrToString
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MangaInfoScreen(
    nav: NavHostController,
    viewModel: SuppotMangaViewModel = hiltViewModel(),
) {
    val item = remember { mutableStateOf(nav.getElement(MangaInfo) ?: SiteCatalogElement()) }

    var isAdded by remember { mutableStateOf(false) }

    LaunchedEffect(item) {
        isAdded = !withContext(Dispatchers.Default) {
            viewModel.isContainManga(item.value)
        }
    }

    TopBarScreenWithInsets(
        navigationButtonListener = { nav.navigateUp() },
        title = stringResource(id = R.string.manga_info_dialog_title),
        actions = {
            AnimatedVisibility(visible = isAdded) {
                IconButton(onClick = { nav.navigate(CatalogsNavTarget.AddLocal, item) }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "add manga",
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }
        }
    ) {
        MangaInfoContent(item)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun MangaInfoContent(
    item: MutableState<SiteCatalogElement>,
) {
    val ctx = LocalContext.current

    var element by item
    var isUpdate by remember { mutableStateOf(false) }
    var isShowLogo by remember { mutableStateOf(false) }
    var statusLogo by remember { mutableStateOf(StatusLogo.Standart) }
    var logo by remember { mutableStateOf(ImageBitmap(60, 60)) }

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
    DialogText(element.link, color = Color.Blue) {
        ctx.browse(element.link)
    }

    LabelText(R.string.manga_info_dialog_about)
    DialogText(element.about)

    LabelText(R.string.manga_info_dialog_logo)
    AnimatedVisibility(visible = !isShowLogo) {
        DialogText(
            stringResource(
                id = when (statusLogo) {
                    StatusLogo.Standart -> R.string.manga_info_dialog_loading
                    StatusLogo.Error -> R.string.manga_info_dialog_loading_failed
                    StatusLogo.None -> R.string.manga_info_dialog_not_image
                }
            )
        )
    }
    AnimatedVisibility(visible = isShowLogo) { Image(logo, null) }

    LaunchedEffect(true) {
        kotlin.runCatching {
            isUpdate = true
            element = ManageSites.getFullElement(element)
        }.fold(
            onSuccess = { isUpdate = false },
            onFailure = {}
        )
    }

    LaunchedEffect(element) {
        if (element.logo.isNotEmpty()) {
            loadImage(element.logo) {
                onSuccess { image ->
                    logo = image
                    isShowLogo = true
                }
                onError {
                    statusLogo = StatusLogo.Error
                }
                start()
            }
        } else {
            statusLogo = StatusLogo.None
        }
    }
}

enum class StatusLogo {
    Standart, Error, None
}
