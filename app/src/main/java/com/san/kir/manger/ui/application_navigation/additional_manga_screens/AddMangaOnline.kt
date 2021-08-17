package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.ui.application_navigation.ApplicationNavigationDestination.AddManga
import com.san.kir.manger.ui.utils.TopBarScreenWithInsets
import com.san.kir.manger.ui.utils.navigate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO добавить вставку из буфера обмена одной кнопкой с выводом содержимого
@Composable
fun AddMangaOnlineScreen(nav: NavHostController) {
    TopBarScreenWithInsets(
        nav = nav,
        title = stringResource(R.string.library_add_manga_title)
    ) {
        AddMangaOnlineContent(nav)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ColumnScope.AddMangaOnlineContent(
    nav: NavHostController,
    scope: CoroutineScope = rememberCoroutineScope(),
) {
    var siteNames = remember { emptyList<String>() }
    var validate by remember { mutableStateOf(emptyList<String>()) }
    var isError by remember { mutableStateOf(false) }
    val validError = stringResource(id = R.string.library_add_manga_error)
    val hint = stringResource(id = R.string.library_add_manga_hint)

    LaunchedEffect(true) {
        siteNames = withContext(Dispatchers.IO) { ManageSites.CATALOG_SITES.map { it.catalogName } }
        validate = siteNames
    }

    var inputText by remember { mutableStateOf("") }
    var check by remember { mutableStateOf(false) }
    val isEnable = remember { mutableStateOf(false) }

    OutlinedTextField(
        value = inputText,
        onValueChange = {
            inputText = it
            validate = validateUrl(it, siteNames, isEnable)
            isError = false
        },
        singleLine = true,
        isError = isError,
        placeholder = { Text(hint) },
        modifier = Modifier.fillMaxWidth(),
    )
    if (check)
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(15.dp)
                .padding(vertical = 5.dp)
        )

    AnimatedVisibility(visible = isError) {
        Text(validError, textAlign = TextAlign.Center, color = MaterialTheme.colors.error)
    }

    AnimatedVisibility(visible = validate.isNotEmpty()) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            mainAxisAlignment = FlowMainAxisAlignment.End,
            crossAxisAlignment = FlowCrossAxisAlignment.End,
        ) {
            validate.forEach { item ->
                Card(modifier = Modifier
                    .padding(5.dp)
                    .clickable {
                        inputText = item
                        validate = validateUrl(item, siteNames, isEnable)
                    }) {
                    Text(
                        item,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.weight(1f, true))

    FlowRow(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(),
        mainAxisAlignment = FlowMainAxisAlignment.End,
        crossAxisAlignment = FlowCrossAxisAlignment.End,
    ) {
        Button(onClick = { nav.popBackStack() }, modifier = Modifier.padding(end = 16.dp)) {
            Text(text = stringResource(id = R.string.library_add_manga_cancel_btn))
        }

        Button(onClick = {
            scope.launch(Dispatchers.Default) {
                check = true
                isEnable.value = false
                ManageSites.getElementOnline(inputText)?.also { item ->
                    nav.navigate(AddManga, item)
                } ?: run {
                    isError = true
                    check = false
                }


            }
        }, enabled = isEnable.value) {
            Text(text = stringResource(id = R.string.library_add_manga_add_btn))
        }
    }
}

private fun validateUrl(
    text: String,
    siteNames: List<String>,
    isEnable: MutableState<Boolean>
): List<String> {
    return if (text.isNotBlank()) {
        // список сайтов подходящий под введеный адрес
        val temp = siteNames
            .filter { it.contains(text) }

        // Если список не пуст, то отображаем его
        if (temp.isNotEmpty()) {
            isEnable.value = false
            temp
        } else {
            // Если в списке что-то есть
            // то получаем соответствующий сайт
            val site = siteNames
                .filter { text.contains(it) }

            //Если есть хоть один сайт, то включаем кнопку
            if (site.isNotEmpty()) {
                isEnable.value = true
                site
            } else {
                isEnable.value = false
                emptyList()
            }
        }
    }
    // Если нет текста, то отображается список
    // доступных сайтов
    else {
        isEnable.value = false
        siteNames
    }
}
