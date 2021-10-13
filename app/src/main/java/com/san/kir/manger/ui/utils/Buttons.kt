package com.san.kir.manger.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> RadioGroup(
    state: T,
    onSelected: (T) -> Unit,
    stateList: List<T>,
    textList: List<String>,
    verticalPadding: Dp = 5.dp,
) {
    Column {
        stateList.zip(textList).forEach { (s, text) ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = verticalPadding)
                .clickable { onSelected(s) })
            {
                RadioButton(selected = state == s, onClick = { onSelected(s) })
                Text(text, modifier = Modifier.padding(horizontal = 10.dp))
            }
        }
    }
}
