package com.san.kir.core.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun <T> SingleChoiceList(
    title: Int = -1,
    initialValue: T,
    stateList: ImmutableList<T>,
    textList: ImmutableList<String>,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit
) {
    BaseListAlert(title, onDismiss, onDismiss) {
        LazyRadioGroup(
            state = initialValue,
            onSelected = {
                onSelect(it)
                onDismiss()
            },
            stateList = stateList,
            textList = textList,
        )
    }
}

@Composable
fun <T> MultiChoiceList(
    title: Int = -1,
    items: PersistentList<T>,
    stateList: ImmutableList<T>,
    textList: ImmutableList<String>,
    onDismiss: () -> Unit,
    onSelect: (ImmutableList<T>) -> Unit
) {
    val tempItems = remember(items) { items.toMutableStateList() }
    BaseListAlert(
        title = title,
        onDismiss = onDismiss,
        onSuccess = { onSelect(tempItems.toImmutableList()) }
    ) {
        LazyColumn {
            items(stateList.size, key = { it }) { index ->
                val state = stateList[index]
                val text = textList[index]

                CheckBoxText(
                    state = tempItems.any { it == state },
                    onChange = {
                        if (it) tempItems.add(state)
                        else tempItems.remove(state)
                    },
                    firstText = text,
//                    modifier = Modifier.padding(vertical = Dimensions.half)
                )
            }
        }
    }
}

@Composable
private fun BaseListAlert(
    title: Int = -1,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = if (title != -1) {
            {
                Text(stringResource(title))
            }
        } else null,
        text = content,
        buttons = {
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    modifier = Modifier.padding(
                        bottom = Dimensions.default,
                        end = Dimensions.default
                    ),
                    onClick = {
                        onSuccess()
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.planned_task_button_ready))
                }
            }
        }
    )
}
