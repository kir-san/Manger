package com.san.kir.catalog.ui.addOnline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.san.kir.catalog.R
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.Fonts
import com.san.kir.core.compose.FullWeightSpacer
import com.san.kir.core.compose.NavigationButton
import com.san.kir.core.compose.ScreenContent
import com.san.kir.core.compose.topBar

@Composable
fun AddOnlineScreen(
    navigateUp: () -> Boolean,
    navigateToNext: (String) -> Unit,
) {
    val viewModel: AddOnlineViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    ScreenContent(
        topBar = topBar(
            navigationButton = NavigationButton.Back(navigateUp),
            title = stringResource(R.string.library_add_manga_title),
            hasAction = state.isCheckingUrl
        ),
    ) {
        Content(
            state = state,
            sendEvent = viewModel::sendEvent,
            navigateUp = navigateUp,
            navigateToNext = navigateToNext
        )
    }
}

@Composable
private fun ColumnScope.Content(
    state: AddOnlineState,
    sendEvent: (AddOnlineEvent) -> Unit,
    navigateUp: () -> Boolean,
    navigateToNext: (String) -> Unit,
) {
    var enteredText by remember { mutableStateOf("") }

    // Текстовое поле ввода ссылки
    OutlinedTextField(
        value = enteredText,
        onValueChange = {
            enteredText = it
            sendEvent(AddOnlineEvent.Update(it))
        },
        singleLine = true,
        isError = state.isErrorAvailable,
        placeholder = { Text(stringResource(R.string.library_add_manga_hint)) },
        modifier = Modifier.fillMaxWidth(),
    )

    ClipboardText {
        enteredText = it
        sendEvent(AddOnlineEvent.Update(it))
    }

    // Сообщение об ошибке
    AnimatedVisibility(visible = state.isErrorAvailable) {
        Text(
            stringResource(R.string.library_add_manga_error),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.error
        )
    }

    // Вывод катологов, которым может соответсвтовать введенный адрес ссылки
    AnimatedVisibility(visible = state.validatesCatalogs.isNotEmpty()) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            mainAxisAlignment = FlowMainAxisAlignment.End,
            crossAxisAlignment = FlowCrossAxisAlignment.End,
        ) {
            state.validatesCatalogs.forEach { item ->
                Card(
                    modifier = Modifier
                        .padding(Dimensions.quarter)
                        .clickable {
                            enteredText = item
                            sendEvent(AddOnlineEvent.Update(item))
                        }
                ) {
                    Text(
                        item,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(Dimensions.quarter)
                    )
                }
            }
        }
    }

    FullWeightSpacer()

    // Кнопки
    FlowRow(
        modifier = Modifier
            .padding(top = Dimensions.default)
            .fillMaxWidth(),
        mainAxisAlignment = FlowMainAxisAlignment.End,
        crossAxisAlignment = FlowCrossAxisAlignment.End,
    ) {
        Button(onClick = { navigateUp() }, modifier = Modifier.padding(end = Dimensions.default)) {
            Text(stringResource(R.string.library_add_manga_cancel_btn))
        }

        Button(
            onClick = { navigateToNext(enteredText) },
            enabled = state.isEnableAdding
        ) {
            Text(stringResource(R.string.library_add_manga_add_btn))
        }
    }
}

@Composable
private fun ClipboardText(
    onPaste: (String) -> Unit,
) {
    val clipboardText = LocalClipboardManager.current.getText()?.text

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { clipboardText?.let { onPaste(it) } }
    ) {
        Column(
            modifier = Modifier
                .padding(Dimensions.half)
                .fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.add_manga_online_clipboard),
                fontSize = Fonts.Size.bigger
            )

            Text(
                "$clipboardText",
                fontSize = Fonts.Size.less,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
    }
}
