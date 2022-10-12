package com.san.kir.manger.utils.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.san.kir.core.compose.CheckBoxText
import com.san.kir.core.compose.RadioGroup
import com.san.kir.manger.R

@Composable
fun <T> SingleChoiceList(
    title: Int = -1,
    initialValue: T,
    stateList: List<T>,
    textList: List<String>,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = if (title != -1) {
            {
                Text(stringResource(title))
            }
        } else null,
        text = {
            RadioGroup(
                state = initialValue,
                onSelected = {
                    onSelect(it)
                    onDismiss()
                },
                stateList = stateList,
                textList = textList,
                verticalPadding = 8.dp,
            )
        },
        buttons = {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    modifier = Modifier.padding(bottom = 16.dp, end = 16.dp),
                    onClick = onDismiss
                ) {
                    Text(stringResource(R.string.planned_task_button_ready))
                }
            }
        }
    )
}

@Composable
fun MultiChoiceList(
    title: Int = -1,
    items: List<String>,
    textList: List<String>,
    onDismiss: () -> Unit,
    onSelect: (List<String>) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = if (title != -1) {
            {
                Text(stringResource(title))
            }
        } else null,
        text = {
            Column {
                textList.forEachIndexed { _, text ->
                    CheckBoxText(
                        state = items.any { it == text },
                        onChange = {
                            if (it) onSelect(items + text)
                            else onSelect(items - text)
                        },
                        firstText = text,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        },
        buttons = {
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    modifier = Modifier.padding(bottom = 16.dp, end = 16.dp),
                    onClick = onDismiss
                ) {
                    Text(stringResource(R.string.planned_task_button_ready))
                }
            }
        }
    )
}
