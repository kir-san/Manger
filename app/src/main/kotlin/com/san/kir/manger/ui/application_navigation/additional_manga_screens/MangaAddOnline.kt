package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.san.kir.manger.R
import com.san.kir.ui.utils.TopBarScreenWithInsets

@Composable
fun MangaAddOnlineScreen(
    navigateToBack: () -> Unit,
    navigateToNext: (String) -> Unit,
) {
    TopBarScreenWithInsets(
        navigationButtonListener = navigateToBack,
        title = stringResource(R.string.library_add_manga_title)
    ) {
        MangaAddOnlineContent(navigateToBack, navigateToNext)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ColumnScope.MangaAddOnlineContent(
    navigateToBack: () -> Unit,
    navigateToNext: (String) -> Unit,
    viewModel: MangaAddOnlineViewModel = hiltViewModel(),
) {
    val inputText by viewModel.enteredText.collectAsState()
    val check by viewModel.isCheckingUrl.collectAsState()
    val isError by viewModel.isErrorAvailable.collectAsState()
    val validate by viewModel.validate.collectAsState()
    val isEnable by viewModel.isEnableAdding.collectAsState()

    // Текстовое поле ввода ссылки
    OutlinedTextField(
        value = inputText,
        onValueChange = viewModel::updateEnteredText,
        singleLine = true,
        isError = isError,
        placeholder = {
            Text(stringResource(R.string.library_add_manga_hint))
        },
        modifier = Modifier.fillMaxWidth(),
    )
    ClipboardText(onPaste = viewModel::updateEnteredText)
    // Индикатор обработки запроса
    if (check)
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(15.dp)
                .padding(vertical = 5.dp)
        )

    // Сообщение об ошибке
    AnimatedVisibility(visible = isError) {
        Text(
            stringResource(R.string.library_add_manga_error),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.error)
    }

    // Вывод катологов, которым может соответсвтовать введенный адрес ссылки
    AnimatedVisibility(visible = validate.isNotEmpty()) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            mainAxisAlignment = FlowMainAxisAlignment.End,
            crossAxisAlignment = FlowCrossAxisAlignment.End,
        ) {
            validate.forEach { item ->
                Card(modifier = Modifier
                    .padding(5.dp)
                    .clickable { viewModel.updateEnteredText(item) }
                ) {
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

    // Кнопки
    FlowRow(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(),
        mainAxisAlignment = FlowMainAxisAlignment.End,
        crossAxisAlignment = FlowCrossAxisAlignment.End,
    ) {
        Button(onClick = navigateToBack, modifier = Modifier.padding(end = 16.dp)) {
            Text(text = stringResource(id = R.string.library_add_manga_cancel_btn))
        }

        Button(onClick = {
            viewModel.checkUrl(onSuccess = navigateToNext)
        }, enabled = isEnable) {
            Text(text = stringResource(id = R.string.library_add_manga_add_btn))
        }
    }
}

@Preview
@Composable
fun ClipboardTextPreview() {
    ClipboardText(
        "Типа ссылка на сайт",
        onPaste = {})
}

@Composable
private fun ClipboardText(
    clipboardText: String? = LocalClipboardManager.current.getText()?.text,
    onPaste: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { clipboardText?.let { onPaste(it) } }
    ) {
        Column(modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()) {
            Text(
                stringResource(R.string.add_manga_online_clipboard),
                fontSize = 15.sp
            )

            Text(
                "$clipboardText",
                fontSize = 13.sp,
                maxLines = 1,
            )
        }
    }
}
