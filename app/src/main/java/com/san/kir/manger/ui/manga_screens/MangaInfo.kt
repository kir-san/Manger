package com.san.kir.manger.ui.manga_screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.san.kir.ankofork.browse
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.ui.AddManga
import com.san.kir.manger.ui.MangaInfo
import com.san.kir.manger.ui.catalog.CatalogViewModel
import com.san.kir.manger.ui.utils.DialogText
import com.san.kir.manger.ui.utils.LabelText
import com.san.kir.manger.ui.utils.TopBarScreen
import com.san.kir.manger.ui.utils.getElement
import com.san.kir.manger.utils.extensions.listStrToString
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.san.kir.manger.ui.utils.navigate

@ExperimentalAnimationApi
@Composable
fun MangaInfoScreen(nav: NavHostController) {
    val item = remember { mutableStateOf(nav.getElement(MangaInfo) ?: SiteCatalogElement()) }

    val viewModel: CatalogViewModel = viewModel()
    var isAdded by remember { mutableStateOf(false) }

    LaunchedEffect(item) {
        isAdded = !withContext(Dispatchers.Default) {
            viewModel.isContainManga(item.value)
        }
    }

    TopBarScreen(
        nav = nav,
        title = stringResource(id = R.string.manga_info_dialog_title),
        actions = {
            AnimatedVisibility(visible = isAdded) {
                IconButton(onClick = { nav.navigate(AddManga, item.value) }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "add manga",
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MangaInfoContent(item)
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun MangaInfoContent(item: MutableState<SiteCatalogElement>) {
    val ctx = LocalContext.current

    var element by item
    var isUpdate by remember { mutableStateOf(false) }
    var isShowLogo by remember { mutableStateOf(false) }
    var statusLogo by remember { mutableStateOf(StatusLogo.Standart) }
    var logo by remember { mutableStateOf(ImageBitmap(60, 60)) }


    if (isUpdate) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp)
    ) {
        LabelText(idRes = R.string.manga_info_dialog_name)
        DialogText(text = element.name)

        LabelText(idRes = R.string.manga_info_dialog_authors)
        DialogText(text = listStrToString(element.authors))

        LabelText(idRes = R.string.manga_info_dialog_type)
        DialogText(text = element.type)

        LabelText(idRes = R.string.manga_info_dialog_status_edition)
        DialogText(text = element.statusEdition)

        LabelText(idRes = R.string.manga_info_dialog_volume)
        DialogText(
            text = stringResource(
                R.string.catalog_for_one_site_prefix_volume, element.volume
            )
        )

        LabelText(idRes = R.string.manga_info_dialog_status_translate)
        DialogText(text = element.statusTranslate)

        LabelText(idRes = R.string.manga_info_dialog_genres)
        DialogText(text = listStrToString(element.genres))

        LabelText(idRes = R.string.manga_info_dialog_link)
        DialogText(text = element.link, color = Color.Blue) {
            ctx.browse(element.link)
        }

        LabelText(idRes = R.string.manga_info_dialog_about)
        DialogText(text = element.about)

        LabelText(idRes = R.string.manga_info_dialog_logo)
        AnimatedVisibility(visible = !isShowLogo) {
            DialogText(
                text = stringResource(
                    id = when (statusLogo) {
                        StatusLogo.Standart -> R.string.manga_info_dialog_loading
                        StatusLogo.Error -> R.string.manga_info_dialog_loading_failed
                        StatusLogo.None -> R.string.manga_info_dialog_not_image
                    }
                )
            )
        }
        AnimatedVisibility(visible = isShowLogo) { Image(logo, null) }
    }

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
