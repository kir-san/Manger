package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.san.kir.core.compose_utils.DialogText
import com.san.kir.core.compose_utils.ImageWithStatus
import com.san.kir.core.compose_utils.LabelText
import com.san.kir.core.compose_utils.ScreenContent
import com.san.kir.core.compose_utils.topBar
import com.san.kir.core.utils.browse
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.manger.R
import com.san.kir.manger.ui.SuppotMangaViewModel
import com.san.kir.manger.utils.extensions.listStrToString

@Composable
fun MangaInfoScreen(
    navigateUp: () -> Unit,
    navigateToAdd: (String) -> Unit,
    vm: SiteCatalogItemViewModel,
    viewModel: SuppotMangaViewModel = hiltViewModel(),
) {
    var isUpdate by remember { mutableStateOf(false) }
    var isAdded by remember { mutableStateOf(false) }

    ScreenContent(
        topBar = topBar(
            navigationListener = navigateUp,
            title = stringResource(R.string.manga_info_dialog_title),
            actions = {
                AnimatedVisibility(visible = isAdded) {
                    MenuIcon(
                        icon = Icons.Default.Add,
                        tint = MaterialTheme.colors.onBackground
                    ) {
                        navigateToAdd(vm.url)
                    }
                }
            },
            hasAction = isUpdate
        ),
    ) {
        MangaInfoContent(vm)
    }

    LaunchedEffect(vm.item) {
        isAdded = withDefaultContext {
            viewModel.isContainManga(vm.item).not()
        }
    }

    LaunchedEffect(true) {
        kotlin.runCatching {
            isUpdate = true
            vm.item = viewModel.fullElement(vm.item)
        }.fold(
            onSuccess = { isUpdate = false },
            onFailure = {}
        )
    }
}

@Composable
private fun MangaInfoContent(
    vm: SiteCatalogItemViewModel,
    ctx: Context = LocalContext.current,
) {
    LabelText(R.string.manga_info_dialog_name)
    DialogText(vm.item.name)

    LabelText(R.string.manga_info_dialog_authors)
    DialogText(listStrToString(vm.item.authors))

    LabelText(R.string.manga_info_dialog_type)
    DialogText(vm.item.type)

    LabelText(R.string.manga_info_dialog_status_edition)
    DialogText(vm.item.statusEdition)

    LabelText(R.string.manga_info_dialog_volume)
    DialogText(stringResource(R.string.catalog_for_one_site_prefix_volume, vm.item.volume))

    LabelText(R.string.manga_info_dialog_status_translate)
    DialogText(vm.item.statusTranslate)

    LabelText(R.string.manga_info_dialog_genres)
    DialogText(listStrToString(vm.item.genres))

    LabelText(R.string.manga_info_dialog_link)
    DialogText(vm.item.link, color = Color.Blue) { ctx.browse(vm.item.link) }

    LabelText(R.string.manga_info_dialog_about)
    DialogText(vm.item.about)

    LabelText(R.string.manga_info_dialog_logo)
    ImageWithStatus(vm.item.logo)}
