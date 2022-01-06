package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.san.kir.core.utils.log
import com.san.kir.data.models.Manga
import com.san.kir.manger.R
import com.san.kir.core.compose_utils.CheckBoxText
import com.san.kir.core.compose_utils.DropDownTextField
import com.san.kir.core.compose_utils.ImageWithStatus
import com.san.kir.core.compose_utils.LabelText
import com.san.kir.core.compose_utils.TopBarScreenWithInsets


@Composable
fun MangaEditScreen(
    nav: NavHostController,
    mangaUnic: String,
    viewModel: MangaEditViewModel = hiltViewModel()
) {
    viewModel.mangaUnic = mangaUnic

    TopBarScreenWithInsets(
        navigationButtonListener = { nav.navigateUp() },
        title = stringResource(id = R.string.edit_manga_title),
        actions = {
            IconButton(onClick = { /* save manga */
                viewModel.update()
                nav.navigateUp()
            }) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = "save manga",
                    tint = MaterialTheme.colors.onBackground
                )
            }
        }

    ) {
        MangaEditContent(viewModel.manga) { viewModel.manga = it }
    }
    log = ("viewModel.manga is ${viewModel.manga.name}")
}

@Composable
private fun MangaEditContent(
    manga: Manga,
    change: (Manga) -> Unit,
) {
    log = ("manga is ${manga.name}")
    LabelText(idRes = R.string.about_manga_dialog_name)
    TextField(manga.name, enabled = false) { change(manga.apply { name = it }) }

    LabelText(idRes = R.string.about_manga_dialog_authors)
    TextField(manga.authors) { change(manga.apply { authors = it }) }

    LabelText(idRes = R.string.about_manga_dialog_about)
    TextField(manga.about, maxLines = 10) { change(manga.apply { about = it }) }

    LabelText(idRes = R.string.about_manga_dialog_category)
    CategoryDropDownTextField(manga = manga, change = change)

    LabelText(idRes = R.string.about_manga_dialog_genres)
    TextField(manga.genres) { change(manga.apply { genres = it }) }

    LabelText(idRes = R.string.about_manga_dialog_storage)
    TextField(manga.path, enabled = false) { change(manga.apply { path = it }) }

    LabelText(idRes = R.string.about_manga_dialog_status_edition)
    StatusDropDownTextField(manga = manga, change = change)

    LabelText(idRes = R.string.about_manga_dialog_link)
    TextField(manga.host + manga.shortLink, enabled = false) { change(manga.apply { host = it }) }

    LabelText(idRes = R.string.add_manga_update)
    CheckBoxText(
        state = manga.isUpdate,
        onChange = { change(manga.apply { isUpdate = it }) },
        firstTextId = R.string.add_manga_update_available
    )

    LabelText(idRes = R.string.add_manga_color)
    ColorPicker(manga.color) { change(manga.apply { color = it }) }

    LabelText(idRes = R.string.about_manga_dialog_logo)
    TextField(manga.logo, enabled = false) { change(manga.apply { logo = it }) }
     ImageWithStatus(manga.logo)
}

@Composable
fun CategoryDropDownTextField(
    viewModel: MangaEditViewModel = hiltViewModel(),
    manga: Manga,
    change: (Manga) -> Unit,
) {
    val categoryNames by viewModel.categoryNames.collectAsState(emptyList())

    DropDownTextField(
        inititalValue = manga.categories,
        valuesList = categoryNames,
        onChangeValue = { change(manga.apply { categories = it }) }
    )
}

@Composable
fun StatusDropDownTextField(
    viewModel: MangaEditViewModel = hiltViewModel(),
    manga: Manga,
    change: (Manga) -> Unit,
) {
    DropDownTextField(
        inititalValue = manga.status,
        valuesList = viewModel.statuses,
        onChangeValue = { change(manga.apply { status = it }) }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ColorPicker(initialValue: Int, onValueChange: (Int) -> Unit) {
    val defaultColor = MaterialTheme.colors.primary
    var color by remember {
        mutableStateOf(
            runCatching { Color(initialValue) }.getOrDefault(defaultColor)
        )
    }
    onValueChange(color.toArgb())

    var picker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .clickable { picker = !picker }
            .fillMaxWidth()
            .height(40.dp)
            .background(color)
            .border(
                width = 2.dp,
                color = MaterialTheme.colors.primary,
                shape = RoundedCornerShape(5.dp)
            )
    )

    AnimatedVisibility(
        picker,
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(initialAlpha = 0.3f),
        exit = slideOutVertically() + shrinkVertically() + fadeOut()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colors.primary,
                    shape = RoundedCornerShape(5.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "R", fontWeight = FontWeight.Bold)
                Slider(
                    value = color.red,
                    onValueChange = { color = color.copy(red = it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "G", fontWeight = FontWeight.Bold)
                Slider(
                    value = color.green,
                    onValueChange = { color = color.copy(green = it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "B", fontWeight = FontWeight.Bold)
                Slider(
                    value = color.blue,
                    onValueChange = { color = color.copy(blue = it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "A", fontWeight = FontWeight.Bold)
                Slider(
                    value = color.alpha,
                    onValueChange = { color = color.copy(alpha = it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp)
                )
            }
        }
    }

}

@Composable
private fun TextField(
    initialValue: String,
    maxLines: Int = Int.MAX_VALUE,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit
) {
    var value by rememberSaveable { mutableStateOf(initialValue) }
    onValueChange(value)

    OutlinedTextField(
        value = value,
        onValueChange = { value = it },
        maxLines = maxLines,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    )
}


